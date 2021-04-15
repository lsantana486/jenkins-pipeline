withFolderProperties {
    def nexusURL = "http://192.168.0.124:8081"
    def config = [
      sharedlibs: [
        python: [
          semanticVersionPattern: ~/^[0-9]+\.[0-9]+\.[0-9]+(\+[a-zA-Z]+)?$/,
          releaseRepositoryName: "wm-pypi-release",
          releaseRepositoryUrl: "${nexusURL}/repository/wm-pypi-release/",
          snapshotsRepositoryName: "wm-pypi-snapshot",
          snapshotsRepositoryUrl: "${nexusURL}/repository/wm-pypi-snapshot/"
        ],
        node: [
          semanticVersionPattern: ~/^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z]+)?$/,
          releaseRepositoryName: "wm-npm-release",
          releaseRepositoryUrl: "${nexusURL}/repository/wm-npm-release/",
          snapshotsRepositoryName: "wm-npm-snapshot",
          snapshotsRepositoryUrl: "${nexusURL}/repository/wm-npm-snapshot/"
        ],
        java: [
          semanticVersionPattern: ~/^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$/,
          releaseRepositoryName: "wm-maven2-release",
          releaseRepositoryUrl: "${nexusURL}/repository/wm-maven2-release/",
          snapshotsRepositoryName: "wm-maven2-snapshot",
          snapshotsRepositoryUrl: "${nexusURL}/repository/wm-maven2-snapshot/"
        ],
        customImg: [
          dockerImage: "wm-custom-img:latest"
        ]
      ],
      nexusURL: "http://192.168.0.124:8081"
    ]
    checkout ([
      $class: 'GitSCM',
      branches: [[name: "${params.BRANCH}"]],
      extensions: [
        [$class: 'PruneStaleBranch'],
        [$class: 'CleanCheckout'],
        [$class: 'UserIdentity', email: 'dev@null', name: 'devnull'],
        [$class: 'CloneOption', noTags: false, reference: '', shallow: true, depth: 20]
      ],
      userRemoteConfigs: [[url: "${params.GIT_URL}"]]
    ])
    stage("Detect Artifact Type Settings") {
      artifactTypeSettings = getArtifactTypeSettings(config)
    }
    stage("Build Artifact") {
      builtArtifact = buildArtifact(config, artifactTypeSettings)
    }
    versionModel = getVersionModel(builtArtifact.version)
    stage("Publish Artifact") {
      publishArtifact(config, artifactTypeSettings, builtArtifact.version)
    }
    stage("Add Grafeas Occurence") {
      addGrafeasOccurence(config, builtArtifact, artifactTypeSettings)
    }
}

def detectArtifactTypeSettings() {
    def type = null
    if(fileExists('./setup.py')) {
        type = "python" 

    } else if(fileExists('./package.json')) {
        type = "node"
    
    } else if(fileExists('./pom.xml')) {
        type = "maven"
    
    }
    return type;
}

def getArtifactTypeSettings(config) {
  def artifactTypeSettings = [:]
  def dockerImage = null
  dockerImage = customImgBuildPackage(config)
    
  switch(detectArtifactTypeSettings()) {
    case "python":
      artifactTypeSettings.runtime = config.sharedlibs.python
      artifactTypeSettings.buildFn = factoryBuilderPythonPackage()
      artifactTypeSettings.publishFn = factoryPublisherPythonPackage()
      break;
        
    case "node":
      artifactTypeSettings.runtime = config.sharedlibs.node
      artifactTypeSettings.buildFn = factoryBuilderNPMPackage()
      artifactTypeSettings.publishFn = factoryPublisherNPMPackage()
      break;

    case "maven":
      artifactTypeSettings.runtime = config.sharedlibs.java
      artifactTypeSettings.buildFn = factoryBuilderMavenPackage()
      artifactTypeSettings.publishFn = factoryPublisherMavenPackage()
      break;

    default:
      println "Runtime not supported"
      sh "exit 1"
      break;
  }
  artifactTypeSettings.dockerImage = dockerImage 
  return artifactTypeSettings;
}

def buildArtifact(config, artifactTypeSettings) {
  artifactTypeSettings.dockerImage.inside("--entrypoint '' -u 0:0 -w /mnt -v ${pwd()}:/mnt") {
    builtArtifact = artifactTypeSettings.buildFn(config)
  }
  return builtArtifact;
}

def publishArtifact(config, artifactTypeSettings, builtVersion) {
  artifactTypeSettings.dockerImage.inside("--entrypoint '' -u 0:0 -w /mnt -v ${pwd()}:/mnt") {
    artifactTypeSettings.publishFn(config, builtVersion)
  }
}

def customImgBuildPackage(config) {
  def image = docker.build(config.sharedlibs.customImg.dockerImage, ".")
  return image;
}

def factoryBuilderPythonPackage() {
  return {config ->
    def artifact = [:]
    artifact.version = sh(script: 'python3.8 setup.py --version', returnStdout: true).trim()
    artifact.name = sh(script: 'python3.8 setup.py --name', returnStdout: true).trim()
    /* CHECK SEMANTIC VERSION */
    validateSemanticVersion(artifact.version, config.sharedlibs.python.semanticVersionPattern)
    /* ADD METADATA */
    sh "echo \"[metadata]\nurl = ${env.GIT_URL}\nmaintainer = ${env.BUILD_URL}\n\" > setup.cfg"
    /* BUILD */
    sh "python3.8 setup.py bdist_wheel"
    return artifact;
  };
}

def factoryPublisherPythonPackage() {
  return {config, version -> 
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'user', passwordVariable: 'pass']]) {
      def pypiRepositoryUrl = getRepositoryURL(version, config.sharedlibs.python.snapshotsRepositoryUrl, config.sharedlibs.python.releaseRepositoryUrl)
      sh "twine upload --repository-url ${pypiRepositoryUrl} -u ${user} -p ${pass} dist/*; rm -rf ./dist ./build"
      sh "git clean -fdx"
    }
  };
}

def factoryBuilderNPMPackage() {
  return {config ->
    def artifact = [:]
    artifact.version = sh(script: 'npx -c \'echo "$npm_package_version"\'', returnStdout: true).trim()
    artifact.name = sh(script: 'npx -c \'echo "$npm_package_name"\'', returnStdout: true).trim()
    /* CHECK SEMANTIC VERSION */
    validateSemanticVersion(artifact.version, config.sharedlibs.node.semanticVersionPattern)
    /* ADD METADATA */
    sh "cat package.json | jq '.homepage = \"${env.GIT_URL}\" | .contributors = [\"${env.BUILD_URL}\"]' | tee package.json"
    /* BUILD */
    sh "npm pack"
    return artifact;
  };
}

def factoryPublisherNPMPackage() {
  return {config, version -> 
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'user', passwordVariable: 'pass']]) {
      def npmRepositoryUrl = getRepositoryURL(version, config.sharedlibs.node.snapshotsRepositoryUrl, config.sharedlibs.node.releaseRepositoryUrl)
      sh "npm config set registry ${npmRepositoryUrl}"
      sh "set +x; npm config set _auth \$(echo -n \"${user}:${pass}\" | openssl base64)"
      sh "npm publish"
      sh "git clean -fdx"
    }
  };
}

def factoryBuilderMavenPackage() {
  return {config -> 
    def artifact = [:]
    artifact.version = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim();
    artifact.name = sh(script: 'mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout', returnStdout: true).trim();
    /* CHECK SEMANTIC VERSION */
    validateSemanticVersion(artifact.version, config.sharedlibs.java.semanticVersionPattern)
    /* BUILD */
    sh "mvn package -Dmaven.test.failure.ignore=false"
    return artifact;
  };
}

def factoryPublisherMavenPackage() {
  return {config, version ->
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'user', passwordVariable: 'pass']]) {
      def mavenRepositoryUrl = getRepositoryURL(version, config.sharedlibs.java.snapshotsRepositoryUrl, config.sharedlibs.java.releaseRepositoryUrl)
      /* PUBLISH CODE */
      sh "set +x; echo \"<settings><servers><server><id>nexus</id><username>${user}</username><password>${pass}</password></server></servers></settings>\" > settings.xml"
      sh "mvn deploy -s settings.xml -DaltDeploymentRepository=nexus::default::${mavenRepositoryUrl}"
    }
  };
}

def validateSemanticVersion(version, pattern) {
  if (version ==~ pattern) {
    println "${version} is complaint with Semantic Versioning"
  }else{
    error "${version} is not complaint with Semantic Versioning"
  }
}

def getRepositoryURL(version, snapshotsRepositoryUrl, releaseRepositoryUrl) {
  return getVersionModel(version) == 'snapshot' ? snapshotsRepositoryUrl : releaseRepositoryUrl;
}

def getVersionModel(version) {
  return version.toLowerCase().contains('snapshot') ? 'snapshot' : 'release';
}

def searchArtifactOnRepository(config, artifactName, artifactVersion, artifactRepository) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexus', usernameVariable: 'user', passwordVariable: 'pass']]) {
    def responseRaw = sh(label: 'Query Nexus', script: "curl -u ${user}:${pass} -X GET '${config.nexusURL}/service/rest/v1/search?repository=${artifactRepository}&name=${artifactName}&version=${artifactVersion}'", returnStdout: true)
    println "RESPONSE RAW: ${responseRaw}"
    def response = readJSON(text: responseRaw)
    return response;
  }
}

def addGrafeasOccurence(config, builtArtifact, artifactTypeSettings) {
  def responseSearch = searchArtifactOnRepository(config, builtArtifact.name, builtArtifact.version.toLowerCase().replace("snapshot", "*"), artifactTypeSettings.runtime.snapshotsRepositoryName)
  /*
  sh """
curl --location --request POST 'http://localhost:8085/v1beta1/projects/occurrences/occurrences' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "projects/occurrences/occurrences/mypylibBuildOccurrence",
    "resource": {
        "name": "mypylib",
        "uri": "http://192.168.0.124:8081/repository/wm-pypi-snapshot/packages/mypylib/0.2.1+snapshot/mypylib-0.2.1+snapshot-py3-none-any.whl"
    },
    "noteName": "projects/provider_1/notes/mypylib",
    "kind": "BUILD",
    "build": {
        "provenance": {
            "id": "0.2.1+snapshot",
            "project_id": "mypylib",
            "commands": [],
            "built_artifacts": [
                {
                    "checksum": "b73c66a13c024d23d2df26593a0e29d6eec47dd5891ad414610b5b55774e859d",
                    "id": "default@8E8C24D1-29A1A1ED-B82F76FF-C4F14E16-2F8A768B:2346b893-2988-425a-8ac7-a263519665ed",
                    "names": []
                }
            ],
            "create_time": "2021-04-14T23:09:12.733Z",
            "start_time": "2021-04-14T23:07:12.733Z",
            "end_time": "2021-04-14T23:09:12.733Z",
            "creator": "lsantana",
            "logs_uri": "http://localhost:8080/job/POC/job/buildArtifacts/14/console",
            "source_provenance": {
                "artifact_storage_source_uri": "http://localhost:8081/#browse/browse:wm-pypi-snapshot:mypylib%2F0.2.1%2Bsnapshot%2Fmypylib-0.2.1%2Bsnapshot-py3-none-any.whl",
                "context": {
                    "git": {
                        "url": "https://github.com/lsantana486/test-pythonlib",
                        "revision_id": "03916dddbb3b4aad2adb3bc0f66f32d140e1db0b"
                    },
                    "labels": {}
                }
            },
            "trigger_id": "03916dddbb3b4aad2adb3bc0f66f32d140e1db0b",
            "builder_version": "1.0.0"
        },
        "provenance_bytes": "Z3JhZmVhcw=="
    }
}'
    """
    */
}

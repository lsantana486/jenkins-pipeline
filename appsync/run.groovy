//sh "cp appsync/amplify-headless.sh /Users/l.santana/.jenkins/workspace/POC/"
sh "mkdir -p api"
dir('api') {
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
  withAWS(profile:'amplify-datalegion') {
    //sh 'bash /Users/l.santana/.jenkins/workspace/POC/amplify-headless.sh'
    sh 'bash $WORKSPACE/appsync/amplify-headless.sh'
  }
}

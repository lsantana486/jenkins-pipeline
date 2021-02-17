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
  sh """\
    AMPLIFY='{ \
      \"projectName\":\"amplifyheadlessci\",\
      \"envName\":\"dev\",\
      \"defaultEditor\":\"code\"\
    }' \
    APP='{ \
      \"frontend\":\"javascript\",\
      \"framework\":\"none\",\
      \"config\":{\
        \"SourceDir\":\"src\",\
        \"DistributionDir\":\"dist\",\
        \"BuildCommand\":\"npm run-script build\",\
        \"StartCommand\":\"npm run-script start\"\
      }\
    }' \
    PROVIDERS='{\
      \"awscloudformation\":{
        \"configlevel\":\"project\",\
        \"useProfile\":false,\
        \"profileName\":\"amplify-datalegion\",\
        \"accessKeyId\":\"\$AWS_ACCESS_KEY_ID\",\
        \"secretAccessKey\":\"\$AWS_SECRET_ACCESS_KEY\",\
        \"region\":\"us-east-1\"\
      }\
    }'\
    amplify init \
    --amplify \$AMPLIFY \
    --frontend \$APP \
    --providers \$PROVIDERS \
    --yes \
  """
}

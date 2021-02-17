sh "cp appsync/amplify-headless.sh /Users/l.santana/.jenkins/workspace/POC/ && alias amplify-headless='/Users/l.santana/.jenkins/workspace/POC/amplify-headless.sh'"
sh "ls -la /Users/l.santana/.jenkins/workspace/POC/"
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
sh "ls -la /Users/l.santana/.jenkins/workspace/POC/"
withAWS(profile:'amplify-datalegion') {
  sh "amplify-headless"
}

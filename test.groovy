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
sh "echo test"

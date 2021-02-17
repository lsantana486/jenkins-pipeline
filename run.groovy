node('master') {
  wrap([$class: 'TimestamperBuildWrapper']) {
    checkout scm
    checkout ([
        $class: 'GitSCM',
        branches: [[name: "${p.BRANCH ? p.BRANCH : 'main'}"]],
        extensions: [
          [$class: 'PruneStaleBranch'],
          [$class: 'CleanCheckout'],
          [$class: 'UserIdentity', email: 'dev@null', name: 'devnull'],
          [$class: 'CloneOption', noTags: false, reference: '', shallow: true, depth: 20]
        ],
        userRemoteConfigs: [[url: "${p.GIT_URL}"]]
    ])
    load "${p.TEMPLATE ? p.TEMPLATE : 'test.groovy'}"
  } 
}

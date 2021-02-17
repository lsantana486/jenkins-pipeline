node('master') {
  wrap([$class: 'TimestamperBuildWrapper']) {
    checkout scm 
    load "${params.TEMPLATE}"
  } 
}

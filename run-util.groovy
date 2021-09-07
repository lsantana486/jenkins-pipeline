node('master') {
  wrap([$class: 'TimestamperBuildWrapper']) {
    checkout scm 
    util = load "util.groovy"
    util.testCL {
      println "INSIDE"
    }
  } 
}


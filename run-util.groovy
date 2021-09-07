node('master') {
  wrap([$class: 'TimestamperBuildWrapper']) {
    checkout scm 
    def util = load "util.groovy"
    util.testCl {
      println "INSIDE"
    }
  } 
}


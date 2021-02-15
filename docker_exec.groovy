node('master') {
  checkout scm
  docker.image("alpine:latest").inside('--entrypoint "" -u 0:0') {
    sh "ls -la /Users/l.santana/.jenkins/workspace/POC"
    sh "touch /Users/l.santana/.jenkins/workspace/POC/test.test"
  }
} 

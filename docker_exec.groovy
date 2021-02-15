node('master') {
  checkout scm
  def img = docker.build("jkalpine")
  img.inside('--entrypoint "" -u 0:0') {
    sh "ls -la /Users/l.santana/.jenkins/workspace/POC"
    sh "touch /Users/l.santana/.jenkins/workspace/POC/test.test"
    sh "sleep 600"
  }
} 

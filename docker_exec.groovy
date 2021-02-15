node('master') {
  checkout scm
  def img = docker.build("jkalpine")
  img.inside('--entrypoint "" -u 0:0') {
    sh "ls -la"
    sh "npm install"
  }
} 

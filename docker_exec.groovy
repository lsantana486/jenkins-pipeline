node('master') {
  checkout scm
  docker.image("alpine:latest").inside('--entrypoint "" -u 0:0') {
    sh "ls -la /"
    sh "mkdir -p /test"
    sh "sleep 600"
  }
} 

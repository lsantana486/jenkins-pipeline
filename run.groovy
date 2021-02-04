node('master') {
  checkout scm
  sh "ls -la"
  def wd = sh(script: 'pwd', returnStdout: true).trim()
  def client = evaluate(new File("${wd}/RestClient.groovy"))

  def resp = client.post('http://httpbin.org/post', [
    jobName       : env.JOB_NAME
  ])
  assert resp.status == 201
  println "${resp}"
}

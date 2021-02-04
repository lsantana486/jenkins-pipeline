node('master') {
  def client = evaluate(new File('./RestClient.groovy'))

  def resp = client.post('http://httpbin.org/post', [
    jobName       : env.JOB_NAME
  ])
  assert resp.status == 201
  println "${resp}"
}

node('master') {
  checkout scm
  def patchOrg = """
    {"test": "test"}
  """
  def response = httpRequest acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: patchOrg, url: 'http://httpbin.org/post'
  println('Status: '+response.status)
  println('Response: '+response.content)
}

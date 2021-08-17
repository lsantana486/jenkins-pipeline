import com.amazonaws.DefaultRequest
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AwsCredentialsFromSystem

def endpoint = "search-lsantana-dnx3pxw56bwwkvija6naqyoz24.us-east-2.es.amazonaws.com"
def payload = '''{ "index": { "_index": "mstack360-poc-notes", "_id": "6e568f1e-5235-4d6b-b3c9-684f33b31ed5" } }
{"name":"wm-demo-lib","type":"note","shortDescription":"This is a test","longDescription":"This is a test using ELK to save metadata for wm-demo-lib","kind":"BUILD","build":{"builderVersion":"1.0.0","signature":{"publicKey":"","signature":"Z3JhZmVhcw==","keyId":"04A49FE3","keyType":"PGPKEY"}},"id":"6e568f1e-5235-4d6b-b3c9-684f33b31ed5","@timestamp":"2021-08-01T01:00:00.000Z"}'''

withAWS(credentials: "awspoc", duration: 900, roleSessionName: "jenkins-session", region: "us-east-1") {
    def signedRequest = generateElasticSignedRequest(endpoint, payload)
    println "SIGNED REQUEST: ${signedRequest}"
}

@NonCPS
def generateElasticSignedRequest(endpoint, payload, region="us-east-1") {
  def request = new DefaultRequest<Void>("es".toString())
  def body = "${payload}".toString().getBytes()
  request.setEndpoint(URI.create("https://${endpoint}".toString()))
  request.setHttpMethod(HttpMethodName.POST)
  request.setContent(new ByteArrayInputStream(body))
  request.setHeaders(
    [
        'Host': "${endpoint}".toString(),
        'Content-Type': 'application/json'.toString(),
        'Content-Length': "${body.size()}".toString()
    ]
  )
  request.setResourcePath("/_bulk".toString())
  
  def signer = new AWS4Signer(false)
  signer.setRegionName("${region}".toString())
  signer.setServiceName("es".toString())
  signer.sign(request, new AwsCredentialsFromSystem())
  return [
    headers: request.getHeaders(),
    endpoint: "${request.getEndpoint()}${request.getResourcePath()}",
    content: request.getContent(),
    method: request.getHttpMethod()
  ]
}
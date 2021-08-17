import com.amazonaws.DefaultRequest
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.HttpMethodName
import groovy.json.JsonSlurper


def endpoint = "search-lsantana-sgppskvlgamskg4saasbs2rpca.us-east-1.es.amazonaws.com"
def document = [
    id: '8a698e0d-5235-4d6b-b3c9-684f33b31ed4',
    '@timestamp': '2021-04-19T14:59:39.000Z',
    appgroup: 'apptmwf',
    application: 'hdrwf',
    service: 'acquire',
    runtime: 'python',
    'mstack_type': 'lambda'
]

def index = [
    index: [
        '_index': 'mstack360-poc',
        '_id': '8a698e0d-5235-4d6b-b3c9-684f33b31ed4'
    ]
]

def payload = """
${mapToJson(index)}
${mapToJson(document)}
"""

withAWS(credentials: "awspoc", duration: 900, roleSessionName: "jenkins-session", region: "us-east-1") {
  def awsAccessKeys = [
    'access_key': "${env.AWS_ACCESS_KEY_ID}".toString(),
    'secret_key': "${env.AWS_SECRET_ACCESS_KEY}".toString()
  ]
  def signedRequest = generateElasticSignedRequest(endpoint, payload, awsAccessKeys)
  println "SIGNED REQUEST: ${signedRequest}"
  def response = invokeAPI(signedRequest)
  println "RESPONSE: ${response}"
}

def mapToJson(jsonMap) {
    writeJSON(json: jsonMap, file: 'tmp.json')
    return sh(label: 'mapToJson', script: "cat tmp.json", returnStdout: true)
}

@NonCPS
def generateElasticSignedRequest(endpoint, payload, awsAccessKeys, region="us-east-1") {
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
  def creds = new BasicAWSCredentials(awsAccessKeys.access_key, awsAccessKeys.secret_key)
  signer.sign(request, creds)
  return [
    headers: request.getHeaders(),
    endpoint: "${request.getEndpoint()}${request.getResourcePath()}",
    content: body,
    method: request.getHttpMethod().toString()
  ]
}

def invokeAPI(signedRequest) {
  def conn = new URL(signedRequest.endpoint).openConnection()
  conn.setRequestMethod("POST")
  signedRequest.headers.each { key, value -> conn.setRequestProperty(key, value)}
  conn.setDoOutput(true)
  conn.getOutputStream().write(signedRequest.content)
  JsonSlurper jsonSlurper = new JsonSlurper()
  def response = conn.getContent().newReader()
  def apiResponse = jsonSlurper.parse(response)
  return apiResponse
}

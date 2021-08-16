import com.amazonaws.DefaultRequest
import com.amazonaws.SignableRequest
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.HttpMethodName
import com.amazonaws.AmazonServiceException
import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.ClientConfiguration
import com.amazonaws.DefaultRequest
import com.amazonaws.Request
import com.amazonaws.Response
import com.amazonaws.http.AmazonHttpClient
import com.amazonaws.http.ExecutionContext
import com.amazonaws.http.HttpResponseHandler
import com.amazonaws.http.JsonErrorResponseHandler;
import com.amazonaws.http.JsonResponseHandler;
import groovy.json.JsonSlurper
import groovy.io.FileType

public class StringResponseHandler implements HttpResponseHandler<AmazonWebServiceResponse<String>> {

    @Override
    public AmazonWebServiceResponse<String> handle(com.amazonaws.http.HttpResponse response) throws IOException {

        AmazonWebServiceResponse<String> awsResponse = new AmazonWebServiceResponse<>();

        //putting response string in the result, available outside the handler
        awsResponse.setResult((String) IOUtils.toString(response.getContent()));

        return awsResponse;
    }

    @Override
    public boolean needsConnectionLeftOpen() {
        return false;
    }

} 

withAWS(credentials: "awspoc", duration: 900, roleSessionName: "jenkins-session", region: "us-east-1") {
    sendRequest("${env.AWS_ACCESS_KEY_ID}".toString(), "${env.AWS_SECRET_ACCESS_KEY}".toString())
}

@NonCPS
def sendRequest(user, pass) {
  def endpoint = "search-lsantana-dnx3pxw56bwwkvija6naqyoz24.us-east-2.es.amazonaws.com".toString()
  def payload = '''{ "index": { "_index": "mstack360-poc-notes", "_id": "6e568f1e-5235-4d6b-b3c9-684f33b31ed5" } }
{"name":"wm-demo-lib","type":"note","shortDescription":"This is a test","longDescription":"This is a test using ELK to save metadata for wm-demo-lib","kind":"BUILD","build":{"builderVersion":"1.0.0","signature":{"publicKey":"","signature":"Z3JhZmVhcw==","keyId":"04A49FE3","keyType":"PGPKEY"}},"id":"6e568f1e-5235-4d6b-b3c9-684f33b31ed5","@timestamp":"2021-08-01T01:00:00.000Z"}'''.toString()
  def request = new DefaultRequest<Void>("es".toString())
  def body = payload.getBytes()
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
  signer.setRegionName("us-east-1".toString())
  signer.setServiceName("es".toString())
  def creds = new BasicAWSCredentials(user, pass)
  signer.sign(request, creds)
  def response = new AmazonHttpClient(new ClientConfiguration())
    .requestExecutionBuilder()
    .executionContext(new ExecutionContext(false))
    .request(request)
    .execute(new StringResponseHandler());
  awsResponse = response.getAwsResponse().getResult();
}

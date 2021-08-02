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
import groovy.json.JsonSlurper
import groovy.io.FileType

public final class DummyHandler<T> implements HttpResponseHandler<T> {
    private final T preCannedResponse;
    public DummyHandler(T preCannedResponse) { this.preCannedResponse = preCannedResponse; }

    @Override
    public T handle(HttpResponse response) throws Exception {
        System.out.println(IOUtils.toString(response.getContent()));
        return preCannedResponse;
    }

    @Override
    public boolean needsConnectionLeftOpen() { return false; }
}

def payload = '''{ "index": { "_index": "mstack360-poc-notes", "_id": "6e568f1e-5235-4d6b-b3c9-684f33b31ed5" } }
{"name":"wm-demo-lib","type":"note","shortDescription":"This is a test","longDescription":"This is a test using ELK to save metadata for wm-demo-lib","kind":"BUILD","build":{"builderVersion":"1.0.0","signature":{"publicKey":"","signature":"Z3JhZmVhcw==","keyId":"04A49FE3","keyType":"PGPKEY"}},"id":"6e568f1e-5235-4d6b-b3c9-684f33b31ed5","@timestamp":"2021-08-01T01:00:00.000Z"}'''
def request = new DefaultRequest<String>("es")
def body = new ByteArrayInputStream(payload.getBytes())
request.setEndpoint(URI.create("https://search-ps-logs-preprod-applogs-ou4fjmihj4tkdmcuryv6pnxtuy.us-east-1.es.amazonaws.com"))
request.setHttpMethod(HttpMethodName.POST)
request.setContent(body)
request.setHeaders(
    [
        'Host': 'search-ps-logs-preprod-applogs-ou4fjmihj4tkdmcuryv6pnxtuy.us-east-1.es.amazonaws.com',
        'Content-Type': 'application/json',
        'Content-Length': body.size()
    ]
)
request.setResourcePath("/_bulk")


def signer = new AWS4Signer(false)
signer.setRegionName("us-east-1")
signer.setServiceName("es")
withAWS(credentials: "awspoc", duration: 900, roleSessionName: "jenkins-session", region: "us-east-1") {
    signer.sign(request, new BasicAWSCredentials("${user}", "${pass}"))
    def response = new AmazonHttpClient(new ClientConfiguration())
            .requestExecutionBuilder()
            .executionContext(new ExecutionContext(true))
            .request(request)
            .errorResponseHandler(new DummyHandler<>(new AmazonServiceException("oops")))
            .execute(new DummyHandler<>(new AmazonWebServiceResponse<Void>()));
    awsResponse = response.getAwsResponse();
}

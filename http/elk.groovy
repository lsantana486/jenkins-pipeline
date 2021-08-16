def mapToJson(jsonMap) {
    writeJSON(json: jsonMap, file: 'tmp.json')
    return sh(label: 'mapToJson', script: "cat tmp.json", returnStdout: true)
}

def packageJson = '''
{
  "name": "elk-ingest",
  "version": "1.0.0",
  "description": "",
  "main": "app.js",
  "dependencies": {
    "aws-sdk": "^2.651.0"
  },
  "scripts": {},
  "devDependencies": {}
}
'''

def runnerCode = '''
const AWS = require("aws-sdk");
const fs = require("fs");

function getInputs() {
    let args = process.argv.slice(2);
    let parsedArgs = {};
    args.forEach(value => {
        parsedArgs[value.split("=")[0].replace("--", "")] = value.split("=")[1]
    })
    return parsedArgs;
}

function execRequest() {
    let inputs = getInputs();
    const data = fs.readFileSync(inputs["data-file"], "utf8")
    let requestParams = buildRequest(inputs.endpoint, inputs.region, data);
    const signer = new AWS.Signers.V4(requestParams, "es");
    signer.addAuthorization(
        {
            accessKeyId: process.env["AWS_ACCESS_KEY_ID"],
            secretAccessKey: process.env["AWS_SECRET_ACCESS_KEY"],
            sessionToken: process.env["AWS_SESSION_TOKEN"]
        },
        new Date()
    );
    let client = new AWS.HttpClient();
    let request = client.handleRequest(requestParams, null, function (response) {
        console.log(response.statusCode + " " + response.statusMessage);
        let responseBody = "";
        response.on("data", function (chunk) {
            responseBody += chunk;
        });
        response.on("end", function (chunk) {
            console.log("Response body: " + responseBody);
        });
    }, function(error) {
        console.log("Error: " + error);
    });
}

function buildRequest(endpoint, region, data) {
    let awsEndpoint = new AWS.Endpoint(endpoint);
    let request = new AWS.HttpRequest(awsEndpoint, region);
    request.method = "POST";
    request.path = "/_bulk";
    request.body = data;
    request.headers["Host"] = endpoint;
    request.headers["Content-Type"] = "application/json";
    request.headers["Content-Length"] = Buffer.byteLength(request.body);
    return request;
}

execRequest();
'''

def document = [
    id: '7a698e0d-5235-4d6b-b3c9-684f33b31ed4',
    '@timestamp': '2021-04-19T14:59:39.000Z',
    appgroup: 'apptmwf',
    application: 'hdrwf',
    service: 'acquire',
    runtime: 'python',
    'mstack_type': 'v3-lambda'
]

def index = [
    index: [
        '_index': 'mstack360-poc',
        '_id': '7a698e0d-5235-4d6b-b3c9-684f33b31ed4'
    ]
]

def payload = """
${mapToJson(index)}
${mapToJson(document)}
"""

def payloadFile = '/tmp/payload'
def endpoint = 'search-lsantana-dnx3pxw56bwwkvija6naqyoz24.us-east-2.es.amazonaws.com'
def region = 'us-east-1'

docker.image('node:12').inside('--entrypoint "" -u 0:0') {
    sh "echo '${payload}' | tee '${payloadFile}'"
    sh "echo '${packageJson}' | tee package.json"
    sh "echo '${runnerCode}' | tee app.js"
    sh "npm install"
    withAWS(credentials: "awspoc", duration: 900, roleSessionName: "jenkins-session", region: "us-east-1") {
        sh "node app.js --endpoint=${endpoint} --region=${region} --data-file=/tmp/payload"
    }
}



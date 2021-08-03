const AWS = require('aws-sdk');
const fs = require('fs')

function getInputs() {
    let args = process.argv.slice(2);
    let parsedArgs = {};
    args.forEach(value => {
        parsedArgs[value.split('=')[0].replace('--','')] = value.split('=')[1] 
    })
    return parsedArgs;
}

function execRequest() {
    let inputs = getInputs();
    const data = fs.readFileSync(inputs['data-file'], 'utf8')
    let requestParams = buildRequest(inputs.endpoint, inputs.region, data);
    const signer = new AWS.Signers.V4(requestParams, 'es');
    signer.addAuthorization(
        {
            accessKeyId: process.env['AWS_ACCESS_KEY_ID'],
            secretAccessKey: process.env['AWS_SECRET_ACCESS_KEY'],
            sessionToken: process.env['AWS_SESSION_TOKEN']
        }, 
        new Date()
    );
    let client = new AWS.HttpClient();
    let request = client.handleRequest(requestParams, null, function (response) {
        console.log(response.statusCode + ' ' + response.statusMessage);
        let responseBody = '';
        response.on('data', function (chunk) {
            responseBody += chunk;
        });
        response.on('end', function (chunk) {
            console.log('Response body: ' + responseBody);
        });
    }, function(error) {
        console.log('Error: ' + error);
    });
}

function buildRequest(endpoint, region, data) {
    let awsEndpoint = new AWS.Endpoint(endpoint);
    let request = new AWS.HttpRequest(awsEndpoint, region);
    request.method = 'POST';
    request.path = '/_bulk';
    request.body = data;
    request.headers['Host'] = endpoint;
    request.headers['Content-Type'] = 'application/json';
    request.headers['Content-Length'] = Buffer.byteLength(request.body);
    return request;
}

execRequest();
import groovy.json.JsonSlurper
import groovy.json.JsonOutput 

JsonSlurper jsonSlurper = new JsonSlurper()
def connection = new URL('http://httpbin.org/get').openConnection()
def response = connection.getContent().newReader()
def apiResponse = jsonSlurper.parse(response)
println "${apiResponse}"
def output = JsonOutput.toJson(apiResponse)
println "${output}"

def response = 'http://httpbin.org/get'.toURL().getText()
def result = new JsonSlurper().parseText(response)
def content = new String(result.content.decodeBase64())
def yamlConfig = readYaml(text: content)

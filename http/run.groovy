import groovy.json.JsonSlurper
JsonSlurper jsonSlurper = new JsonSlurper()
def connection = new URL('http://httpbin.org/get').openConnection()
def response = connection.getContent().newReader()
def apiResponse = jsonSlurper.parse(response)
println "${apiResponse}"

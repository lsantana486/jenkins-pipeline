@Grapes(
    @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
)

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

node('master') {
  checkout scm
  def http = new HTTPBuilder("http://httpbin.org/post'")
  http.request(POST, JSON ) { req ->
    body = []
    response.success = { resp, reader ->
        println "$resp.statusLine   Respond rec"

    }
  }
}

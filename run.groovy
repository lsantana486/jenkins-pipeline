import groovy.json.JsonSlurper;
import org.apache.commons.codec.binary.Base64;

node('master') {
  checkout scm
  def get = new URL('http://localhost:8081/service/rest/v1/search?repository=wm-maven&name=my-app&version=1.0.2')
  def authEncBytes = Base64.encodeBase64("admin:nexus@local".getBytes())
  def authStringEnc = new String(authEncBytes)
  def conn = get.openConnection()
  conn.setRequestProperty("Authorization", "Basic ${authStringEnc}")
  conn.setRequestProperty("Accept", "application/json")
  conn.setRequestMethod("GET")
  if (conn.getResponseCode().equals(200)){
    def slurper = new groovy.json.JsonSlurper()
    def result = slurper.parseText(con.getInputStream().getText());
    println "RESPONSE: ${result.items}"
  } else {
    println "RESPONSE: error"
  }
}

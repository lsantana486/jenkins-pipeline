def test = sh(script: 'for rs in \$(find ./bash/ -type f \\( -iname \"*.ruleset\" ! -iname \"*.strict.ruleset\" \\)); do OUTPUT=\"\$(cat $rs)\n\$OUTPUT\"; done && echo $OUTPUT', returnStdout: true)
println "TEST: ${test}"

//def cmd = "sleep 10 && echo test && exit 0"
//def bPID = sh(script: "bash \$WORKSPACE/bash/bg-process.sh \"${cmd}\"", returnStdout: true)
//println "PID: ${bPID}"
//sh "wait ${bPID} && echo \$?"
def cmd = 'sleep 10 && echo test && exit 0'.execute()
while (true) {
  def breakCmd = sh(script: '[ -f stop ] && echo 1 || echo 0', returnStdout: true).trim().equals("1")
  if (breakCmd) {
    cmd.destroy()
    break
  } else {
    println "Checking..."
    sleep(5)
  }
}

def exitValue = cmd.exitValue()
println "${exitValue}"

cmd.in.eachLine { println it }
cmd.err.eachLine { println it }
println "${cmd.text}"

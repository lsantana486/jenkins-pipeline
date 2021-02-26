// Test process
//println "$WORKSPACE/bash/bg-process.sh".execute().text


def test = sh(script: 'find ./bash/ -type f \\( -iname \"*.ruleset\" ! -iname \"*.strict.ruleset\" \\) | jq -R . | jq -s .', returnStdout: true)
println "TEST: ${test}"

def output = ""
for(ruleset in readJSON(text: test)) {
  output += sh(script: "cat ${ruleset}", returnStdout: true).trim() << "\n"
}

println "OUTPUT: ${output}"

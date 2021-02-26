// Test process
//println "$WORKSPACE/bash/bg-process.sh".execute().text


def test = sh(script: 'find ./bash/ -type f \\( -iname \"*.ruleset\" ! -iname \"*.strict.ruleset\" \\) | jq -R . | jq -s .', returnStdout: true)
println "TEST: ${test}"

def output = ""
for(ruleset in readJSON(text: test)) {
  output += sh(script: "cat ${ruleset}", returnStdout: true).trim() << "\n"
}

println "OUTPUT: ${output}"

test = sh(script: 'cat ./bash/output.txt', returnStdout: true).trim()
def splitTest = test.tokenize('\n\n')
println splitTest
splitTest = splitTest.unique()
println splitTest
def newTest = []
for (st in splitTest) {
    //println st
    def matcherA = st =~ /^\[(?<policy>.*)\] failed because \[(?<rscr>.*)\] is(?<sep>.*) \[(?<value>.*)\] (?<msg>.*)$/
    if (matcherA.matches()) {
        //println matcherA.group("policy")
        //println matcherA.group("rscr")
        //println matcherA.group("value")
        //println matcherA.group("msg")
        newTest.add(matcherA.replaceAll('(${policy}) failed because (${rscr}) is${sep} (${value}) ${msg}'))
    }
    def matcherB = st =~ /^\[(?<policy>.*)\] failed because it does not contain the required property of \[(?<property>.*)\]$/
    if (matcherB.matches()) {
        //println matcherB.group("policy")
        //println matcherB.group("property")
        newTest.add(matcherB.replaceAll('(${policy}) failed because it does not contain the required property of (${property})'))
    }
}
println newTest.join('\n')

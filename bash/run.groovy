def test = sh(script: 'for rs in \$(find ./bash/ -type f \\( -iname \"*.ruleset\" ! -iname \"*.strict.ruleset\" \\)); do OUTPUT=\"\$(cat $rs)\n\$OUTPUT\"; done && echo $OUTPUT', returnStdout: true).trim()
println "TEST: ${test}"

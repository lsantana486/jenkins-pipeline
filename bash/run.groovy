def test = sh(script: 'for rs in \$(find ./bash/ -type f \\( -iname \"*.ruleset\" ! -iname \"*.strict.ruleset\" \\)); do OUTPUT=\"\$(echo $rs)\n\$OUTPUT\"; done', returnStdout: true).trim()


sh "echo test"

def getMStack360Runtime(recursive=true) {
    def runtime = "unknown"
    def tmpRuntime = "unknown"
    if(fileExists('Dockerfile')) {
        runtime = "docker"
            
    } else if(fileExists('package.json')) {
        runtime = "nodejs"
                
    } else if(fileExists('pom.xml')) {
        runtime = "java"
        
    } else {
        def items = findFiles()
        for (item in items) {
            if(item.directory) {
                dir(item.name) {
                  tmpRuntime = getMStack360Runtime(false)
                  if(!tmpRuntime.equals("unknown")) {
                    runtime = tmpRuntime
                  }
                }
            }
        }
    }
    return runtime
}

println "${getMStack360Runtime()}"

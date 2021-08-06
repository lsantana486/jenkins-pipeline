sh "ls -la"
def getMStack360Runtime(recursive=true) {
    def runtime = "unknown"
    def tmpRuntime = "unknown"
    if(fileExists('setup.py')) {
        runtime = "python"
            
    } else if(fileExists('package.json')) {
        runtime = "nodejs"
                
    } else if(fileExists('pom.xml')) {
        runtime = "java"
        
    } else if(recursive) {
        def items = findFiles()
        for (item in items) {
          if(item.directory && !(item.name ==~ /\.git.*/) && !(item.name.contains('@'))) {
            tmpRuntime = getMStack360Runtime(false)
            if(!tmpRuntime.equals("unknown")) {
              sh "ls -la"
              runtime = tmpRuntime
            }
          }
        }
      }
    return runtime
}

println "${getMStack360Runtime()}"

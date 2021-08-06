sh "echo test"

def files = findFiles() 
files.each{ f -> 
  if(f.directory) {
    dir(f.name) {
      sh "ls -la"
    }
  }
}

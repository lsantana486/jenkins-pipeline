tasksSettings = [
  [
    name: "taskA",
    exec: "sleep 6"
  ],
  [
    name: "taskB",
    exec: "sleep 3"
  ],
  [
    name: "taskC",
    exec: "sleep 1"
  ]
]
def barrier = createBarrier count: tasksSettings.size();
echo "Start pipeline"
def tasks = [:]
for (taskSettings in tasksSettings) {
  echo "${taskSettings.name}"
  tasks["${taskSettings.name}"] = {
    execQueue()
  }
}
parallel tasks
/*parallel(
    taskA: {
      awaitBarrier (barrier){
        echo "Start taskA"
        sleep 5
        echo "End taskA"
      }
    },
    taskB: {
      awaitBarrier (barrier){
        echo "Start taskB"
        sleep 2
        echo "End taskB"
      }
    }
)*/

echo "End pipeline"

def execQueue(){
  def taskSettings = tasksSettings.pop()
  echo "Start ${taskSettings.name}"
  sh "${taskSettings.exec}"
  echo "End ${taskSettings.name}"
}

def tasksSettings = [
  [
    name: "taskA",
    exec: "sleep 5"
  ],
  [
    name: "taskB",
    exec: "sleep 3"
  ]
]
def barrier = createBarrier count: tasksSettings.size();
echo "Start pipeline"
def tasks = [:]
for (taskSettings in tasksSettings) {
  echo "${taskSettings.name}"
  tasks["${taskSettings.name}"] = {
    /*awaitBarrier (barrier){
      echo "Start ${taskSettings.name}"
      sh "${taskSettings.exec}"
      echo "End ${taskSettings.name}"
    }*/
    echo "Start ${taskSettings.name}"
    sh "${taskSettings.exec}"
    echo "End ${taskSettings.name}"
  }
}
echo "${tasks}"
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

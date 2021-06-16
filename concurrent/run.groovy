tasksSettings = [
  [
    name: "taskA",
    exec: "sleep 6"
  ],
  [
    name: "taskB",
    exec: "sleep 4"
  ],
  [
    name: "taskC",
    exec: "sleep 4"
  ],
  [
    name: "taskD",
    exec: "sleep 4"
  ],
  [
    name: "taskE",
    exec: "sleep 4"
  ],
  [
    name: "taskF",
    exec: "sleep 4"
  ]
  [
    name: "taskG",
    exec: "sleep 4"
  ],
  [
    name: "taskH",
    exec: "sleep 4"
  ],
  [
    name: "taskI",
    exec: "sleep 4"
  ],
  [
    name: "taskJ",
    exec: "sleep 4"
  ],
  [
    name: "taskK",
    exec: "sleep 4"
  ],
  [
    name: "taskL",
    exec: "sleep 4"
  ],
  [
    name: "taskM",
    exec: "sleep 4"
  ],
  [
    name: "taskN",
    exec: "sleep 4"
  ]
  [
    name: "taskO",
    exec: "sleep 4"
  ],
  [
    name: "taskP",
    exec: "sleep 4"
  ],
  [
    name: "taskQ",
    exec: "sleep 4"
  ]
]
echo "Start pipeline"
def tasks = [:]
for (taskSettings in tasksSettings) {
  echo "${taskSettings.name}"
  tasks["${taskSettings.name}"] = {
    execQueue()
  }
}
parallel tasks
/*def barrier = createBarrier count: tasksSettings.size();
parallel(
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

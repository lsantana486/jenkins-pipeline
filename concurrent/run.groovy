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
  ],
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
  ],
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
chunkTasksSettings = tasksSettings.collate(5)
def tasks = [:]
chunkTasksSettings.eachWithIndex {
  entry, index ->
  echo "Process chunk-${index}"
  tasks["Process chunk-${index}"] = {
    execQueue()
  }
}
/*for (chunkTaskSettings in chunkTasksSettings) {
  echo "${}"
  tasks["${taskSettings.name}"] = {
    execQueue()
  }
}*/
stage("Start") {
  echo "Start pipeline"
}
parallel tasks
stage("End") {
  echo "End pipeline"
}
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


def execQueue(){
  def chunkTaskSettings = chunkTasksSettings.pop()
  for (taskSettings in chunkTaskSettings) {
    echo "Start ${taskSettings.name}"
    sh "${taskSettings.exec}"
    echo "End ${taskSettings.name}"
  }
}

def tasks = [
  [
    name: "TaskA"
    exec: "sleep 5"
  ],
  [
    name: "TaskB"
    exec: "sleep 3"
  ]
]
def barrier = createBarrier count: tasks.size();
echo "Start pipeline"
for (task in tasks) {
  awaitBarrier (barrier){
    echo "Start ${task.name}"
    sh "${task.exec}"
    echo "End ${task.name}"
  }
}
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

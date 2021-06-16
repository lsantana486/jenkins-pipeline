def barrier = createBarrier count: 2;
echo "Start pipeline"
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
)
echo "End pipeline"

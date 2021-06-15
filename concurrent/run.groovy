def barrier = createBarrier count: 2;
parallel(
    await1: {
      awaitBarrier (barrier){
        sleep 1
        echo "Inside Await 1 after sleep 1"
        sleep 5
        echo "Inside Await 1 after sleep 5"
      }
    },
    await3: {
      awaitBarrier (barrier){
        sleep 3
        echo "Inside Await 3 after sleep 3"
        sleep 2
        echo "Inside Await 3 after sleep 2"
      }
    }
)
echo "Outside parallalel"

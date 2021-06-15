def barrier = createBarrier count: 3;
boolean out = false
parallel(
        await1: {
            awaitBarrier barrier
            echo "out=${out}"
        },
        await2: {
            awaitBarrier (barrier){
                sleep 2 //simulate a long time execution.
            }
            echo "out=${out}"
        },
        await3: {
            awaitBarrier (barrier){
                sleep 3 //simulate a long time execution.
                out = true
            }
            echo "out=${out}"
        }
)

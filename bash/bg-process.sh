CMD=$1
echo "Command to execute on background -> $CMD"

BREAK_CMD=$2
echo "Break command to stop the execution of background process -> $BREAK_CMD"

echo "Executing command in background" > output.log
tail -f output.log &
TPID=$!

eval "$CMD" 2>&1 >> output.log &
PID=$!
echo "Brackground process PID: $PID"

while ps -p "$PID" > /dev/null; do
  if [ $(eval "$BREAK_CMD") = 1 ] 
  then
    echo "Stoping $PID"
    kill $PID
    kill $TPID
    echo "Stopped $PID" >> output.log
    echo "{\"exit_code\":0,\"status\":\"test\"}"
  else
    sleep 1
  fi
done
wait $PID
EXIT_CODE=$?
echo "Exit code from background process -> $EXIT_CODE"
echo "Output from background process"
echo "$(cat output.log)"
echo "{\"exit_code\":$EXIT_CODE,\"status\":\"test\"}"
kill $TPID

CMD=$1
echo "Command to execute on background -> $CMD"

BREAK_CMD=$2
echo "Break command to stop the execution of background process -> $BREAK_CMD"

echo "Executing command in background"
eval "$CMD" 2>&1 > output.log &
PID=$!
echo "Brackground process PID: $PID"

while kill -0 "$PID"; do
  if [ $(eval "$BREAK_CMD") = 1 ] 
  then
    echo "Stoping $PID"
    kill $PID
    echo "Stopped $PID" >> output.log
    exit 0
  else
    sleep 1
  fi
done
wait $PID
EXIT_CODE=$?
echo "Exit code from background process -> $EXIT_CODE"
echo "Output from background process"
echo "$(cat output.log)"
exit $EXIT_CODE


CMD=$1
eval "$CMD" 2>&1 > output.log &
PID=$!
echo "$PID"

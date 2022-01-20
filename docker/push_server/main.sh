#!/bin/bash

sed -i "s/ListenIP=.*/ListenIP=0.0.0.0/g" /teamtalk/push_server/pushserver.conf

server=push_server
echo -e "\033[32m $server ==> START ... \033[0m"
./restart.sh $server log
cd /teamtalk/$server
./monitor.sh  $server log
waitterm() {
        local PID
        # any process to block
        tail -f /dev/null &
        PID="$!"
        # setup trap, could do nothing, or just kill the blocker
        trap "kill -TERM ${PID}" TERM INT
        # wait for signal, ignore wait exit code
        wait "${PID}" || true
        # clear trap
        trap - TERM INT
        # wait blocker, ignore blocker exit code
        wait "${PID}" 2>/dev/null || true
}
waitterm
echo "==> STOP"
stop
echo "==> STOP SUCCESSFUL ..."
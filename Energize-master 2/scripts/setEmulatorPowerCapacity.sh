#!/bin/bash
TOKEN=`cat ~/.emulator_console_auth_token`
expect << EOF
spawn telnet localhost 5554
expect "OK"
send -- "auth $TOKEN\r"
expect "OK"
send -- "power capacity $1\r"
expect "OK"
send -- "exit\r"
EOF

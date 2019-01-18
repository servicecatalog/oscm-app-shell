#!/bin/bash

. /opt/scripts/common.sh
entry_log

date >> /tmp/empty.log

echo "$MY_SCRIPT_VM_NAME" >> /tmp/empty.log
printenv 2>&1 >> /tmp/empty.log
echo "$OPERATION" >> /tmp/empty.log

default_success

#uptime
#ps ax |grep myvm |grep -v grep
echo "END_OF_SCRIPT"
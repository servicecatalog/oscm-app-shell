#!/bin/bash

THIS=operation; export THIS

. /opt/scripts/common.sh
entry_log

default() {
  default_success
}

log $OPERATION

case $OPERATION in
  VM_START) VM_Start 	$MY_SCRIPT_VM_NAME ;;
  VM_STOP)  VM_Stop	$MY_SCRIPT_VM_NAME ;;
  *) default ;;
esac


echo "END_OF_SCRIPT"


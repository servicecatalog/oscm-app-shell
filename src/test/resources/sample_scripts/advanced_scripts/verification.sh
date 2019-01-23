#!/bin/bash

THIS=verification; export THIS

. /opt/scripts/common.sh
entry_log

VM_Exists  $MY_SCRIPT_VM_NAME

if [ $? == 1 ]
then 
  script_failure $THIS "VM $MY_SCRIPT_VM_NAME already exists"
  echo "END_OF_SCRIPT"
  exit 1
fi

default_success

echo "END_OF_SCRIPT"
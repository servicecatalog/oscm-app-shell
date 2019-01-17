#!/bin/bash

THIS=provisioning; export THIS

. /opt/scripts/common.sh
entry_log

VM_Exists $MY_SCRIPT_VM_NAME
if [ $? == 1 ] ; then 
  script_failure $0 "VM $MY_SCRIPT_VM_NAME already exists"
  echo "END_OF_SCRIPT"
  exit 1
fi

VM_Create $MY_SCRIPT_VM_NAME
if [ $? == 1 ] ; then
  script_failure $0 "Failed to create VM $MY_SCRIPT_VM_NAME"
  echo "END_OF_SCRIPT"
  exit 1
fi

VM_Start $MY_SCRIPT_VM_NAME
if [ $? == 1 ] ; then
  script_failure $0 "Failed to start VM $MY_SCRIPT_VM_NAME"
  echo "END_OF_SCRIPT"
  exit 1
fi

ACCESS_INFO="The VM is accessible at the following address: '$VM_URL'"
export ACCESS_INFO

provisioning_success $VM_URL

echo "END_OF_SCRIPT"

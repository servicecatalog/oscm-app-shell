#!/bin/bash

THIS=deprovisioning; export THIS

. /opt/scripts/common.sh
entry_log

VM_Exists $MY_SCRIPT_VM_NAME
if [ $? == 0 ] ; then
  script_failure $THIS "VM $MY_SCRIPT_VM_NAME does not exist"
  echo "END_OF_SCRIPT"
  exit 1
fi

VM_Stop $MY_SCRIPT_VM_NAME
if [ $? == 1 ] ; then
  script_failure $THIS "Failed to stop VM $MY_SCRIPT_VM_NAME"
  echo "END_OF_SCRIPT"
  exit 1
fi

VM_Delete $MY_SCRIPT_VM_NAME
if [ $? == 1 ] ; then
  script_failure $THIS "Failed to delete VM $MY_SCRIPT_VM_NAME"
  echo "END_OF_SCRIPT"
  exit 1
fi

deprovisioning_success

echo "END_OF_SCRIPT"
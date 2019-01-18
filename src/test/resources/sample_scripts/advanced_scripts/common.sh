#!/bin/bash

# Variables
OUT=/tmp/$THIS.log
PREFIX="myvm"
DIR=/tmp

MY_SCRIPT_VM_NAME="VM01"
export MY_SCRIPT_VM_NAME
VM_URL="https://example.com"
export VM_URL

. /opt/scripts/responses.sh

# Define your functions here
get_script_dir() {
  CURRENT_SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
  export CURRENT_SCRIPT_PATH
}

log() {
  echo "` date +'%Y%m%d %H:%M:%S'` $*" >> $OUT
}

entry_log() {
  get_script_dir
  log "Running script ${0} from directory: $CURRENT_SCRIPT_PATH."
}

VM_Create() { 
  log "Call of VM_Create $*"

  VM_FILE=$PREFIX.$1
  VM_PATH=$DIR/$VM_FILE

  rm -f $VM_PATH
  cp /usr/bin/sleep  $VM_PATH
}

VM_Delete() {
  log "Call of VM_Delete $*"

  VM_FILE=$PREFIX.$1
  VM_PATH=$DIR/$VM_FILE

  rm -f  $VM_PATH
}

VM_Start() {
  log "Call of VM_Start $*"

  VM_FILE=$PREFIX.$1
  VM_PATH=$DIR/$VM_FILE
  (PATH=$DIR; $VM_FILE  10000 &)
}


VM_Stop() {
  log "Call of VM_Stop $*"

  VM_FILE=$PREFIX.$1
  VM_PATH=$DIR/$VM_FILE

  MYPID=`ps ax|grep "$VM_FILE " | grep -v grep | awk '{print $1}'`
  kill $MYPID >> $OUT 2>&1
}

VM_IsRunning() {
  log "Call of VM_IsRunning $*"

  VM_FILE=$PREFIX.$1
  VM_PATH=$DIR/$VM_FILE

  MYPID=`ps ax|grep "$VM_FILE " | grep -v grep | awk '{print $1}'`
  if [ "$MYPID" == "" ]
    then return 0
    else return 1
  fi
}

VM_Exists() {
  log "Call of VM_Exists $*"

  VM_FILE=$PREFIX.$1
  VM_PATH=$DIR/$VM_FILE

  if [ -f $VM_PATH ]
    then return 1
    else return 0
  fi
}
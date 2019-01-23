#!/bin/bash

script_failure() {
  FAILURE_PROPER_CALL="Script failure should contain the following format: script_failure <FILENAME> <MESSAGE>"
  if [ $# -eq 0 ] ; then
    log "No arguments supplied to the script_failure method!\n"$FAILURE_PROPER_CALL
    echo "END_OF_SCRIPT"
    exit 1
  elif [ -z "$1" ] || [ -z "$2" ] ; then
    log "File name or message not supplied to the script_failure method!\n"$FAILURE_PROPER_CALL
    echo "END_OF_SCRIPT"
    exit 1
  else
    FILE=$1
    ERROR=${@:2}
    log $ERROR
    echo '{
  "status": "error",
  "message": "Failed to execute '$FILE'. '$ERROR'"
}'
  fi
}

default_success() {
  echo '{
  "status": "ok",
  "message": "default success message"
}'
}

provisioning_success() {
  PROVISIONING_PROPER_CALL="Provisioning success should contain the following format: provisining_success <VM'S_URL>"
  if [ -z $1 ] ; then
    log "URL of the VM not supplied! Check provisioning script...\n"$PROVISIONING_PROPER_CALL
    echo "END_OF_SCRIPT"
    exit 1
  else
    VM_URL=$1
    echo '{
  "status": "ok",
  "message": "VM provisioned successfully.",
  "data": {
    "access info": "'$ACCESS_INFO'"
  }
}'
  fi
}

deprovisioning_success() {
  echo '{
  "status": "ok",
  "message": "VM deprovisioned successfully."
}'
}

status_success() {
  STATUS_PROPER_CALL="Status success should contain the following format: status_success <OUTPUT>"
  if [ $# -eq 0 ] ; then
    log "No output supplied to the status_success method"
    echo "END_OF_SCRIPT"
    exit 1
  else
    OUTPUT=$*
    echo '{
  "status": "ok",
  "message": "Status checked successfully.",
  "data": {
    "access info": "'$ACCESS_INFO'",
    "output": "'$OUTPUT'"
  }
}'
  fi
}
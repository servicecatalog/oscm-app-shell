#!/bin/bash

THIS=status; export THIS

. /opt/scripts/common.sh
entry_log

date >> $OUT

status_success "sample output"

echo "END_OF_SCRIPT"


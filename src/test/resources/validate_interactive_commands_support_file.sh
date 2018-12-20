#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# THIS FILE IS AN EXAMPLE SCRIPT FILE THAT INVOKES INTERACTIVE COMMANDS.      #
# I UPLOADED THIS TO REPOSITORY, BECAUSE IN CASE OF CHANGE OF REQUIREMENTS    #
# IN INTERACTIVE COMMAND VERIFICATION, THIS FILE WILL BE HELPFUL.             #
# I USE IT WITH ATOM TEXT EDITOR TO EASILY VERIFY IF EACH REGEXP IS CORRECT.  #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Check if read command is used at the beggining of any line.
# Regexp: ^\s*read\s
read VAR_B

VAR=5
if [ VAR_B==5 ] ; then
  read VAR
  echo "Test read "
  some_command | xargs read
fi

# Check if an --interactive flag is passed to some command.
# Regexp: ^((?![#]).)*[a-zA-Z0-9$,.\s|]--interactive\s?
some_command --interactive

something_happening_here # comment with --interactive flag

if [ VAR_B==3 ] ; then
  echo "Something" | command --interactive $ARG1 $ARG2 | command2
if

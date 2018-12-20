#!/usr/bin/bash

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# THIS FILE IS AN EXAMPLE SCRIPT FILE THAT RETURNS JSON DATA IN MANY FORMATS  #
# I UPLOADED THIS TO REPOSITORY, BECAUSE IN CASE OF CHANGE  OF REQUIREMENTS   #
# IN FORMAT OF THE JSON ANSWER, THIS FILE WILL HELP A LOT.                    #
# I USE IT WITH ATOM TEXT EDITOR TO EASILY VERIFY IF EACH REGEXP IS CORRECT.  #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

set -e

# Declare variables
STATUS="uninitialized"
MESSAGE="uninitialized"
DATA="uninitialized"
ID="100"

function set_default_message {
    echo "Setting default message..."
    MESSAGE="default message"
}

function set_default_data {
    echo "Setting default data..."
    DATA='{
    "information1": "default information",
    "information2": "other information"
    }'
    echo $DATA
    sleep 5
}

function set_status {
    if [ -z $1 ] ; then
        echo "Argument not found!"
        STATUS="UNDEFINED"
    else
        echo "Setting status to $1..."
        STATUS="$1"
    fi
    set_default_data
    set_default_message
}

# Call function with argument - initialize variable in correct format
set_status ok


# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# SCENARIOS WITHOUT DATA INITIALIZED                                          #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Regexp: echo\s['"][{]\s*"status":\s?"[$]?[a-zA-Z0-9]+",\s+"message":\s?"[a-zA-Z0-9$,.\s]+"\s*[}]['"]
echo '{
 "status": "$STATUS",
 "message": "$MESSAGE"
}'
echo '{"status": "$STATUS",
       "message": "Test $MESSAGE words and $VARS."}'
echo '{ "status": "$STATUS",
        "message": "$MESSAGE" }'
echo '{ "status":"$STATUS",
        "message":"$MESSAGE" }'

# Regexp: echo\s['"][{]\s*\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s+\\"message\\":\s?\\"[a-zA-Z0-9$,.\s]+\\"\s*[}]['"]
echo "{
 \"status\": \"$STATUS\",
 \"message\": \"$MESSAGE\"
}"
echo "{ \"status\": \"$STATUS\",
        \"message\": \"Test $MESSAGE words and $VARS.\" }"
echo "{\"status\":\"$STATUS\",
       \"message\":\"This is a mesasge\"}"
echo "{\"status\": \"$STATUS\",
       \"message\": \"$MESSAGE\"}"

# Regexp: printf\s['"][{]\s*(\\n)?\s?\\"status\\":\s?\\"%s\\",\s?\\n\s?\\"message\\":\s?\\"[a-zA-Z0-9$\s]*%s[a-zA-Z0-9$,.\s]*\\"\s?(\\n)?\s*[}]['"]
printf '{\n \"status\": \"%s\",\n \"message\": \"%s\"\n}' "$STATUS" "$MESSAGE"
printf '{\n\"status\":\"%s\",\n\"message\":\"Test %s words and vars.\"\n}' "$STATUS" "$MESSAGE"
printf '{  \n \"status\": \"%s\", \n \"message\": \"%s\" \n  }' "$STATUS" "$MESSAGE"
printf '{\"status\": \"%s\", \n \"message\": \"%s\"}' "$STATUS" "$MESSAGE"

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s?"status":\s?"[$]?[a-zA-Z0-9]+",\s?\\n\s?"message":\s?"\s*[a-zA-Z0-9$,.\s]+"\s?(\\n)?\s*[}]['"]
echo -e '{\n "status": "$STATUS",\n "message": " $MESSAGE"\n}'
echo -e '{"status": "$STATUS",\n "message": "Test $MESSAGE words and $VARS."}'
echo -e '{ \n "status": "$STATUS", \n "message": "$MESSAGE"\n }'
echo -e '{\n "status":"$STATUS",\n "message":"$MESSAGE"\n}'

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s?\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s?(\\n)?\s?\\"message\\":\s?\\"\s*[a-zA-Z0-9$,.\s]+\\"\s?(\\n)?\s*[}]['"]
echo -e "{\n \"status\": \"$STATUS\",\n \"message\": \"$MESSAGE\"\n}"
echo -e "{\"status\":\"$STATUS\",\n \"message\":\" $MESSAGE\"}"
echo -e "{ \n \"status\": \"$STATUS\",\n \"message\": \"Test $MESSAGE words and $VARS.\" \n }"
echo -e "{\n \"status\":\"$STATUS\", \n \"message\":\"$MESSAGE\"\n }"

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# SCENARIOS WITH DATA INITIALIZED                                             #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Regexp: echo\s['"][{]\s*"status":\s?"[$]?[a-zA-Z0-9]+",\s+"message":\s?"\s*[a-zA-Z0-9$,.\s]+",\s+"data":\s*((\s?[$]?[a-zA-Z0-9\s]+)|([{]\s*("[a-zA-Z0-9]+":\s?"[a-zA-Z0-9$,.\s]+",?\s*)+[}]))\s*[}]['"]
echo '{
 "status": "$STATUS",
 "message": "Test $MESSAGE words and $VARS.",
 "data": {
   "id": "Test $VAR1 and $VAR2.",
   "type": "Test $VAR1 and $VAR2."
 }
}'
echo '{
 "status": "$STATUS",
 "message": "Test $MESSAGE words and $VARS.",
 "data": {
   "id": "Test $VAR1 and $VAR2.",
   "type": "Test $VAR1 and $VAR2."
 }
}'
echo '{
 "status": "$STATUS",
 "message": "$MESSAGE",
 "data": {
   "id": "$ID",
   "menuitem": "item01, item02, item03",
   "other": "something",
   "different": "anything"
 }
}'
echo '{"status": "$STATUS",
      "message": "$MESSAGE",
      "data": $DATA}'

# Regexp: echo\s['"][{]\s*\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s+\\"message\\":\s?\\"\s*[a-zA-Z0-9$,.\s]+\\",\s+\\"data\\":\s*((\s?[$]?[a-zA-Z0-9\s]+)|([{]\s*(\\"[a-zA-Z0-9]+\\":\s?\\"[a-zA-Z0-9$,.\s]+\\",?\s*)+[}]))\s*[}]['"]
echo "{
 \"status\": \"$STATUS\",
 \"message\": \"Test $MESSAGE words and $VARS.\",
 \"data\": {
   \"id\": \"Test $MESSAGE words and $VARS.\",
   \"type\": \"server1, server2.\"
 }
}"
echo "{
 \"status\": \"$STATUS\",
 \"message\": \" Test $MESSAGE words and $VARS. \",
 \"data\": $DATA
}"

# Regexp: printf\s['"][{]\s*(\\n)?\s*\\"status\\":\s?\\"\s?%s\s?\\",\\n\s*\\"message\\":\s?\\"[a-zA-Z0-9$\s]*%s[a-zA-Z0-9$,.\s]*\\"\\n\s*\\"data\\":\s?\s?%s\s?(\\n)?\s*[}]['"]
printf '{\n   \"status\": \"%s\",\n \"message\": \"%s\"\n \"data\": %s\n}' "$STATUS" "$MESSAGE" "$DATA"
printf '{ \n \"status\": \"%s\",\n   \"message\": \"Test %s words and vars.\"\n \"data\": %s\n }' "$STATUS" "$MESSAGE" "$DATA"
printf '{\n \"status\":\"%s\",\n\"message\":\"%s\"\n    \"data\":%s\n    }' "$STATUS" "$MESSAGE" "$DATA"
printf '{\"status\":\"%s\",\n   \"message\":\"%s\"\n   \"data\":%s}' "$STATUS" "$MESSAGE" "$DATA"

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s*"status":\s?"[$]?[a-zA-Z0-9]+",\s?\\n\s*"message":\s?"\s*[a-zA-Z0-9$,.\s]+",\s?\\n\s*"data":\s?"[$]?[a-zA-Z0-9]+"\s?(\\n)?\s*[}]['"]
echo -e '{\n "status": "$STATUS",\n "message": "  $MESSAGE,  ",\n    "data": "$DATA"\n}'
echo -e '{"status": "$STATUS",\n "message":" Test $MESSAGE words and $VARS. ",\n "data": "$DATA"}'
echo -e '{\n   "status":"$STATUS",\n   "message":"$MESSAGE",\n "data":"$DATA"   }'
echo -e '{\n    "status": "$STATUS",\n   "message": "$MESSAGE Test $MESSAGE",\n   "data": "$DATA"\n}'

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s*\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s*(\\n)?\s?\\"message\\":\s?\\"\s*[a-zA-Z0-9$,.\s]+\\",\s*(\\n)?\s?\\"data\\":\s?[$]?[a-zA-Z0-9\s]+\s*(\\n)?\s*[}]['"]
echo -e "{   \n  \"status\": \"$STATUS\",\n \"message\": \"$MESSAGE\", \"data\": $DATA \n}"
echo -e "{\n \"status\": \"$STATUS\", \n\"message\": \"Test $MESSAGE words and $VARS.\",  \"data\": $DATA\n}"
echo -e "{\n \"status\":\"$STATUS\",\n\"message\":\"$MESSAGE  \",  \"data\": $DATA}"
echo -e "{\"status\": \"$STATUS\",  \n \"message\": \"  $MESSAGE\",  \"data\": $DATA  }"

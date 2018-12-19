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

# Regexp: echo\s['"][{]\s*"status":\s?"[$]?[a-zA-Z0-9]+",\s+"message":\s?"[a-zA-Z0-9$\s]+"\s*[}]['"]
echo '{
 "status": "$STATUS",
 "message": "$MESSAGE"
}'
echo '{"status": "$STATUS",
       "message": "fara $MESSAGE test $dolar $variables ff"}'
echo '{ "status": "$STATUS",
        "message": "$MESSAGE" }'
echo '{ "status":"$STATUS",
        "message":"$MESSAGE" }'

# Regexp: echo\s['"][{]\s*\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s+\\"message\\":\s?\\"[a-zA-Z0-9$\s]+\\"\s*[}]['"]
echo "{
 \"status\": \"$STATUS\",
 \"message\": \"$MESSAGE\"
}"
echo "{ \"status\": \"$STATUS\",
        \"message\": \"asd  fd $MESSAGE fd fd $OTHER \" }"
echo "{\"status\":\"$STATUS\",
       \"message\":\"fdsf farafara\"}"
echo "{\"status\": \"$STATUS\",
       \"message\": \"$MESSAGE\"}"

# Regexp: printf\s['"][{]\s*(\\n)?\s?\\"status\\":\s?\\"%s\\",\s?\\n\s?\\"message\\":\s?\\"[a-zA-Z0-9$\s]*%s[a-zA-Z0-9$\s]*\\"\s?(\\n)?\s*[}]['"]
printf '{\n \"status\": \"%s\",\n \"message\": \"%s\"\n}' "$STATUS" "$MESSAGE"
printf '{\n\"status\":\"%s\",\n\"message\":\"fara r %s rafafar\"\n}' "$STATUS" "$MESSAGE"
printf '{  \n \"status\": \"%s\", \n \"message\": \"%s\" \n  }' "$STATUS" "$MESSAGE"
printf '{\"status\": \"%s\", \n \"message\": \"%s\"}' "$STATUS" "$MESSAGE"

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s?"status":\s?"[$]?[a-zA-Z0-9]+",\s?\\n\s?"message":\s?"\s*[a-zA-Z0-9$\s]+"\s?(\\n)?\s*[}]['"]
echo -e '{\n "status": "$STATUS",\n "message": " $MESSAGE"\n}'
echo -e '{"status": "$STATUS",\n "message": "fara r $MESSAGE rafaaf"}'
echo -e '{ \n "status": "$STATUS", \n "message": "$MESSAGE"\n }'
echo -e '{\n "status":"$STATUS",\n "message":"$MESSAGE"\n}'

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s?\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s?(\\n)?\s?\\"message\\":\s?\\"\s*[a-zA-Z0-9$\s]+\\"\s?(\\n)?\s*[}]['"]
echo -e "{\n \"status\": \"$STATUS\",\n \"message\": \"$MES SAGE\"\n}"
echo -e "{\"status\":\"$STATUS\",\n \"message\":\" $MESSAGE\"}"
echo -e "{ \n \"status\": \"$STATUS\",\n \"message\": \"$MESS AGE\" \n }"
echo -e "{\n \"status\":\"$STATUS\", \n \"message\":\"$MESSAGE\"\n }"

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# SCENARIOS WITH DATA INITIALIZED                                             #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Regexp: echo\s['"][{]\s*"status":\s?"[$]?[a-zA-Z0-9]+",\s+"message":\s?"\s*[a-zA-Z0-9$\s]+",\s+"data":\s*((\s?[$]?[a-zA-Z0-9\s]+)|([{]\s*("[a-zA-Z0-9]+":\s?"[a-zA-Z0-9$\s]+",?\s*)+[}]))\s*[}]['"]
echo '{
 "status": "$STATUS",
 "message": "fara $MESSAGE ra fa f",
 "data": {
   "id": "fara $ID rafaf",
   "type": "server dfsfd $F$f$F$F$F"
 }
}'

echo '{
 "status": "$STATUS",
 "message": " $ME SSAGE",
 "data": {
   "id": "$TEST I D",
   "type": "ser ver"
 }
}'

echo '{
 "status": "$STATUS",
 "message": "$MESSAGE",
 "data": {
   "id": "$ID",
   "menuitem": "it em01",
   "other": "something",
   "different": "anything"
 }
}'

echo '{
 "status": "$STATUS",
 "message": "$MESSAGE",
 "data": $DATA
}'

echo '{
 "status": "$STATUS",
 "message": "$MESSAGE",
 "data": {
   "id": "$ID"
 }
}'

# Regexp: echo\s['"][{]\s*\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s+\\"message\\":\s?\\"\s*[a-zA-Z0-9$\s]+\\",\s+\\"data\\":\s*((\s?[$]?[a-zA-Z0-9\s]+)|([{]\s*(\\"[a-zA-Z0-9]+\\":\s?\\"[a-zA-Z0-9$\s]+\\",?\s*)+[}]))\s*[}]['"]
echo "{
 \"status\": \"$STATUS\",
 \"message\": \"$M ESSAGE\",
 \"data\": {
   \"id\": \"$TEST opt $TEST2 FID\",
   \"type\": \"se rver\"
   \"type\": \"ser ver\"
 }
}"

echo "{
 \"status\": \"$STATUS\",
 \"message\": \" $M  ES $SAGE \",
 \"data\": $DATA
}"

# Regexp: printf\s['"][{]\s*(\\n)?\s*\\"status\\":\s?\\"\s?%s\s?\\",\\n\s*\\"message\\":\s?\\"[a-zA-Z0-9$\s]*%s[a-zA-Z0-9$\s]*\\"\\n\s*\\"data\\":\s?\s?%s\s?(\\n)?\s*[}]['"]
printf '{\n   \"status\": \"%s\",\n \"message\": \"%s\"\n \"data\": %s\n}' "$STATUS" "$MESSAGE" "$DATA"
echo ""
printf '{ \n \"status\": \"%s\",\n   \"message\": \"fafa  ra %s raf f $dollar f \"\n \"data\": %s\n }' "$STATUS" "$MESSAGE" "$DATA"
echo ""
printf '{\n \"status\":\"%s\",\n\"message\":\"%s\"\n    \"data\":%s\n    }' "$STATUS" "$MESSAGE" "$DATA"
echo ""
printf '{\"status\":\"%s\",\n   \"message\":\"%s\"\n   \"data\":%s}' "$STATUS" "$MESSAGE" "$DATA"
echo ""

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s*"status":\s?"[$]?[a-zA-Z0-9]+",\s?\\n\s*"message":\s?"\s*[a-zA-Z0-9$\s]+",\s?\\n\s*"data":\s?"[$]?[a-zA-Z0-9]+"\s?(\\n)?\s*[}]['"]
echo -e '{\n "status": "$STATUS",\n "message": " $MESSAGE",\n    "data": "$DATA"\n}'
echo -e '{"status": "$STATUS",\n "message":" $MES test $SAGE $dollar test",\n "data": "$DATA"}'
echo -e '{\n   "status":"$STATUS",\n   "message":"$MESS AGE",\n "data":"$DATA"   }'
echo -e '{\n    "status": "$STATUS",\n   "message": "$MESS AGE",\n   "data": "$DATA"\n}'

# Regexp: echo\s[-]e\s['"][{]\s*(\\n)?\s*\\"status\\":\s?\\"[$]?[a-zA-Z0-9]+\\",\s*(\\n)?\s?\\"message\\":\s?\\"\s*[a-zA-Z0-9$\s]+\\",\s*(\\n)?\s?\\"data\\":\s?[$]?[a-zA-Z0-9\s]+\s*(\\n)?\s*[}]['"]
echo -e "{   \n  \"status\": \"$STATUS\",\n \"message\": \"$MESSAGE\", \"data\": $DATA \n}"
echo -e "{\n \"status\": \"$STATUS\", \n\"message\": \"$MESSA test $dollar TESTING\",  \"data\": $DATA\n}"
echo -e "{\n \"status\":\"$STATUS\",\n\"message\":\"$MESSAGE \",  \"data\": $DATA}"
echo -e "{\"status\": \"$STATUS\",  \n \"message\": \" $MESSAGE\",  \"data\": $DATA  }"

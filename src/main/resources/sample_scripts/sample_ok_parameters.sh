#!/bin/bash

PARAM1="some value"
PARAM2="some value"
PARAM3="some value"

echo '
	{"status":"ok",
	 "message":"Script execution is successful",
   "paramters": [
       {
         "key": "PARAM_TO_SAVE1",
         "value": "'$PARAM1'"
       },
       {
         "key": "PARAM_TO_SAVE2",
         "value": "'$PARAM2'"
       },
       {
         "key": "PARAM_TO_SAVE3",
         "value": "'$PARAM3'"
       }
   ]}'
 
echo "END_OF_SCRIPT";
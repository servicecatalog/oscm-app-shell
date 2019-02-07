#!/bin/bash

echo '
	{"status":"ok",
	 "message":"Script execution is successful",
	 "usageData": [
         {
           "eventId": "EVENT_DISK_GIGABYTE_HOURS",
           "multiplier": 100
         },
         {
           "eventId": "EVENT_CPU_HOURS",
           "multiplier": 300
         },
         {
           "eventId": "EVENT_RAM_MEGABYTE_HOURS",
           "multiplier": 250
         }
     ]}'
 
echo "END_OF_SCRIPT";
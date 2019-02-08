[![Build status](https://travis-ci.org/servicecatalog/oscm-app-shell.svg?branch=master)](https://travis-ci.org/servicecatalog/oscm-app-shell)
[![codecov](https://codecov.io/gh/servicecatalog/oscm-app-shell/branch/master/graph/badge.svg)](https://codecov.io/gh/servicecatalog/oscm-app-shell)

# What is it?
The OSCM shell controller is a shell based framework adapter that can be used to integrate OSCM with any type of cloud. It is flexibly usable, covering any cases of instance and application provisioning. It allows for collecting status information that can be displayed in the subscription details in the marketplace portal. The OSCM shell adapter supports for gathering event data of cloud service consumption. This allows the supplier to charge back the usage costs with respective event price models.

# Quick Start
This quick start guide is intended to help you getting started with OSCM shell controller.

## Deploying Scripts 
The oscm shell controller is based on shell script execution. The script files are expected in a shared folder on the docker host and mounted in the oscm-app container. Each script has to be defined with a [technical service parameter](https://github.com/servicecatalog/oscm-app-shell/blob/master/src/main/resources/TechnicalService.xml) in order to connect it with the controller. Check-out [this description](https://github.com/servicecatalog/oscm-dockerbuild#import-local-shell-scripts-for-oscm-app-shell-component) for more details about how to mount the scripts in the container.

## Logging
The controller provides for logging detailed information of scripts execution. The logfile is located inside oscm-app container, and can be inspected by checking `/opt/apache-tomee/logs/app-shell.log` file.

## Script Format and Execution Result
The provided shell scripts need to fulfill a few rules:

1. The script response has to contain a valid JSON object followed by "**END_OF_SCRIPT**" string value
2. It must result with **exactly one** JSON object
3. The JSON object must consist of following fields:
   * status - **required**, can contain only "**ok**" or "**error**",
   * message - **required**, is supposed to contain useful information related to the script execution and presented to the user
   * data - **optional**, contains result data which is processed after the script is executed. Valid sub fields: ```accessInfo``` and ```output```, both supporting HTML and plain text. ```output``` is expected in response of status script. 
   * usageData - **optional**, expected in response of usage data script for gathering billable events, contains array of events composed of ```eventId``` and ```multiplier``` field according the technical service definition.
   
Simple JSON response example:
```json   
   {
     "status": "ok",
     "message": "Script executed successfully",
     "data": {
       "output": "Custom output for the status tab",
       "accessInfo": "<a href=\"http://accessInfo.url\">http://accessInfo.url </a>"
     },
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
     ]
   }
```

More information on possible script usage and expected format can be found in [these examples](https://github.com/servicecatalog/oscm-app-shell/tree/master/src/main/resources/sample_scripts).

Enjoy it!

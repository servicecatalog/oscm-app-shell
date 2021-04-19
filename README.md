[![Build status](https://travis-ci.com/servicecatalog/oscm-app-shell.svg?branch=master)](https://travis-ci.com/servicecatalog/oscm-app-shell)
[![codecov](https://codecov.io/gh/servicecatalog/oscm-app-shell/branch/master/graph/badge.svg)](https://codecov.io/gh/servicecatalog/oscm-app-shell)

# What is it?
The OSCM shell controller is a script based framework adapter for integrating OSCM with any type of cloud. 

It is flexibly usable, covering any cases of instance and application provisioning. This controller allows for modifying, provisioning and deprovisioning of cloud applications and instances, executing service operations, assigning and deassigning users and querying detailed status information. The latter is accessible for privileged users in the subscription details view in the marketplace portal. 

Furthermore, the OSCM shell controller supports for gathering event data of cloud service consumption. This enables the supplier to charge back the usage costs by using respective event price models.

# Quick Start
This quick start guide is intended to help you getting started with OSCM shell controller. This controller is deployed together with OSCM. Instructions for installing OSCM can be found in the [readme of the oscm-deployer](https://hub.docker.com/r/servicecatalog/oscm-deployer).

## Deploying Scripts 
The OSCM shell controller is based on shell script execution. The script files are expected in a shared folder on the docker host and mounted in the oscm-app container. Each script has to be defined with a [technical service parameter](https://github.com/servicecatalog/oscm-app-shell/blob/master/src/main/resources/TechnicalService.xml#L35-L65) in order to connect it with the controller. 
Check-out [this description](https://github.com/servicecatalog/oscm-dockerbuild#import-scripts-for-the-shell-controller-oscm-app-shell) for more details about how to mount the scripts into the oscm-app container.

## Logging
The controller provides for logging detailed information of scripts execution. The logfile is located inside oscm-app container, and can be inspected by checking `/opt/apache-tomee/logs/app-shell.log` file.

## Script and Response Format
The required format is simple and semi-structured based on JSON. Following rules apply:

1. The script response has to contain a valid JSON object followed by "**END_OF_SCRIPT**" string value
2. It must result with **exactly one** JSON object
3. The JSON object must consist of following fields:
   * status - **required**, can contain only "**ok**" or "**error**",
   * message - **required**, is supposed to contain useful information related to the script execution and presented to the user
   * data - **optional**, contains result data which is processed after the script is executed. Valid sub fields: ```accessInfo``` and ```output```, both supporting HTML and plain text. ```output``` is expected in response of status script. 
   * usageData - **optional**, expected in response of usage data script for gathering billable events, contains array of events composed of ```eventId``` and ```multiplier``` field according the technical service definition.
   
#### Example: ####
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

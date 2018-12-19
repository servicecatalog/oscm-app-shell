[![Build status](https://travis-ci.org/servicecatalog/oscm-app-shell.svg?branch=master)](https://travis-ci.org/servicecatalog/oscm-app-shell)
[![codecov](https://codecov.io/gh/servicecatalog/oscm-app-shell/branch/master/graph/badge.svg)](https://codecov.io/gh/servicecatalog/oscm-app-shell)

# Quick start using oscm-app-shell controller
This is a quick start guide intended to help you start using app-shell controller with OSCM

## Deploying scripts into container
The oscm-app-shell controller is working based on shell scripts execution. Those scripts are defined within the [technical service](https://github.com/servicecatalog/oscm-app-shell/blob/master/src/main/resources/TechnicalService.xml) parameters either accessible localy or externaly with url. To get more details about how to mount the script so that they can be used by shell-controller, please refer to the part of oscm-dockerbuild repository [description](https://github.com/servicecatalog/oscm-dockerbuild#import-local-shell-scripts-for-oscm-app-shell-component) related to that topic.

## Logging
In case of more details regarding scripts execution are needed, the controller provides it with the logfile. The logfile is located inside oscm-app container, and can be inspected by checking `/opt/apache-tomee/logs/app-shell.log` file.

## Script execution result
So that shell script used with app-shell related service is executed properly, it must fulfill a few rules which must be respected:

1. It must result with valid JSON object followed by "**END_OF_SCRIPT**" string value.
2. It must result with **exactly one** JSON object.
3. JSON result must consists of following fields:
   * status - **required**, must contain only "**ok**" or "**error**",
   * message - **required**, should contain information related to the script execution which is delivered to the user,
   * data - **optional**, should contain any data which can be used after the script is executed e.g access info, HTML data for status script.
   
   Plese refer to scripts examples directory https://github.com/servicecatalog/oscm-app-shell/tree/master/src/test/resources/sample_scripts to get familiar with both valid and invalid script samples.

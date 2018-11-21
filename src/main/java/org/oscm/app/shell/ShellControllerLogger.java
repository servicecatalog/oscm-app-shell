package org.oscm.app.shell;

import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Nov 21, 2018
 *
 *******************************************************************************/
import java.util.LinkedHashMap;
import java.util.Map;


public class ShellControllerLogger {

    private static final Logger LOG = LoggerFactory.getLogger(ShellController.class);

    public void safeOutputFromScript(Configuration config, String scriptKey, String output){
        LOG.warn("Output from script " + scriptKey);
        LOG.info(output);

    }

    public void safeScriptConfiguration(Configuration configuration, String scriptName, String script){
        LOG.info("***********************configuration information*****************************");
        LOG.info("<scriptType>:" + "<"+ scriptName + ">");
        LOG.info("<scriptPath>: " + getScriptPath(script, scriptName));
        if(configuration.getRequestingUser()!=null){
            LOG.info("<requestingUser>: " +"<"
                    +configuration.getRequestingUser().getFirstName()+":"
                    +configuration.getRequestingUser().getLastName()+">");
        }
        else
            LOG.info("<requestingUser>: <>");

        LOG.info("<organizationId>:" + "<" + configuration.getOrganizationId()+">");
        LOG.info("<organizationName>:" + "<" + configuration.getOrganizationName()+">");
        LOG.info("<subscriptionId>:" + "<" + configuration.getSubscriptionId()+">");

    }

    private String getScriptPath(String script, String scriptName){

        String fileName = "";
        Map<String, String> map = new LinkedHashMap<>();
        for(String keyValue : script.split("\n")) {
            String[] pairs = keyValue.split("=", 2);
            map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }
        for(String key: map.keySet()) {
            if (key.startsWith(scriptName)) {
                fileName= map.get(key);
            }

        }
        return fileName;
    }

    public void safeScriptCommand(ShellCommand command){
        LOG.info("********************* script command *****************");
        LOG.info(command.getCommand());
    }

    public void consumeShellOutput(ShellStatus status){
        LOG.info("script status : " +status);
    }
}

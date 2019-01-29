/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Nov 21, 2018
 *
 *******************************************************************************/
package org.oscm.app.shell;

import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.api.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScriptLogger {

    private static final Logger LOG = LoggerFactory.getLogger(ShellController.class);

    public static void logOutputFromScript(String scriptKey, String output) {
        LOG.debug("*********************** script output ***********************");
        LOG.debug("Output from script " + scriptKey);
        LOG.debug(output);
    }

    public static void logScriptConfiguration(Configuration configuration,
                                              String scriptType, String script) {

        LOG.info("Running: " + scriptType + " located in: " + getScriptPath(script, scriptType));
        LOG.debug("*********************** configuration information ***********************");
        LOG.debug("<scriptType>:" + "<" + scriptType + ">");
        LOG.debug("<scriptName>: " + getScriptPath(script, scriptType));
        if (configuration.getRequestingUser() != null) {
            LOG.debug("<requestingUser>: " + "<"
                    + configuration.getRequestingUser()
                    .getFirstName() + ":"
                    + configuration.getRequestingUser()
                    .getLastName() + ">");
        } else
            LOG.debug("<requestingUser>: <>");

        LOG.debug("<organizationId>:" + "<" + configuration
                .getOrganizationId() + ">");
        LOG.debug("<organizationName>:" + "<" + configuration
                .getOrganizationName() + ">");
        LOG.debug("<subscriptionId>:" + "<" + configuration
                .getSubscriptionId() + ">");
    }

    private static String getScriptPath(String script, String scriptName) {
        String fileName = "";
        Map<String, String> map = new LinkedHashMap<>();
        for (String keyValue : script.split("\n")) {
            String[] pairs = keyValue.split("=", 2);
            map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }
        for (String key : map.keySet()) {
            if (key.startsWith(scriptName)) {
                fileName = map.get(key);
            }

        }
        return fileName;
    }

    public static void logScriptCommand(ShellCommand command) {
        LOG.debug("*********************** script command ***********************");
        LOG.debug(command.getCommand());
    }

}

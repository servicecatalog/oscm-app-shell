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

    private static final Logger LOG = LoggerFactory
            .getLogger(ShellController.class);

    public static void logOutputFromScript(Configuration config,
                                           String scriptKey, String output) {
        LOG.warn("Output from script " + scriptKey);
        LOG.info(output);
    }

    public static void logScriptConfiguration(Configuration configuration,
                                              String scriptName, String script) {
        LOG.info("***********************configuration information*****************************");
        LOG.info("<scriptType>:" + "<" + scriptName + ">");
        LOG.info("<scriptName>: " + getScriptPath(script, scriptName));
        if (configuration.getRequestingUser() != null) {
            LOG.info("<requestingUser>: " + "<"
                    + configuration.getRequestingUser()
                    .getFirstName() + ":"
                    + configuration.getRequestingUser()
                    .getLastName() + ">");
        } else
            LOG.info("<requestingUser>: <>");

        LOG.info("<organizationId>:" + "<" + configuration
                .getOrganizationId() + ">");
        LOG.info("<organizationName>:" + "<" + configuration
                .getOrganizationName() + ">");
        LOG.info("<subscriptionId>:" + "<" + configuration
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
        LOG.info(
                "********************* script command *****************");
        LOG.info(command.getCommand());
    }

}

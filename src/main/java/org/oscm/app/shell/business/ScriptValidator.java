/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-11-14
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class ScriptValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptValidator.class);

    public void validate(Configuration configuration, ConfigurationKey scriptKey)
            throws APPlatformException, IOException {

        LOGGER.debug("Validation of script [" + scriptKey + "] started");

        String scriptPath = configuration.getSetting(scriptKey);

        LOGGER.debug("Script path being validated: " + scriptPath + "");

        validateIfScriptPathIsNotEmpty(scriptPath, scriptKey);
        Script script = validateIfScriptExists(scriptPath);
        validateInteractiveCommands(script);
        validateEndOfScript(script);

        LOGGER.debug("Validation of script [" + scriptKey + "] finished successfully");
    }

    public void validateIfScriptPathIsNotEmpty(String scriptPath, ConfigurationKey scriptKey)
            throws APPlatformException {

        if (scriptPath.isEmpty()) {
            throw new APPlatformException("Failed to read service parameter " + scriptKey);
        }
    }

    public Script validateIfScriptExists(String scriptPath) throws APPlatformException {

        try {
            Script script = new Script(scriptPath);
            script.loadContent();
            return script;
        } catch (Exception e) {
            throw new APPlatformException("Script: " + scriptPath + "cannot be loaded: " + e.getMessage());
        }
    }

    public void validateInteractiveCommands(Script script) throws APPlatformException, IOException {
        String[] interactiveCommands = {"read", "ssh", "rm -i"};

        String scriptContent = script.getContent();
        StringReader stringReader = new StringReader(scriptContent);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            for (int i = 0; i < interactiveCommands.length; i++) {
                if (line.matches("(^\\s*" + interactiveCommands[i] +
                        "(\\s?\\w+)*)|(^[a-zA-Z0-9_-]+\\s?(\\w*\\s?)*)*(\\|" + interactiveCommands[i] +
                        "|\\| " + interactiveCommands[i] + ")(\\s?\\w+)*")) {
                    throw new APPlatformException("Script " + script.getPath() + " " +
                            "contains an interactive command! Line that caused the exception: " + line);
                }
            }
        }
    }

    public void validateEndOfScript(Script script) throws APPlatformException {

        String scriptContent = script.getContent();

        if (scriptContent.indexOf("END_OF_SCRIPT") < 0) {
            throw new APPlatformException(
                    "Missing output \"END_OF_SCRIPT\" in " + script.getPath());
        }
    }

}

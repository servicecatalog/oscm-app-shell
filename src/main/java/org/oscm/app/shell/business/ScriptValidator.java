/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-11-14
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.oscm.app.shell.ShellController;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.richfaces.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptValidator.class);

    public void validate(Configuration configuration, ConfigurationKey scriptKey)
            throws APPlatformException {

        LOGGER.debug("Validation of script [" + scriptKey + "] started");

        String scriptPath = configuration.getSetting(scriptKey);

        LOGGER.debug("Script path being validated: " + scriptPath + "");

        validateIfScriptPathIsNotEmpty(scriptPath, scriptKey);
        Script script = validateIfScriptExists(scriptPath);
        validateInteractiveCommands(script);
        validateEndOfScript(script);
        validateJSONinScript(script);

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
            throw new APPlatformException("Script: " + scriptPath + " cannot be loaded: " + e.getMessage());
        }
    }

    public void validateInteractiveCommands(Script script) throws APPlatformException {
        // TODO: Verify if an interactive flag is passed to a command (rm -i, cp -i, mv -i).
        String[] interactiveCommands = {"read"};
        String content = script.getContent();
        StringReader stringReader = new StringReader(content);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        try {
            while ((content = bufferedReader.readLine()) != null) {
                for (int i = 0; i < interactiveCommands.length; i++) {
                    if (content.matches("^(\\s)*" + interactiveCommands[i] + "\\s(.*)")) {
                        throw new APPlatformException("Script " + script.getPath() + " " +
                                "contains an interactive command! Line that caused the exception: " + content);
                    }
                }
            }
        } catch(IOException e) {
            throw new APPlatformException("IOException caught while working with script: " + script.getPath() +
                    ". Error message: " + e.getMessage());
        }
    }

    public void validateEndOfScript(Script script) throws APPlatformException {

        String scriptContent = script.getContent();

        if (scriptContent.indexOf("END_OF_SCRIPT") < 0) {
            throw new APPlatformException(
                    "Missing output \"END_OF_SCRIPT\" in " + script.getPath());
        }
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(ScriptValidator.class);

    public void validateJSONinScript(Script script) {
        String content = script.getContent();

        //search JSON in script content
        final String regex = "(\\{)(\\ \"status\" *: *\"([a-zA-Z]*)\"\\ ,)(\\ \"message\" *: *\".*?\"\\ ,)(\\ \"data\" *: \\{(\\ *\".*?\"\\ ),)(.*\\})";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(content);

        if (matcher.find()){
           LOG.warn("Full match: " + matcher.group(0));
        }
        else{
            LOG.error("JSON is not created correctly or the script doesn't return JSON.");
        }

    }

}

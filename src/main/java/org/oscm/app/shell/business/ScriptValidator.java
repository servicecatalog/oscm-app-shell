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
        } catch (IOException e) {
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

    public void validateJSONinScript(Script script) throws APPlatformException {

        final String multiLineEcho = "echo\\s[\'\"][{]\\s+" +
                "\"status\":\\s\"[$]?[a-zA-Z0-9]+\",\\s+" +
                "\"message\":\\s\"[$]?[a-zA-Z0-9]+\",\\s+" +
                "\"data\":\\s\"[$]?[a-zA-Z0-9]+\"\\s+" +
                "[}]";

        final String multiLineEcho2 = "echo\\s[\'\"][{]\\s+" +
                "\\\\\"status\\\\\":\\s\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\s+" +
                "\\\\\"message\\\\\":\\s\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\s+" +
                "\\\\\"data\\\\\":\\s\\\\\"[$]?[a-zA-Z0-9]+\\\\\"\\s+" +
                "[}]";

        final String printfExample = "printf\\s[\'\"][{]\\\\n\\s" +
                "\\\\\"status\\\\\":\\s\\\\\"%s\\\\\",\\\\n\\s" +
                "\\\\\"message\\\\\":\\s\\\\\"%s\\\\\",\\\\n\\s" +
                "\\\\\"data\\\\\":\\s\\\\\"%s\\\\\"\\\\n" +
                "[}]";

        final String echoExpression = "echo\\s[-]e\\s[\'\"][{]\\\\n\\s" +
                "\"status\":\\s\"[$]?[a-zA-Z0-9]+\",\\\\n\\s" +
                "\"message\":\\s\"[$]?[a-zA-Z0-9]+\",\\\\n\\s" +
                "\"data\":\\s\"[$]?[a-zA-Z0-9]+\"\\\\n" +
                "[}]";

        final String echoExpression2 = "echo\\s[-]e\\s[\'\"][{]\\\\n\\s" +
                "\\\\\"status\\\\\":\\s\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\\\n\\s" +
                "\\\\\"message\\\\\":\\s\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\\\n\\s" +
                "\\\\\"data\\\\\":\\s\\\\\"[$]?[a-zA-Z0-9]+\\\\\"\\\\n" +
                "[}]";

        String content = script.getContent();

        final String defaultErrorMessage = "Script " + script.getPath() +
                "does not return JSON or the JSON is not created correctly";
        final String[] patternList = {multiLineEcho, multiLineEcho2, printfExample, echoExpression, echoExpression2};

        boolean found = false;

        for (String stringPattern : patternList) {
            Pattern pattern = Pattern.compile(stringPattern, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                found = true;
                LOGGER.debug("Found match: " + matcher.group(0));
            }
        }

        if (!found) {
            LOGGER.error(defaultErrorMessage);
            throw new APPlatformException(defaultErrorMessage);
        }

    }
}

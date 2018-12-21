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

        final String readCommand = "^\\s*read\\s";
        final String interactiveFlag = "^((?![#]).)*[a-zA-Z0-9$,.\\s|]--interactive\\s?";

        final String[] patternList = {readCommand, interactiveFlag};

        String content = script.getContent();

        boolean found = false;

        for (String stringPattern : patternList) {
            Pattern pattern = Pattern.compile(stringPattern, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                found = true;
                LOGGER.warn("Found an interactive command: " + matcher.group(0));
            }
        }

        if (found) {
            LOGGER.error("Script " + script.getPath() + " contains an interactive command.");
            throw new APPlatformException("Script " + script.getPath() + " contains an interactive command.");
        }

    }

    public void validateEndOfScript(Script script) throws APPlatformException {

        String scriptContent = script.getContent();

        if (!scriptContent.contains("END_OF_SCRIPT")) {
            throw new APPlatformException(
                    "Missing output \"END_OF_SCRIPT\" in " + script.getPath());
        }
    }

    public void validateJSONinScript(Script script) throws APPlatformException {

        final String multilineEchoWithoutData = "echo\\s" +
                "['\"]\\s*[{]\\s*" +
                "\"status\":\\s?\"[$]?[a-zA-Z0-9]+\",\\s+" +
                "\"message\":\\s?\"[a-zA-Z0-9$,.\\s]+\"\\s*" +
                "[}]\\s*['\"]";

        final String multilineEchoEscapedWithoutData = "echo\\s" +
                "['\"]\\s*[{]\\s*" +
                "\\\\\"status\\\\\":\\s?\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\s+" +
                "\\\\\"message\\\\\":\\s?\\\\\"[a-zA-Z0-9$,.\\s]+\\\\\"\\s*" +
                "[}]\\s*['\"]";

        final String printfWithoutData = "printf\\s" +
                "['\"]\\s*[{]\\s*(\\\\n)?\\s?" +
                "\\\\\"status\\\\\":\\s?\\\\\"%s\\\\\",\\s?\\\\n\\s?" +
                "\\\\\"message\\\\\":\\s?\\\\\"[a-zA-Z0-9$,.\\s]*%s[a-zA-Z0-9$,.\\s]*\\\\\"\\s?(\\\\n)?\\s*" +
                "[}]\\s*['\"]";

        final String echoEWithoutData = "echo\\s[-]e\\s" +
                "['\"]\\s*[{]\\s*(\\\\n)?\\s?" +
                "\"status\":\\s?\"[$]?[a-zA-Z0-9]+\",\\s?\\\\n\\s?" +
                "\"message\":\\s?\"\\s*[a-zA-Z0-9$,.\\s]+\"\\s?(\\\\n)?\\s*" +
                "[}]\\s*['\"]";

        final String echoEEscapedWithoutData = "echo\\s[-]e\\s" +
                "['\"]\\s*[{]\\s*(\\\\n)?\\s?" +
                "\\\\\"status\\\\\":\\s?\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\s?(\\\\n)?\\s?" +
                "\\\\\"message\\\\\":\\s?\\\\\"\\s*[a-zA-Z0-9$,.\\s]+\\\\\"\\s?(\\\\n)?\\s*" +
                "[}]\\s*['\"]";

        final String multilineEchoWithData = "echo\\s" +
                "['\"]\\s*[{]\\s*" +
                "\"status\":\\s?\"[$]?[a-zA-Z0-9]+\",\\s+" +
                "\"message\":\\s?\"\\s*[a-zA-Z0-9$,.\\s]+\",\\s+" +
                "\"data\":\\s*((\\s?[$]?[a-zA-Z0-9\\s]+)|" +
                "([{].*[}]))\\s*" +
                "[}]\\s*['\"]";

        final String multilineEchoEscapedWithData = "echo\\s" +
                "['\"]\\s*[{]\\s*" +
                "\\\\\"status\\\\\":\\s?\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\s+" +
                "\\\\\"message\\\\\":\\s?\\\\\"\\s*[a-zA-Z0-9$,.\\s]+\\\\\",\\s+" +
                "\\\\\"data\\\\\":\\s*((\\s?[$]?[a-zA-Z0-9\\s]+)|" +
                "([{].*[}]))\\s*" +
                "[}]\\s*['\"]";

        final String printfWithData = "printf\\s" +
                "['\"]\\s*[{]\\s*(\\\\n)?\\s*" +
                "\\\\\"status\\\\\":\\s?\\\\\"\\s?%s\\s?\\\\\",\\\\n\\s*" +
                "\\\\\"message\\\\\":\\s?\\\\\"[a-zA-Z0-9$,.\\s]*%s[a-zA-Z0-9$,.\\s]*\\\\\"\\\\n\\s*" +
                "\\\\\"data\\\\\":\\s?\\s?%s\\s?(\\\\n)?\\s*" +
                "[}]\\s*['\"]";

        final String echoEWithData = "echo\\s[-]e\\s" +
                "['\"]\\s*[{]\\s*(\\\\n)?\\s*" +
                "\"status\":\\s?\"[$]?[a-zA-Z0-9]+\",\\s?\\\\n\\s*" +
                "\"message\":\\s?\"\\s*[a-zA-Z0-9$,.\\s]+\",\\s?\\\\n\\s*" +
                "\"data\":\\s?\"[$]?[a-zA-Z0-9]+\"\\s?(\\\\n)?\\s*" +
                "[}]\\s*['\"]";

        final String echoEEscapedWithData = "echo\\s[-]e\\s" +
                "['\"]\\s*[{]\\s*(\\\\n)?\\s*" +
                "\\\\\"status\\\\\":\\s?\\\\\"[$]?[a-zA-Z0-9]+\\\\\",\\s*(\\\\n)?\\s?" +
                "\\\\\"message\\\\\":\\s?\\\\\"\\s*[a-zA-Z0-9$\\s]+\\\\\",\\s*(\\\\n)?\\s?" +
                "\\\\\"data\\\\\":\\s?[$]?[a-zA-Z0-9\\s]+\\s*(\\\\n)?\\s*" +
                "[}]\\s*['\"]";


        final String[] patternList = {multilineEchoWithoutData, multilineEchoEscapedWithoutData,
                printfWithoutData, echoEWithoutData, echoEEscapedWithoutData, multilineEchoWithData,
                multilineEchoEscapedWithData, printfWithData, echoEWithData, echoEEscapedWithData};

        String content = script.getContent();

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
            LOGGER.error("Script " + script.getPath() +
                    " does not return JSON or the JSON is not created correctly");
            throw new APPlatformException("Script " + script.getPath() +
                    " does not return JSON or the JSON is not created correctly");
        }

    }
}

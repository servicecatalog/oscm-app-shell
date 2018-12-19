/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-11-15
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.junit.Test;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;

import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.oscm.app.shell.business.ConfigurationKey.*;

public class ScriptValidatorTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test
    public void testValidate() throws Exception {

        //given
        Configuration configuration = new Configuration(getProvisioningSettings());
        ScriptValidator validator = spy(new ScriptValidator());
        doReturn(getValidScript()).when(validator).validateIfScriptExists(anyString());

        //when
        validator.validate(configuration, ConfigurationKey.PROVISIONING_SCRIPT);

        //then
        verify(validator, times(1)).validateEndOfScript(any(Script.class));
        verify(validator, times(1)).validateInteractiveCommands(any(Script.class));

    }

    @Test
    public void testValidateIfScriptPathIsNotEmpty() throws APPlatformException {

        //given
        ScriptValidator validator = new ScriptValidator();
        String scriptPath = "/path/to/script";

        //when
        validator.validateIfScriptPathIsNotEmpty(scriptPath, ConfigurationKey.PROVISIONING_SCRIPT);

        //then
        //validation executed without any issues
    }

    @Test(expected = APPlatformException.class)
    public void testValidateIfScriptPathIsNotEmpty_pathIsEmpty() throws APPlatformException {

        //given
        ScriptValidator validator = new ScriptValidator();
        String scriptPath = "";

        //when
        validator.validateIfScriptPathIsNotEmpty(scriptPath, ConfigurationKey.PROVISIONING_SCRIPT);

        //then
        //exception is thrown
    }

    @Test
    public void testValidateEndOfScript() throws Exception {

        ScriptValidator validator = new ScriptValidator();
        Script validScript = getValidScript();

        //when
        validator.validateEndOfScript(validScript);

        //then
        //validation executed without any issues
    }

    @Test(expected = APPlatformException.class)
    public void testValidateEndOfScript_invalidEnd() throws Exception {

        ScriptValidator validator = new ScriptValidator();
        Script invalidScript = getInvalidScript();

        //when
        validator.validateEndOfScript(invalidScript);

        //then
        //exception is thrown
    }

    @Test
    public void testValidateJSONinScript() throws Exception {

        ScriptValidator validator = new ScriptValidator();
        Script validScript = getValidScript();

        validator.validateJSONinScript(validScript);

    }

    @Test(expected = APPlatformException.class)
    public void testValidateJSONinScript_noJSON() throws Exception {

        ScriptValidator validator = new ScriptValidator();
        Script invalidScript = getInvalidScript();

        validator.validateJSONinScript(invalidScript);

    }

    private Script getValidScript() throws Exception {

        String filename = "file.name";
        Script script = spy(new Script(filename));
        doReturn(getValidScriptContent()).when(script).loadLocalScript(anyString());
        doReturn(getValidScriptContent()).when(script).getContent();
        return script;
    }

    private Script getInvalidScript() throws Exception {

        String filename = "file.name";
        Script script = spy(new Script(filename));
        doReturn(getInvalidScriptContent()).when(script).loadLocalScript(anyString());
        doReturn(getInvalidScriptContent()).when(script).getContent();
        return script;
    }

    private String getValidScriptContent() {

        return "#!/bin/bash" + NEW_LINE +
                "# This is a comment!" + NEW_LINE +
                "echo Hello World" + NEW_LINE +
                "echo '{" + NEW_LINE +
                " \"status\": \"ok\"," + NEW_LINE +
                " \"message\": \"valid message\"," + NEW_LINE +
                " \"data\": {" + NEW_LINE +
                "   \"key1\": \"value1\"," + NEW_LINE +
                "   \"key2\": \"value2\"" + NEW_LINE +
                " }" + NEW_LINE +
                "}'" + NEW_LINE +
                "sleep 10s" + NEW_LINE +
                "echo END_OF_SCRIPT";
    }

    private String getInvalidScriptContent() {

        return "#!/bin/bash" + NEW_LINE +
                "# This is a comment!" + NEW_LINE +
                "echo Hello World" + NEW_LINE +
                "sleep 10s" + NEW_LINE +
                "echo NO_END";
    }

    private ProvisioningSettings getProvisioningSettings() {

        String sampleScriptPath = "/some/path/script.sh";

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put(PROVISIONING_SCRIPT.name(), new Setting(PROVISIONING_SCRIPT.name(), sampleScriptPath));
        parameters.put(UPDATE_SCRIPT.name(), new Setting(UPDATE_SCRIPT.name(), sampleScriptPath));
        parameters.put(DEPROVISIONING_SCRIPT.name(), new Setting(DEPROVISIONING_SCRIPT.name(), sampleScriptPath));
        parameters.put(ASSIGN_USER_SCRIPT.name(), new Setting(ASSIGN_USER_SCRIPT.name(), sampleScriptPath));
        parameters.put(DEASSIGN_USER_SCRIPT.name(), new Setting(DEASSIGN_USER_SCRIPT.name(), sampleScriptPath));
        parameters.put(CHECK_STATUS_SCRIPT.name(), new Setting(CHECK_STATUS_SCRIPT.name(), sampleScriptPath));

        HashMap<String, Setting> emptyMap = new HashMap<>();

        ProvisioningSettings settings = new ProvisioningSettings(parameters, emptyMap, emptyMap,
                emptyMap, "en");

        ServiceUser user = new ServiceUser();
        user.setUserId("supplier");
        settings.setRequestingUser(user);

        return settings;
    }

}

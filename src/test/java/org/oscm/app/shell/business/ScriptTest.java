/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.junit.Test;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.oscm.app.shell.business.ConfigurationKey.INSTANCE_ID;
import static org.oscm.app.shell.business.ConfigurationKey.OPERATIONS_ID;

public class ScriptTest {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String LOCAL_SCRIPT_LOCATION = "/opt/scripts/";

    @Test
    public void testLoadContent_localScriptIsLoaded() throws Exception {

        //given
        String filename = "file.name";
        Script script = spy(new Script(filename));
        doReturn(getValidScript()).when(script).loadLocalScript(anyString());

        //when
        script.loadContent();

        //then
        verify(script, times(1)).loadLocalScript(LOCAL_SCRIPT_LOCATION + filename);
    }

    @Test
    public void testLoadContent_externalScriptIsLoaded() throws Exception {

        //given
        String filename = "http://example.com/script.sh";
        Script script = spy(new Script(filename));
        doReturn(getValidScript()).when(script).loadExternalScript(anyString());

        //when
        script.loadContent();

        //then
        verify(script, times(1)).loadExternalScript(filename);
    }

    @Test
    public void testInsertServiceParameter() throws Exception {

        // given
        Script script = spy(new Script("someFile.path"));
        doReturn(getValidScript()).when(script).loadLocalScript(anyString());

        String subscriptionId = "Sub_123";
        String instanceId = "Instance_123";
        String userId = "supplier";
        String organizationId = "est01018";
        String referenceId = "ref4564423255";

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key", new Setting("key", "value"));
        parameters.put(INSTANCE_ID.name(), new Setting(INSTANCE_ID.name(), instanceId));

        ServiceUser user = new ServiceUser();
        user.setUserId(userId);

        ProvisioningSettings settings = new ProvisioningSettings(parameters, null, "");
        settings.setSubscriptionId(subscriptionId);
        settings.setOrganizationId(organizationId);
        settings.setRequestingUser(user);
        settings.setReferenceId(referenceId);

        // when
        script.loadContent();
        script.insertServiceParameters(settings);

        // then
        assertTrue(script.getContent().contains("key=\"value\""));
        assertTrue(script.getContent().contains("INSTANCE_ID=\"" + instanceId + "\""));
        assertTrue(script.getContent().contains("SUBSCRIPTION_ID=\"" + subscriptionId + "\""));
        assertTrue(script.getContent().contains("REQUESTING_ORGANIZATION_ID=\"" + organizationId + "\""));
        assertTrue(script.getContent().contains("REQUESTING_USER=\"" + userId + "\""));
        assertTrue(script.getContent().contains("REFERENCE_ID=\"" + referenceId + "\""));
    }

    @Test
    public void testInsertOperationId() throws Exception {

        // given
        Script script = spy(new Script("someFile.path"));
        doReturn(getValidScript()).when(script).loadLocalScript(anyString());

        String operationId = "op_5234532423";

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key", new Setting("key", "value"));
        parameters.put(OPERATIONS_ID.name(), new Setting(OPERATIONS_ID.name(), operationId));

        ProvisioningSettings settings = new ProvisioningSettings(parameters, null, "");

        Configuration configuration = new Configuration(settings);

        // when
        script.loadContent();
        script.insertOperationId(configuration);

        // then
        assertTrue(script.getContent().contains("OPERATION=\"" + operationId + "\""));
    }

    private String getValidScript() {

        return "#!/bin/bash" + NEW_LINE +
                "# This is a comment!" + NEW_LINE +
                "echo Hello World" + NEW_LINE +
                "sleep 10s" + NEW_LINE +
                "echo END_OF_SCRIPT";
    }

}

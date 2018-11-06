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
import static org.oscm.app.shell.business.ConfigurationKey.INSTANCE_ID;
import static org.oscm.app.shell.business.ConfigurationKey.OPERATIONS_ID;

public class ScriptTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test
    public void testInsertServiceParameter() throws Exception {

        // given
        String subscriptionId = "Sub_123";
        String instanceId = "Instance_123";
        String userId = "supplier";
        String organizationId = "est01018";
        String referenceId = "ref4564423255";

        Script script = new Script();
        script.script = getValidScript();

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
        script.insertServiceParameters(settings);

        // then
        assertTrue(script.get().contains("key=\"value\""));
        assertTrue(script.get().contains("INSTANCE_ID=\"" + instanceId + "\""));
        assertTrue(script.get().contains("SUBSCRIPTION_ID=\"" + subscriptionId + "\""));
        assertTrue(script.get().contains("REQUESTING_ORGANIZATION_ID=\"" + organizationId + "\""));
        assertTrue(script.get().contains("REQUESTING_USER=\"" + userId + "\""));
        assertTrue(script.get().contains("REFERENCE_ID=\"" + referenceId + "\""));
    }

    @Test
    public void testInsertOperationId() throws Exception {

        // given
        String operationId = "op_5234532423";

        Script script = new Script();
        script.script = getValidScript();

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key", new Setting("key", "value"));
        parameters.put(OPERATIONS_ID.name(), new Setting(OPERATIONS_ID.name(), operationId));

        ProvisioningSettings settings = new ProvisioningSettings(parameters, null, "");

        Configuration configuration = new Configuration(settings);

         // when
        script.insertOperationId(configuration);

        // then
        assertTrue(script.get().contains("OPERATION=\"" + operationId + "\""));
    }

    private String getValidScript() {

        return "#!/bin/bash" + NEW_LINE +
                "# This is a comment!" + NEW_LINE +
                "echo Hello World" + NEW_LINE +
                "sleep 10s" + NEW_LINE +
                "echo END_OF_SCRIPT";
    }

}

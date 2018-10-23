package com.fujitsu.bss.app.powershell.business.actions;

import static com.fujitsu.bss.app.powershell.business.ConfigurationKey.SCRIPT_FILE;
import static com.fujitsu.bss.app.powershell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static com.fujitsu.bss.app.powershell.business.actions.StatemachineEvents.FAILED;

import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.bss.app.powershell.business.Configuration;
import com.fujitsu.bss.app.powershell.business.Script;
import com.fujitsu.bss.app.statemachine.api.StateMachineAction;

public class OperationActions {

    private static final Logger LOG = LoggerFactory.getLogger(OperationActions.class);

    Actions getActions() {
	return new Actions();
    }

    @StateMachineAction
    public String executeScript(String instanceId, ProvisioningSettings settings, InstanceStatus result) {
	Configuration config = new Configuration(settings);
	try {
	    Script script = new Script(config.getSetting(SCRIPT_FILE));
	    script.insertOperationId(config);
	    script.insertServiceParameter(settings);
	    return getActions().executeScript(instanceId, settings, result, script);
	} catch (Exception e) {
	    LOG.error("Couldn't execute powershell script", e);
	    config.setSetting(SM_ERROR_MESSAGE, e.getMessage());
	    return FAILED;
	}
    }

    @StateMachineAction
    public String consumeScriptOutput(String instanceId, ProvisioningSettings settings, InstanceStatus result)
	    throws Exception {

	return getActions().consumeScriptOutput(instanceId, settings, result);
    }

    @StateMachineAction
    public String finalizeOperation(String instanceId, ProvisioningSettings settings, InstanceStatus result) {
	result.setIsReady(true);
	return StatemachineEvents.SUCCESS;
    }

}

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business.actions;

import org.oscm.app.shell.ScriptLogger;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.shell.business.script.Script;
import org.oscm.app.statemachine.api.StateMachineAction;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.oscm.app.shell.business.ConfigurationKey.SCRIPT_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.shell.business.actions.StatemachineEvents.FAILED;

public class OperationActions {

    private static final Logger LOG = LoggerFactory.getLogger(OperationActions.class);

    Actions getActions() {
        return new Actions();
    }

    @StateMachineAction
    public String executeScript(String instanceId, ProvisioningSettings settings,
                                InstanceStatus result) {

        Configuration config = new Configuration(settings);

        try {
            Script script = new Script(config.getSetting(SCRIPT_FILE));
            script.loadContent();
            script.insertOperationId(config);
            script.insertProvisioningSettings(settings);
            ScriptLogger.logScriptConfiguration(config, ConfigurationKey.OPERATIONS_SCRIPT.name(),
                    script.getScriptContent());
            return getActions().executeScript(instanceId, settings, result, script);
        } catch (Exception e) {
            LOG.error("Couldn't execute shell script", e);
            config.setSetting(SM_ERROR_MESSAGE, e.getMessage());
            return FAILED;
        }
    }

    @StateMachineAction
    public String consumeScriptOutput(String instanceId, ProvisioningSettings settings,
                                      InstanceStatus result) throws Exception {

        return getActions().consumeScriptOutput(instanceId, settings, result);
    }

    @StateMachineAction
    public String finalizeScriptExecution(String instanceId, ProvisioningSettings settings,
                                          InstanceStatus result) throws Exception {

        return getActions().finalizeScriptExecution(instanceId, settings, result);
    }

}

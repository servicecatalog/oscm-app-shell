/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business.actions;

import static org.oscm.app.shell.business.ConfigurationKey.SCRIPT_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.shell.business.actions.StatemachineEvents.FAILED;

import org.oscm.app.shell.ScriptLogger;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.shell.business.Script;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.statemachine.api.StateMachineAction;

public class OperationActions {

        private static final Logger LOG = LoggerFactory
                .getLogger(OperationActions.class);

        Actions getActions() {
                return new Actions();
        }

        @StateMachineAction
        public String executeScript(String instanceId,
                ProvisioningSettings settings, InstanceStatus result) {
                Configuration config = new Configuration(settings);
                ScriptLogger logger = new ScriptLogger();
                try {
                        Script script = new Script(
                                config.getSetting(SCRIPT_FILE));
                        script.insertOperationId(config);
                        script.insertServiceParameters(settings);
                        logger.logScriptConfiguration(config,
                                ConfigurationKey.PROVISIONING_SCRIPT.name(),
                                script.get());
                        return getActions()
                                .executeScript(instanceId, settings, result,
                                        script);
                } catch (Exception e) {
                        LOG.error("Couldn't execute shell script", e);
                        config.setSetting(SM_ERROR_MESSAGE, e.getMessage());
                        return FAILED;
                }
        }

        @StateMachineAction
        public String consumeScriptOutput(String instanceId,
                ProvisioningSettings settings, InstanceStatus result)
                throws Exception {

                return getActions()
                        .consumeScriptOutput(instanceId, settings, result);
        }

        @StateMachineAction
        public String finalizeOperation(String instanceId,
                ProvisioningSettings settings, InstanceStatus result) {
                result.setIsReady(true);
                return StatemachineEvents.SUCCESS;
        }

}

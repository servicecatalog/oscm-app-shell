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
import org.oscm.app.shell.business.Script;
import org.oscm.app.shell.business.api.*;
import org.oscm.app.statemachine.api.StateMachineAction;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;

import static org.oscm.app.shell.business.ConfigurationKey.CONSOLE_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.shell.business.actions.StatemachineEvents.*;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;
import static org.oscm.app.shell.business.api.ShellStatus.SUCCESS;

public class Actions {

    private static final Logger LOG = LoggerFactory
            .getLogger(Actions.class);

    private ScriptLogger logger;

    private ShellPool pool = CDI.current().select(ShellPool.class).get();

    public String executeScript(String instanceId,
                                ProvisioningSettings settings, InstanceStatus result,
                                Script script)
            throws Exception {

        Configuration config = new Configuration(settings);
        logger = new ScriptLogger();

        ShellCommand command = new ShellCommand(script.getContent());
        logger.logScriptCommand(command);
        String consoleFile = config.getSetting(CONSOLE_FILE);

        try {
            pool.runCommand(command, instanceId, consoleFile);
            return EXECUTING;
        } catch (ShellPoolException e) {
            config.setSetting(SM_ERROR_MESSAGE,
                    "Shell pool capacity reached. No free shell available.");
            return FAILED;
        }
    }

    @StateMachineAction
    public String consumeScriptOutput(String instanceId,
                                      ProvisioningSettings settings, InstanceStatus result)
            throws Exception {

        ScriptLogger logger = new ScriptLogger();
        ShellStatus shellStatus = pool.consumeShellOutput(instanceId);

        if (shellStatus == RUNNING) {
            LOG.info("script is still running....");
            return RUN;
        }

        if (shellStatus == SUCCESS) {
            LOG.info("Calling the script was successful");
            return StatemachineEvents.SUCCESS;
        }

        String errorOutput = pool.getShellErrorOutput(instanceId);
        LOG.error("Shell error output: " + errorOutput);
        pool.terminateShell(instanceId);
        new Configuration(settings)
                .setSetting(SM_ERROR_MESSAGE, errorOutput);
        return ERROR;
    }

    public String finalizeScriptExecution(String instanceId, ProvisioningSettings settings,
                                          InstanceStatus result) throws ShellPoolException {

        ShellResult shellResult = pool.getShellResult(instanceId);
        shellResult.getData().ifPresent(data -> result.setAccessInfo(data.getAccessInfo()));

        result.setIsReady(true);
        pool.terminateShell(instanceId);
        return StatemachineEvents.SUCCESS;
    }


}

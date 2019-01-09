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
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;

import static org.oscm.app.shell.business.ConfigurationKey.CONSOLE_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.shell.business.actions.StatemachineEvents.*;

public class Actions {

    private static final Logger LOG = LoggerFactory.getLogger(Actions.class);

    private ScriptLogger logger;

    private ShellPool pool;

    private void initializeShellPool() {
        if (pool == null) {
            pool = CDI.current().select(ShellPool.class).get();
        }
    }

    public String executeScript(String instanceId, ProvisioningSettings settings, InstanceStatus result, Script script)
            throws IOException, APPlatformException {

        initializeShellPool();
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

    public String consumeScriptOutput(String instanceId, ProvisioningSettings settings, InstanceStatus result)
            throws ShellPoolException, ShellResultException {

        initializeShellPool();
        ShellStatus shellStatus = pool.consumeShellOutput(instanceId);
        String stateMachineEvent;

        switch (shellStatus) {

            case RUNNING:
                stateMachineEvent = RUN;
                break;

            case SUCCESS:
                ShellResult shellResult = pool.getShellResult(instanceId);

                if (Shell.STATUS_ERROR.equals(shellResult.getStatus())) {
                    stateMachineEvent = ERROR;
                } else {
                    stateMachineEvent = SUCCESS;
                    break;
                }

            default:
                ShellResult errorShellResult = pool.getShellResult(instanceId);
                String errorMessage = errorShellResult.getMessage();

                String errorMsgKey = SM_ERROR_MESSAGE.name();
                settings.getParameters().put(errorMsgKey, new Setting(errorMsgKey, errorMessage));
                stateMachineEvent = ERROR;
                break;
        }

        LOG.info("Instance [" + instanceId + "] returned with StateMachineEvent " +
                "[" + stateMachineEvent + "]");
        return stateMachineEvent;
    }

    public String finalizeScriptExecution(String instanceId, ProvisioningSettings settings, InstanceStatus result)
            throws ShellPoolException, ShellResultException {

        initializeShellPool();
        ShellResult shellResult = pool.getShellResult(instanceId);
        shellResult.getData().ifPresent(data -> result.setAccessInfo(data.getAccessInfo()));

        result.setIsReady(true);
        pool.terminateShell(instanceId);
        return StatemachineEvents.SUCCESS;
    }


}

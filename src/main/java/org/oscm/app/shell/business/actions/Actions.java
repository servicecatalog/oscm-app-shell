/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.actions;

import static org.oscm.app.shell.business.ConfigurationKey.CONSOLE_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.shell.business.actions.StatemachineEvents.ERROR;
import static org.oscm.app.shell.business.actions.StatemachineEvents.EXECUTING;
import static org.oscm.app.shell.business.actions.StatemachineEvents.FAILED;
import static org.oscm.app.shell.business.actions.StatemachineEvents.RUN;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;
import static org.oscm.app.shell.business.api.ShellStatus.SUCCESS;

import javax.enterprise.inject.spi.CDI;

import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.Script;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellPool;
import org.oscm.app.shell.business.api.ShellPoolException;
import org.oscm.app.shell.business.api.ShellStatus;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.statemachine.api.StateMachineAction;

public class Actions {

    private static final Logger LOG = LoggerFactory.getLogger(Actions.class);

    private ShellPool pool = null;

    private void getPool() throws Exception {
	if (pool == null) {
	    pool = CDI.current().select(ShellPool.class).get();
	}
    }

    public String executeScript(String instanceId, ProvisioningSettings settings, InstanceStatus result, Script script)
	    throws Exception {

	Configuration config = new Configuration(settings);
	getPool();
	ShellCommand command = new ShellCommand(script.getContent());
	String consoleFile = config.getSetting(CONSOLE_FILE);

	try {
	    pool.runCommand(command, instanceId, consoleFile);
	    return EXECUTING;
	} catch (ShellPoolException e) {
	    config.setSetting(SM_ERROR_MESSAGE, "Shell pool capacity reached. No free shell available.");
	    return FAILED;
	}
    }

    @StateMachineAction
    public String consumeScriptOutput(String instanceId, ProvisioningSettings settings, InstanceStatus result)
	    throws Exception {

	getPool();
	ShellStatus shellStatus = pool.consumeShellOutput(instanceId);
	if (shellStatus == RUNNING) {
	    return RUN;
	}

	if (shellStatus == SUCCESS) {
	    pool.terminateShell(instanceId);
	    return StatemachineEvents.SUCCESS;
	}

	String errorOutput = pool.getShellErrorOutput(instanceId);
	LOG.error("Shell error output: " + errorOutput);
	pool.terminateShell(instanceId);
	new Configuration(settings).setSetting(SM_ERROR_MESSAGE, errorOutput);
	return ERROR;
    }

}

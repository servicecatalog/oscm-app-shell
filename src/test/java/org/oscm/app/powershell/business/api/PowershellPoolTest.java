/*******************************************************************************
 *
 *  COPYRIGHT (C) 2017 FUJITSU Limited - ALL RIGHTS RESERVED.
 *
 *  Creation Date: 20.03.2017
 *
 *******************************************************************************/

package org.oscm.app.powershell.business.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.oscm.app.powershell.business.api.PowershellStatus.PSSHELL_ERROR;
import static org.oscm.app.powershell.business.api.PowershellStatus.RUNNING;
import static org.oscm.app.powershell.business.api.PowershellStatus.SUCCESS;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oscm.app.powershell.business.api.PowershellCommand;
import org.oscm.app.powershell.business.api.PowershellPool;
import org.oscm.app.powershell.business.api.PowershellStatus;

public class PowershellPoolTest {

    private PowershellPool pool;

    @Before
    public void before() {
	pool = new PowershellPool();
	pool.initializeResources();
    }

    @After
    public void after() {
	pool.releaseResources();
    }

    @Test
    public void runSingleCommand() throws Exception {
	// given
	String instanceId = "myInstanceId";
	PowershellCommand command = new PowershellCommand("echo $?;sleep(2);echo \"END_OF_SCRIPT\"");
	String powershellConsoleFile = "";

	// when
	PowershellStatus res = pool.runCommand(command, instanceId, powershellConsoleFile);

	// then
	assertEquals(PowershellStatus.RUNNING, res);
	assertEquals(1, pool.shellPool.size());
	assertEquals(99, pool.shellctrl.availablePermits());
    }

    @Test
    public void runSingleCommand_readShellOutput() throws Exception {
	// given
	String instanceId = "myInstanceId";
	PowershellCommand command = new PowershellCommand("echo $?;sleep(2);echo \"END_OF_SCRIPT\"");
	pool.runCommand(command, instanceId, "");

	// when
	PowershellStatus res;
	do {
	    res = pool.consumeShellOutput(instanceId);
	} while (res == PowershellStatus.RUNNING);

	// then
	pool.unlockShell(instanceId);
	assertEquals(1, pool.shellPool.size());
	assertEquals(99, pool.shellctrl.availablePermits());
	assertEquals(PowershellStatus.SUCCESS, res);
    }

    @Test
    public void exitShellScript() throws Exception {
	String instanceId = "myInstanceId";
	PowershellCommand command = new PowershellCommand();
	command.init("sleep(1);exit;");
	String powershellConsoleFile = "";

	// when
	pool.runCommand(command, instanceId, powershellConsoleFile);
	PowershellStatus res = pool.consumeShellOutput(instanceId);

	// then
	assertEquals(PowershellStatus.RUNNING, res);
	pool.terminateShell(instanceId);
	assertEquals(0, pool.shellPool.size());
	assertEquals(100, pool.shellctrl.availablePermits());
    }

    @Test
    public void useDifferentShells() throws Exception {
	// given
	PowershellCommand cmd1 = new PowershellCommand("echo $?;\r\nsleep(5);\r\necho \"END_OF_SCRIPT\"");
	PowershellCommand cmd2 = new PowershellCommand("echo $?;\r\nsleep(6);\r\necho \"END_OF_SCRIPT\"");
	PowershellCommand cmd3 = new PowershellCommand("echo $?;\r\nsleep(7);\r\necho \"END_OF_SCRIPT\"");
	String consoleFile = "";
	pool.runCommand(cmd1, "id1", consoleFile);
	pool.runCommand(cmd2, "id2", consoleFile);
	pool.runCommand(cmd3, "id3", consoleFile);

	// when
	long ref = System.currentTimeMillis();
	long runtime = 0;
	boolean done = false;
	do {
	    PowershellStatus res1 = pool.consumeShellOutput("id1");
	    assertTrue(res1 == SUCCESS || res1 == RUNNING);

	    PowershellStatus res2 = pool.consumeShellOutput("id2");
	    assertTrue(res2 == SUCCESS || res2 == RUNNING);

	    PowershellStatus res3 = pool.consumeShellOutput("id3");
	    assertTrue(res3 == SUCCESS || res3 == RUNNING);

	    done = (res1 == SUCCESS) && (res2 == SUCCESS) && (res3 == SUCCESS);
	    // done = (res1 == SUCCESS);
	    runtime = System.currentTimeMillis() - ref;
	} while (!done && runtime < 20000);

	// then
	assertTrue(runtime < 20000);
	assertEquals(3, pool.shellPool.size());
	assertEquals(97, pool.shellctrl.availablePermits());
    }

    @Test
    public void reuseShell() throws Exception {
	PowershellCommand cmd1 = new PowershellCommand("echo $?;echo \"END_OF_SCRIPT\"");
	PowershellCommand cmd2 = new PowershellCommand("echo $?;echo \"END_OF_SCRIPT\"");
	String consoleFile = "";
	PowershellStatus res = pool.runCommand(cmd1, "id1", consoleFile);
	assertEquals(PowershellStatus.RUNNING, res);

	do {
	    res = pool.consumeShellOutput("id1");
	    assertTrue(res == SUCCESS || res == RUNNING);
	} while (res != SUCCESS);

	pool.unlockShell("id1");

	res = pool.runCommand(cmd2, "id2", consoleFile);
	assertEquals(PowershellStatus.RUNNING, res);

	do {
	    res = pool.consumeShellOutput("id2");
	    assertTrue(res == SUCCESS || res == RUNNING);
	} while (res != SUCCESS);

    }

    @Test
    public void runFailingCommand() throws Exception {
	PowershellCommand cmd1 = new PowershellCommand("unknown_command;echo \"END_OF_SCRIPT\"");
	String consoleFile = "";
	PowershellStatus res = pool.runCommand(cmd1, "id1", consoleFile);

	do {
	    res = pool.consumeShellOutput("id1");
	    assertTrue(res.name(), res == PSSHELL_ERROR || res == RUNNING);
	} while (res != PSSHELL_ERROR);

	pool.unlockShell("id1");
    }

}

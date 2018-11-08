/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.oscm.app.shell.business.api.ShellStatus.PSSHELL_ERROR;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;
import static org.oscm.app.shell.business.api.ShellStatus.SUCCESS;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellPool;
import org.oscm.app.shell.business.api.ShellStatus;

@Ignore
public class ShellPoolTest {

    private ShellPool pool;

    @Before
    public void before() {
	pool = new ShellPool();
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
	ShellCommand command = new ShellCommand("echo $?;sleep(2);echo \"END_OF_SCRIPT\"");
	String shellConsoleFile = "";

	// when
	ShellStatus res = pool.runCommand(command, instanceId, shellConsoleFile);

	// then
	assertEquals(ShellStatus.RUNNING, res);
	assertEquals(1, pool.shellPool.size());
	assertEquals(99, pool.shellctrl.availablePermits());
    }

    @Test
    public void runSingleCommand_readShellOutput() throws Exception {
	// given
	String instanceId = "myInstanceId";
	ShellCommand command = new ShellCommand("echo $?;sleep(2);echo \"END_OF_SCRIPT\"");
	pool.runCommand(command, instanceId, "");

	// when
	ShellStatus res;
	do {
	    res = pool.consumeShellOutput(instanceId);
	} while (res == ShellStatus.RUNNING);

	// then
	pool.unlockShell(instanceId);
	assertEquals(1, pool.shellPool.size());
	assertEquals(99, pool.shellctrl.availablePermits());
	assertEquals(ShellStatus.SUCCESS, res);
    }

    @Test
    public void exitShellScript() throws Exception {
	String instanceId = "myInstanceId";
	ShellCommand command = new ShellCommand();
	command.init("sleep(1);exit;");
	String shellConsoleFile = "";

	// when
	pool.runCommand(command, instanceId, shellConsoleFile);
	ShellStatus res = pool.consumeShellOutput(instanceId);

	// then
	assertEquals(ShellStatus.RUNNING, res);
	pool.terminateShell(instanceId);
	assertEquals(0, pool.shellPool.size());
	assertEquals(100, pool.shellctrl.availablePermits());
    }

    @Test
    public void useDifferentShells() throws Exception {
	// given
	ShellCommand cmd1 = new ShellCommand("echo $?;\r\nsleep(5);\r\necho \"END_OF_SCRIPT\"");
	ShellCommand cmd2 = new ShellCommand("echo $?;\r\nsleep(6);\r\necho \"END_OF_SCRIPT\"");
	ShellCommand cmd3 = new ShellCommand("echo $?;\r\nsleep(7);\r\necho \"END_OF_SCRIPT\"");
	String consoleFile = "";
	pool.runCommand(cmd1, "id1", consoleFile);
	pool.runCommand(cmd2, "id2", consoleFile);
	pool.runCommand(cmd3, "id3", consoleFile);

	// when
	long ref = System.currentTimeMillis();
	long runtime = 0;
	boolean done = false;
	do {
	    ShellStatus res1 = pool.consumeShellOutput("id1");
	    assertTrue(res1 == SUCCESS || res1 == RUNNING);

	    ShellStatus res2 = pool.consumeShellOutput("id2");
	    assertTrue(res2 == SUCCESS || res2 == RUNNING);

	    ShellStatus res3 = pool.consumeShellOutput("id3");
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
	ShellCommand cmd1 = new ShellCommand("echo $?;echo \"END_OF_SCRIPT\"");
	ShellCommand cmd2 = new ShellCommand("echo $?;echo \"END_OF_SCRIPT\"");
	String consoleFile = "";
	ShellStatus res = pool.runCommand(cmd1, "id1", consoleFile);
	assertEquals(ShellStatus.RUNNING, res);

	do {
	    res = pool.consumeShellOutput("id1");
	    assertTrue(res == SUCCESS || res == RUNNING);
	} while (res != SUCCESS);

	pool.unlockShell("id1");

	res = pool.runCommand(cmd2, "id2", consoleFile);
	assertEquals(ShellStatus.RUNNING, res);

	do {
	    res = pool.consumeShellOutput("id2");
	    assertTrue(res == SUCCESS || res == RUNNING);
	} while (res != SUCCESS);

    }

    @Test
    public void runFailingCommand() throws Exception {
	ShellCommand cmd1 = new ShellCommand("unknown_command;echo \"END_OF_SCRIPT\"");
	String consoleFile = "";
	ShellStatus res = pool.runCommand(cmd1, "id1", consoleFile);

	do {
	    res = pool.consumeShellOutput("id1");
	    assertTrue(res.name(), res == PSSHELL_ERROR || res == RUNNING);
	} while (res != PSSHELL_ERROR);

	pool.unlockShell("id1");
    }

}

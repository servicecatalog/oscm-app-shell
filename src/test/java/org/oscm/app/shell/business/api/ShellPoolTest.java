/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.junit.*;
import org.mockito.internal.util.reflection.Whitebox;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.oscm.app.shell.business.api.Shell.STATUS_ERROR;
import static org.oscm.app.shell.business.api.ShellStatus.*;


/**
 * FIXME
 */

public class ShellPoolTest {
//    private ShellPool pool;
//
//    @Before
//    public void before() {
//            pool = new ShellPool();
//            pool.initializeResources();
//    }
//
//    @After
//    public void after() {
//        pool.releaseResources();
//    }

    @Test
    public void testGetShellOutput_ReturnsOutput_ifFoundShellForLockId() throws Exception {

        //given
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.addOutputLine("output string");
        Shell shell = new Shell();
        Whitebox.setInternalState(shell, "command", shellCommand);
        shell.setLockId("100");
        ArrayList<Shell> shellPoolList = new ArrayList<>();
        shellPoolList.add(shell);
        ShellPool shellPool = new ShellPool();
        Whitebox.setInternalState(shellPool, "shellPool", shellPoolList);

        //when
        String output = shellPool.getShellOutput("100");

        //then
        Assert.assertEquals("output string\n\t", output);
    }

    @Test
    public void testGetShellOutput_ReturnsEmptyString_ifLockIdNotFound() throws Exception {

        //given
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.addOutputLine("output string");
        Shell shell = new Shell();
        Whitebox.setInternalState(shell, "command", shellCommand);
        shell.setLockId("100");
        ArrayList<Shell> shellPoolList = new ArrayList<>();
        shellPoolList.add(shell);
        ShellPool shellPool = new ShellPool();
        Whitebox.setInternalState(shellPool, "shellPool", shellPoolList);

        //when
        String output = shellPool.getShellOutput("300");

        //then
        Assert.assertEquals("", output);
    }

    @Test
    public void testGetShellResult_returnsStatusError_ifErrorStreamNotEmpty() throws Exception {

        //given
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.addOutputLine("output string");
        ArrayList<String> errorOutput = new ArrayList<>();
        errorOutput.add("Error output");
        Whitebox.setInternalState(shellCommand, "errorOutput", errorOutput);
        Shell shell = new Shell();
        Whitebox.setInternalState(shell, "command", shellCommand);
        shell.setLockId("100");
        ArrayList<Shell> shellPoolList = new ArrayList<>();
        shellPoolList.add(shell);
        ShellPool shellPool = new ShellPool();
        Whitebox.setInternalState(shellPool, "shellPool", shellPoolList);

        //when
        ShellResult shellResult = shellPool.getShellResult("100");

        //then
        Assert.assertEquals(STATUS_ERROR, shellResult.getStatus());

    }

    @Test(expected = ShellPoolException.class)
    public void testGetShellResult_throwsException_ifLockIdNotFound() throws Exception {

        //given
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.addOutputLine("output string");
        Shell shell = new Shell();
        Whitebox.setInternalState(shell, "command", shellCommand);
        shell.setLockId("100");
        ArrayList<Shell> shellPoolList = new ArrayList<>();
        shellPoolList.add(shell);
        ShellPool shellPool = new ShellPool();
        Whitebox.setInternalState(shellPool, "shellPool", shellPoolList);

        //when
        ShellResult result = shellPool.getShellResult("300");

        //then
        //exception is thrown
    }

//    @Ignore
//    public void runSingleCommand() throws Exception {
//
//        // given
//        String instanceId = "myInstanceId";
//        ShellCommand command = new ShellCommand("echo $?;sleep(2);echo \"END_OF_SCRIPT\"");
//        String shellConsoleFile = "";
//
//        // when
//        ShellStatus res = pool.runCommand(command, instanceId, shellConsoleFile);
//
//        // then
//        assertEquals(ShellStatus.RUNNING, res);
//        assertEquals(1, pool.shellPool.size());
//        assertEquals(99, pool.shellctrl.availablePermits());
//    }
//
//    @Ignore
//    public void runSingleCommand_readShellOutput() throws Exception {
//        // given
//        String instanceId = "myInstanceId";
//        ShellCommand command = new ShellCommand("echo $?;sleep(2);echo \"END_OF_SCRIPT\"");
//        pool.runCommand(command, instanceId, "");
//
//        // when
//        ShellStatus res;
//        do {
//            res = pool.consumeShellOutput(instanceId);
//        } while (res == ShellStatus.RUNNING);
//
//        // then
//        pool.unlockShell(instanceId);
//        assertEquals(1, pool.shellPool.size());
//        assertEquals(99, pool.shellctrl.availablePermits());
//        assertEquals(ShellStatus.SUCCESS, res);
//    }
//
//    @Ignore
//    public void exitShellScript() throws Exception {
//        String instanceId = "myInstanceId";
//        ShellCommand command = new ShellCommand();
//        command.init("sleep(1);exit;");
//        String shellConsoleFile = "";
//
//        // when
//        pool.runCommand(command, instanceId, shellConsoleFile);
//        ShellStatus res = pool.consumeShellOutput(instanceId);
//
//        // then
//        assertEquals(ShellStatus.RUNNING, res);
//        pool.terminateShell(instanceId);
//        assertEquals(0, pool.shellPool.size());
//        assertEquals(100, pool.shellctrl.availablePermits());
//    }
//
//    @Ignore
//    public void useDifferentShells() throws Exception {
//        // given
//        ShellCommand cmd1 = new ShellCommand("echo $?;\r\nsleep(5);\r\necho \"END_OF_SCRIPT\"");
//        ShellCommand cmd2 = new ShellCommand("echo $?;\r\nsleep(6);\r\necho \"END_OF_SCRIPT\"");
//        ShellCommand cmd3 = new ShellCommand("echo $?;\r\nsleep(7);\r\necho \"END_OF_SCRIPT\"");
//        String consoleFile = "";
//        pool.runCommand(cmd1, "id1", consoleFile);
//        pool.runCommand(cmd2, "id2", consoleFile);
//        pool.runCommand(cmd3, "id3", consoleFile);
//
//        // when
//        long ref = System.currentTimeMillis();
//        long runtime = 0;
//        boolean done = false;
//        do {
//            ShellStatus res1 = pool.consumeShellOutput("id1");
//            assertTrue(res1 == SUCCESS || res1 == RUNNING);
//
//            ShellStatus res2 = pool.consumeShellOutput("id2");
//            assertTrue(res2 == SUCCESS || res2 == RUNNING);
//
//            ShellStatus res3 = pool.consumeShellOutput("id3");
//            assertTrue(res3 == SUCCESS || res3 == RUNNING);
//
//            done = (res1 == SUCCESS) && (res2 == SUCCESS) && (res3 == SUCCESS);
//            // done = (res1 == SUCCESS);
//            runtime = System.currentTimeMillis() - ref;
//        } while (!done && runtime < 20000);
//
//        // then
//        assertTrue(runtime < 20000);
//        assertEquals(3, pool.shellPool.size());
//        assertEquals(97, pool.shellctrl.availablePermits());
//    }
//
//    @Ignore
//    public void reuseShell() throws Exception {
//        ShellCommand cmd1 = new ShellCommand("echo $?;echo \"END_OF_SCRIPT\"");
//        ShellCommand cmd2 = new ShellCommand("echo $?;echo \"END_OF_SCRIPT\"");
//        String consoleFile = "";
//        ShellStatus res = pool.runCommand(cmd1, "id1", consoleFile);
//        assertEquals(ShellStatus.RUNNING, res);
//
//        do {
//            res = pool.consumeShellOutput("id1");
//            assertTrue(res == SUCCESS || res == RUNNING);
//        } while (res != SUCCESS);
//
//        pool.unlockShell("id1");
//
//        res = pool.runCommand(cmd2, "id2", consoleFile);
//        assertEquals(ShellStatus.RUNNING, res);
//
//        do {
//            res = pool.consumeShellOutput("id2");
//            assertTrue(res == SUCCESS || res == RUNNING);
//        } while (res != SUCCESS);
//
//    }
//
//    @Ignore
//    public void runFailingCommand() throws Exception {
//        ShellCommand cmd1 = new ShellCommand("unknown_command;echo \"END_OF_SCRIPT\"");
//        String consoleFile = "";
//        ShellStatus res = pool.runCommand(cmd1, "id1", consoleFile);
//
//        do {
//            res = pool.consumeShellOutput("id1");
//            assertTrue(res.name(), res == PSSHELL_ERROR || res == RUNNING);
//        } while (res != PSSHELL_ERROR);
//
//        pool.unlockShell("id1");
//    }

}

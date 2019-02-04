/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: 15.01.2019
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.oscm.app.shell.business.api.json.ShellResult;

import java.util.ArrayList;

import static org.oscm.app.shell.business.api.Shell.STATUS_ERROR;
import static org.oscm.app.shell.business.api.ShellStatus.SUCCESS;

public class ShellPoolTest {

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
        shellPool.getShellResult("300");

        //then
        //exception is thrown
    }

    @Test
    public void testConsumeShellOutput_returnsSuccess_ifReturnCodeSuccess() throws Exception {

        //given
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.setReturnCode(SUCCESS);
        shellCommand.addOutputLine("output string");
        Shell shell = new Shell();
        Whitebox.setInternalState(shell, "command", shellCommand);
        shell.setLockId("100");
        ArrayList<Shell> shellPoolList = new ArrayList<>();
        shellPoolList.add(shell);
        ShellPool shellPool = new ShellPool();
        Whitebox.setInternalState(shellPool, "shellPool", shellPoolList);

        //when
        ShellStatus result = shellPool.consumeShellOutput("100");

        //then
        Assert.assertEquals(SUCCESS, result);
    }

    @Test(expected = ShellPoolException.class)
    public void testConsumeShellOutput_throwsException_ifLockIdNotFound() throws Exception {

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
        shellPool.consumeShellOutput("300");

        //then
        //exception is thrown
    }

    @Test
    public void testGetShellErrorOutput_returnsOutput_ifFoundShellForLockId() throws Exception {

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
        String result = shellPool.getShellErrorOutput("100");

        //then
        Assert.assertEquals("Error output\n\t", result);
    }

    @Test
    public void testGetShellErrorOutput_returnsEmptyString_ifLockIdNotFound() throws Exception {

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
        String result = shellPool.getShellErrorOutput("200");

        //then
        Assert.assertEquals("", result);
    }

}
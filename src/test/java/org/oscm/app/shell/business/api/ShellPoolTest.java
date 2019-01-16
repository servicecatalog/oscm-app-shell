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

import java.util.ArrayList;

import static org.oscm.app.shell.business.api.Shell.STATUS_ERROR;

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
        ShellResult result = shellPool.getShellResult("300");

        //then
        //exception is thrown
    }
}

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 20, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.actions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.shell.business.api.json.ShellResultParameter;
import org.oscm.app.shell.business.script.Script;
import org.oscm.app.shell.business.api.*;
import org.oscm.app.shell.business.api.json.ShellResult;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.oscm.app.shell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.shell.business.actions.StatemachineEvents.*;

public class ActionsTest {

    @InjectMocks
    @Spy
    Actions actions;

    @Mock
    ShellPool pool;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteScript_returnsStateExecuting_ifCommandIsRun() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        InstanceStatus status = null;
        Script script = mock(Script.class);

        when(pool.runCommand(any(ShellCommand.class), anyString(), anyString())).thenReturn(ShellStatus.RUNNING);

        //when
        String state = actions.executeScript(instanceId, settings, status, script);

        //then
        assertEquals("State returned is " + state, EXECUTING, state);
    }

    @Test
    public void testExecuteScript_returnsStateFailed_ifCommandThrowsException() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        InstanceStatus status = null;
        Script script = mock(Script.class);

        when(pool.runCommand(any(ShellCommand.class), anyString(), anyString())).thenThrow(ShellPoolException.class);

        //when
        String state = actions.executeScript(instanceId, settings, status, script);

        //then
        assertEquals("State returned is " + state, FAILED, state);
    }

    @Test
    public void testConsumeScriptOutput_returnsStateRun_ifShellIsRunning() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = null;
        InstanceStatus status = null;
        when(pool.consumeShellOutput(instanceId)).thenReturn(ShellStatus.RUNNING);

        //when
        String state = actions.consumeScriptOutput(instanceId, settings, status);

        //then
        assertEquals("State returned is " + state, RUN, state);
    }

    @Test
    public void testConsumeScriptOutput_returnsStateSuccess_ifShellIsSuccessAndResultIsOk() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = null;
        InstanceStatus instanceStatus = null;
        String status = "ok";
        String message = "Script was executed successfully";
        ShellResult shellResult = new ShellResult(status, message);

        when(pool.consumeShellOutput(instanceId)).thenReturn(ShellStatus.SUCCESS);
        when(pool.getShellResult(instanceId)).thenReturn(shellResult);

        //when
        String state = actions.consumeScriptOutput(instanceId, settings, instanceStatus);

        //then
        assertEquals("State returned is " + state, SUCCESS, state);
    }

    @Test
    public void testConsumeScriptOutput_returnsStateError_ifShellIsSuccessAndResultIsError() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        InstanceStatus instanceStatus = null;
        String status = "error";
        String message = "Script failed";
        ShellResult shellResult = new ShellResult(status, message);

        when(pool.consumeShellOutput(instanceId)).thenReturn(ShellStatus.SUCCESS);
        when(pool.getShellResult(instanceId)).thenReturn(shellResult);

        //when
        String state = actions.consumeScriptOutput(instanceId, settings, instanceStatus);

        //then
        assertEquals(message, settings.getParameters().get(SM_ERROR_MESSAGE.name()).getValue());
        assertEquals("State returned is " + state, ERROR, state);
    }

    @Test
    public void testConsumeScriptOutput_returnsStateError_ifShellIsError() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        InstanceStatus instanceStatus = null;
        String status = "error";
        String message = "Script failed";
        ShellResult shellResult = new ShellResult(status, message);

        when(pool.consumeShellOutput(instanceId)).thenReturn(ShellStatus.PSSHELL_ERROR);
        when(pool.getShellResult(instanceId)).thenReturn(shellResult);

        //when
        String state = actions.consumeScriptOutput(instanceId, settings, instanceStatus);

        //then
        assertEquals(message, settings.getParameters().get(SM_ERROR_MESSAGE.name()).getValue());
        assertEquals("State returned is " + state, ERROR, state);
    }

    @Test
    public void testFinalizeScriptExecution_returnsStateSuccess() throws Exception {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        InstanceStatus instanceStatus = new InstanceStatus();
        String status = "ok";
        String message = "Script was executed successfully";
        ShellResult shellResult = new ShellResult(status, message);
        HashSet<ShellResultParameter> parameters = new HashSet<>();
        parameters.add(new ShellResultParameter("KEY", "VALUE"));
        shellResult.setParameters(parameters);

        when(pool.consumeShellOutput(instanceId)).thenReturn(ShellStatus.PSSHELL_ERROR);
        when(pool.getShellResult(instanceId)).thenReturn(shellResult);

        //when
        String state = actions.finalizeScriptExecution(instanceId, settings, instanceStatus);

        //then
        verify(pool, times(1)).terminateShell(instanceId);
        assertTrue(instanceStatus.isReady());
        assertEquals("State returned is " + state, SUCCESS, state);
    }
}

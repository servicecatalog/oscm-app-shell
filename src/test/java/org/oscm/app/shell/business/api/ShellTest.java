/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 12, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.oscm.app.shell.business.api.json.ShellResult;
import org.oscm.app.shell.business.api.json.ShellResultData;
import org.oscm.app.shell.business.api.json.ShellResultUsageData;
import org.oscm.app.shell.business.script.ScriptType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.oscm.app.shell.business.api.Shell.STATUS_ERROR;
import static org.oscm.app.shell.business.api.Shell.STATUS_OK;
import static org.oscm.app.shell.business.api.ShellStatus.*;

public class ShellTest {

    @Test
    public void testUnlock() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId("100");

        //when
        shell.unlock();

        //then
        Assert.assertNull(shell.getLockId());
    }

    @Test
    public void testIsLocked_returnFalse_ifNotLocked() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId(null);

        //when
        boolean result = shell.isLocked();

        //then
        Assert.assertEquals(false, result);
    }

    @Test
    public void testIsLocked_returnTrue_ifLocked() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId("100");

        //when
        boolean result = shell.isLocked();

        //then
        Assert.assertEquals(true, result);
    }

    @Test
    public void testIsLockedFor_returnEmptyString_ifNotLocked() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId(null);

        //when
        String result = shell.isLockedFor();

        //then
        Assert.assertEquals("", result);
    }

    @Test
    public void testIsLockedFor_returnLockId_ifLocked() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId("100");

        //when
        String result = shell.isLockedFor();

        //then
        Assert.assertEquals("100", result);
    }

    @Test
    public void testLockShell_returnsFalse_ifLockIdIsNotNull() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId("100");

        //when
        boolean result = shell.lockShell("200");

        //then
        Assert.assertFalse(result);
    }

    @Test
    public void testLockShell_returnsTrue_ifLockIdIsNull() throws Exception {

        //given
        Shell shell = new Shell(ScriptType.SHELL);
        shell.setLockId(null);
        String lockId = "100";

        //when
        boolean result = shell.lockShell(lockId);

        //then
        Assert.assertEquals(shell.getLockId(), lockId);
        Assert.assertTrue(result);
    }

    @Test
    public void testRunCommand_returnsStdinClosed_ifFailedToWriteCommandToStdin() throws Exception {

        //given
        ShellCommand shellCommand = mock(ShellCommand.class);
        BufferedWriter bufferedWriter = mock(BufferedWriter.class);
        Shell shell = new Shell(ScriptType.SHELL);
        Whitebox.setInternalState(shell, "stdIn", bufferedWriter);
        shell.setLockId("100");

        //when
        doThrow(new IOException()).when(bufferedWriter).write(anyString());
        ShellStatus shellStatus = shell.runCommand("100", shellCommand);

        //then
        Assert.assertEquals(STDIN_CLOSED.toString(), shellStatus.toString());
    }

    @Test
    public void testRunCommand_returnsCallerIdDoesNotMatch_ifShellLockError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok.sh");
        ShellCommand shellCommand = mock(ShellCommand.class);
        Shell shell = runScript(scriptContent, instanceId);
        shell.setLockId("100");

        //when
        ShellStatus shellStatus = shell.runCommand("200", shellCommand);

        //then
        Assert.assertEquals(CALLERID_DOES_NOT_MATCH.toString(), shellStatus.toString());
    }

    @Test
    public void testClose_interruptsStdOutAndStdErr_ifRunsProperly() throws Exception {

        //given
        BufferedWriter stdInMock = mock(BufferedWriter.class);
        StreamGobbler stdOutMock = mock(StreamGobbler.class);
        StreamGobbler stdErrMock = mock(StreamGobbler.class);
        Shell shell = new Shell(ScriptType.SHELL);
        Whitebox.setInternalState(shell, "stdIn", stdInMock);
        Whitebox.setInternalState(shell, "stdOut", stdOutMock);
        Whitebox.setInternalState(shell, "stdErr", stdErrMock);

        //when
        doNothing().when(stdInMock).write(anyString());
        doNothing().when(stdInMock).flush();
        shell.close();

        //then
        verify(stdInMock, times(1)).close();
        verify(stdOutMock, times(1)).interrupt();
        verify(stdErrMock, times(1)).interrupt();
    }

    @Test
    public void testClose_ignoresCatch_ifStdInThrowsIOException() throws Exception {

        //given
        BufferedWriter stdInMock = mock(BufferedWriter.class);
        StreamGobbler stdOutMock = mock(StreamGobbler.class);
        StreamGobbler stdErrMock = mock(StreamGobbler.class);
        Shell shell = new Shell(ScriptType.SHELL);
        Whitebox.setInternalState(shell, "stdIn", stdInMock);
        Whitebox.setInternalState(shell, "stdOut", stdOutMock);
        Whitebox.setInternalState(shell, "stdErr", stdErrMock);

        //when
        doThrow(new IOException()).when(stdInMock).write(anyString());
        shell.close();

        //then
        verify(stdInMock, times(1)).close();
        verify(stdOutMock, times(1)).interrupt();
        verify(stdErrMock, times(1)).interrupt();
    }

    @Test
    public void testGetResult_isOk_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        ShellResult result = shell.getResult();

        //then
        assertEquals(STATUS_OK, result.getStatus());
    }


    @Test
    public void testGetResult_isOkWithHtmlData_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok_html.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        ShellResult result = shell.getResult();

        //then
        String status = result.getStatus();
        Optional<ShellResultData> data = result.getData();

        assertEquals("Failed as resulting status is " + status, STATUS_OK, status);
        assertTrue("Failed as resulting data is " + data, data.get().getOutput().contains("<table style="));
    }

    @Test
    public void testGetResult_isOkWithUsageData_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok_usage_data.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        ShellResult result = shell.getResult();

        //then
        String status = result.getStatus();
        Set<ShellResultUsageData> usageData = result.getUsageData();

        assertEquals("Failed as resulting status is " + status, STATUS_OK, status);
        assertFalse("Failed as resulting usageData is " + usageData, usageData.isEmpty());
    }

    @Test
    public void testGetResult_isError_ifReturnedDataIsError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        ShellResult result = shell.getResult();

        //then
        assertEquals(STATUS_ERROR, result.getStatus());
    }

    @Test
    public void testGetResult_isErrorWithMessage_ifErrorWhenExecuting() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error_execution.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        ShellResult result = shell.getResult();

        //then
        assertEquals(STATUS_ERROR, result.getStatus());
        assertEquals(shell.getErrorOutput(), result.getMessage());
    }

    @Test(expected = ShellResultException.class)
    public void testGetResult_throwsException_ifReturnedDataIsInvalid() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_invalid.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        shell.getResult();

        //then
        //expected Exception
    }

    @Test
    public void testGetOutput_isOk_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        String output = shell.getOutput();

        //then
        assertTrue(output.contains("\"status\":\"ok\""));
    }

    @Test
    public void testGetOutput_isError_ifReturnedDataIsError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        String output = shell.getOutput();

        //then
        assertTrue(output.contains("\"status\":\"error\""));
    }

    @Test
    public void testGetErrorOutput_isEmpty_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        String output = shell.getErrorOutput();

        //then
        assertTrue(output.isEmpty());
    }

    @Test
    public void testGetErrorOutput_isNotEmpty_ifErrorWhenExecution() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error_execution.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        String output = shell.getErrorOutput();

        //then
        assertFalse(output.isEmpty());
    }


    private Shell runScript(String scriptContent, String instanceId) throws Exception {

        Shell shell = new Shell(ScriptType.SHELL);
        ShellCommand command = new ShellCommand(scriptContent);

        shell.lockShell(instanceId);
        shell.runCommand(instanceId, command);

        ShellStatus status;
        do {
            status = shell.consumeOutput(instanceId);
            Thread.sleep(1000);
        } while (status == RUNNING);

        return shell;
    }

    private String getScriptContent(String fileName) throws Exception {

        String lineSeparator = System.getProperty("line.separator");
        URI uri = this.getClass().getClassLoader().getResource(fileName).toURI();
        Path path = Paths.get(uri);
        String content = Files.lines(path).collect(Collectors.joining(lineSeparator));

        return content;
    }
}

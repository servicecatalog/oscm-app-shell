/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 12, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.junit.Test;
import org.richfaces.json.JSONException;

import javax.json.JsonObject;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.oscm.app.shell.business.api.Shell.*;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

public class ShellTest {

    @Test
    public void testGetResult_isOk_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_OK, result.getString(JSON_STATUS));
    }

    @Test
    public void testGetResult_isOkWithHtmlData_ifReturnedDataIsOk() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_ok_html.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_OK, result.getString(JSON_STATUS));
        assertTrue(result.getString(JSON_DATA).contains("<table>"));
    }

    @Test
    public void testGetResult_isError_ifReturnedDataIsError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_ERROR, result.getString(JSON_STATUS));
    }

    @Test
    public void testGetResult_isErrorWithMessage_ifErrorWhenExecuting() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error_execution.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_ERROR, result.getString(JSON_STATUS));
        assertEquals(shell.getErrorOutput(), result.getString(JSON_MESSAGE));
    }

    @Test(expected = JSONException.class)
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
    public void testGetErrorOutput_isError_ifReturnedDataIsError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/sample_error.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        String output = shell.getErrorOutput();
        System.out.println(output);

        //then
        assertTrue(output.contains("\"status\":\"error\""));
    }

    private Shell runScript(String scriptContent, String instanceId) throws Exception {

        Shell shell = new Shell();
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

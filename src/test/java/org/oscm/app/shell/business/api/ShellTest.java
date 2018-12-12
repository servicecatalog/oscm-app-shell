/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 12, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.oscm.app.shell.business.api.Shell.*;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

public class ShellTest {

    @Test
    public void testGetResultReturnedSuccess() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/status_success.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_OK, result.getString(JSON_STATUS));
    }

    @Test
    public void testGetResultReturnedError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/status_error.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_ERROR, result.getString(JSON_STATUS));
    }

    @Test
    public void testGetResultReturnedExecutionError() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("sample_scripts/status_execution_error.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals(STATUS_ERROR, result.getString(JSON_STATUS));
        assertEquals(shell.getErrorOutput(), result.getString(JSON_MESSAGE));
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

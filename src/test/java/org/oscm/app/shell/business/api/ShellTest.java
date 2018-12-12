package org.oscm.app.shell.business.api;

import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

public class ShellTest {

    @Test
    public void testGetResult() throws Exception {

        //given
        String instanceId = "Instance_1236678329433";
        String scriptContent = getScriptContent("status.sh");
        Shell shell = runScript(scriptContent, instanceId);

        //when
        JsonObject result = shell.getResult();

        //then
        assertEquals("success", result.getString("status"));
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

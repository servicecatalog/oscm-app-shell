/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 01.02.2019
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api.json;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShellResultTest {

    private static final String STATUS_OK = "ok";
    private static final String STATUS_MESSAGE = "Script executed successfully";

    private static final String DATA_OUTPUT = "Custom output for the status tab";
    private static final String DATA_ACCESS_INFO = "http://accessInfo.url";

    private static final String EVENT_DISK = "EVENT_DISK_GIGABYTE_HOURS";
    private static final String EVENT_CPU = "EVENT_CPU_HOURS";
    private static final String EVENT_RAM = "EVENT_RAM_MEGABYTE_HOURS";

    @Test
    public void testShellResult_isValidData_whenJsonIsValid() throws Exception {

        //when
        String jsonOutput = getJSONFromFile("sample_scripts/valid.json");

        //when
        Gson json = new Gson();
        ShellResult shellResult = json.fromJson(jsonOutput, ShellResult.class);
        Set<ShellResultUsageData> usageData = shellResult.getUsageData();
        ShellResultData data = shellResult.getData().get();

        //then
        assertEquals(STATUS_OK, shellResult.getStatus());
        assertEquals(STATUS_MESSAGE, shellResult.getMessage());
        assertEquals(DATA_OUTPUT, data.getOutput());
        assertEquals(DATA_ACCESS_INFO, data.getAccessInfo());
        assertEquals(3, usageData.size());
        assertTrue(usageData.stream().anyMatch(event -> EVENT_CPU.equals(event.getEventId())));
        assertTrue(usageData.stream().anyMatch(event -> EVENT_DISK.equals(event.getEventId())));
        assertTrue(usageData.stream().anyMatch(event -> EVENT_RAM.equals(event.getEventId())));
    }

    @Test
    public void testShellResult_usageDataEventAreUnique_whenJsonContainsSameEvents() throws Exception {

        //when
        String jsonOutput = getJSONFromFile("sample_scripts/validUniqueUsage.json");

        //when
        Gson json = new Gson();
        ShellResult shellResult = json.fromJson(jsonOutput, ShellResult.class);
        Set<ShellResultUsageData> usageData = shellResult.getUsageData();

        //then
        assertEquals(1, usageData.size());
        assertTrue(usageData.stream().allMatch(event -> EVENT_DISK.equals(event.getEventId())));
        assertTrue(usageData.stream().allMatch(event -> 100 == event.getMultiplier()));
    }

    private String getJSONFromFile(String fileName) throws IOException, URISyntaxException {

        String lineSeparator = System.getProperty("line.separator");
        URI uri = this.getClass().getClassLoader().getResource(fileName).toURI();
        Path path = Paths.get(uri);
        String content = Files.lines(path).collect(Collectors.joining(lineSeparator));

        return content;
    }

}


/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.apache.commons.io.IOUtils;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeSet;

import static java.lang.String.join;
import static org.oscm.app.shell.business.ConfigurationKey.OPERATIONS_ID;

public class Script {

    private static final Logger LOG = LoggerFactory.getLogger(Script.class);
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String LOCAL_SCRIPT_LOCATION = "/opt/scripts/";
    private static final String EXTERNAL_SCRIPT_PREFIXES = "http:https";

    private String content;
    private String path;
    private boolean external;

    public Script(String scriptPath) {

        if (isExternalPath(scriptPath)) {
            path = scriptPath;
            external = true;
        } else {
            path = LOCAL_SCRIPT_LOCATION + scriptPath;
            external = false;
        }
    }

    public void loadContent() throws Exception {
        if (isExternal()) {
            content = loadExternalScript(path);
        } else {
            content = loadLocalScript(path);
        }
    }

    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }

    public boolean isExternal() {
        return external;
    }

    public String loadLocalScript(String pathfile) throws Exception {
        return new String(Files.readAllBytes(Paths.get(pathfile)));
    }

    public String loadExternalScript(String url) throws Exception {
        //TODO: load script content from external url (check downloadFile method)
        return null;
    }

    private String downloadFile(String url) throws Exception {

        HttpURLConnection conn = null;
        StringWriter writer = new StringWriter();

        try {
            URL urlSt = new URL(url);
            conn = (HttpURLConnection) urlSt.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestMethod("GET");
            try (InputStream in = conn.getInputStream();) {
                IOUtils.copy(in, writer, "UTF-8");
            }
        } catch (Exception e) {
            LOG.error("Failed to download content file " + url, e);
            throw new Exception("Failed to download content file " + url);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
            throw new Exception("Failed to download content file " + url);
        }

        return writer.toString();
    }

    public void insertServiceParameters(ProvisioningSettings settings) {

        LOG.trace("Script before patching:\n" + content);

        TreeSet<String> parameters = new TreeSet<>();

        HashMap<String, Setting> params = settings.getParameters();
        for (String key : params.keySet()) {
            parameters.add(buildParameterCommand(key, params.get(key).getValue()));
        }
        parameters.add(buildParameterCommand("SUBSCRIPTION_ID", settings.getSubscriptionId()));
        if (settings.getRequestingUser() != null) {
            parameters.add(buildParameterCommand("REQUESTING_USER", settings.getRequestingUser().getUserId()));
        }
        parameters.add(buildParameterCommand("REQUESTING_ORGANIZATION_ID", settings.getOrganizationId()));
        parameters.add(buildParameterCommand("REFERENCE_ID", settings.getReferenceId()));

        String firstLine = content.substring(0, content.indexOf(NEW_LINE));
        String rest = content.substring(content.indexOf(NEW_LINE) + 1, content.length());
        String patchedScript = join("", parameters) + NEW_LINE + firstLine + NEW_LINE + rest;

        LOG.trace("Patched content:\n" + patchedScript);
        content = patchedScript;
    }

    public void insertOperationId(Configuration config) {

        String firstLine = content.substring(0, content.indexOf(NEW_LINE));
        String rest = content.substring(content.indexOf(NEW_LINE) + 1, content.length());
        content = buildParameterCommand("OPERATION", config.getSetting(OPERATIONS_ID)) + NEW_LINE + firstLine + NEW_LINE
                + rest;
    }

    private String buildParameterCommand(String key, String value) {
        return key + "=\"" + value + "\"" + NEW_LINE;
    }

    private boolean isExternalPath(String scriptPath) {

        Optional<String> optionalUrl = Arrays.stream(EXTERNAL_SCRIPT_PREFIXES.split(":"))
                .filter(prefix -> scriptPath.startsWith(prefix))
                .findAny();

        return optionalUrl.isPresent();
    }
}

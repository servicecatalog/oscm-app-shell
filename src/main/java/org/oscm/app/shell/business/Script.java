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

import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeSet;

import static java.lang.String.join;
import static org.oscm.app.shell.business.ConfigurationKey.OPERATIONS_ID;

public class Script {

    private static final Logger LOG = LoggerFactory.getLogger(Script.class);

    private static final String NEW_LINE = System.getProperty("line.separator");

    String script;

    public Script() {

    }

    public Script(String filename) throws Exception {
        if (filename.startsWith("http")) {
            script = downloadFile(filename);
        } else {
            script = loadFile(filename);
        }
    }

    public String get() {
        return script;
    }

    String loadFile(String filename) throws Exception {
        return new String(Files.readAllBytes(Paths.get(filename)));
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
            LOG.error("Failed to download script file " + url, e);
            throw new Exception("Failed to download script file " + url);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
            throw new Exception("Failed to download script file " + url);
        }

        return writer.toString();
    }

    public void insertServiceParameters(ProvisioningSettings settings) throws Exception {

        LOG.trace("Script before patching:\n" + script);

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

        String firstLine = script.substring(0, script.indexOf(NEW_LINE));
        String rest = script.substring(script.indexOf(NEW_LINE) + 1, script.length());
        String patchedScript = join("", parameters) + NEW_LINE + firstLine + NEW_LINE + rest;

        LOG.trace("Patched script:\n" + patchedScript);
        script = patchedScript;
    }

    public void insertOperationId(Configuration config) {

        String firstLine = script.substring(0, script.indexOf(NEW_LINE));
        String rest = script.substring(script.indexOf(NEW_LINE) + 1, script.length());
        script = buildParameterCommand("OPERATION", config.getSetting(OPERATIONS_ID)) + NEW_LINE + firstLine + NEW_LINE
                + rest;
    }

    private String buildParameterCommand(String key, String value) {
        return key + "=\"" + value + "\"" + NEW_LINE;
    }
}

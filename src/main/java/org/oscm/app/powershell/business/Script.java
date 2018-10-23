/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.powershell.business;

import static java.lang.String.join;
import static org.oscm.app.powershell.business.ConfigurationKey.INSTANCE_ID;
import static org.oscm.app.powershell.business.ConfigurationKey.OPERATIONS_ID;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.oscm.app.data.ProvisioningSettings;
import org.oscm.app.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	int returnErrorCode = HttpURLConnection.HTTP_OK;
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
	    returnErrorCode = conn.getResponseCode();
	} catch (Exception e) {
	    LOG.error("Failed to download script file " + url, e);
	    throw new Exception("Failed to download script file " + url);
	} finally {
	    if (conn != null) {
		conn.disconnect();
	    }
	}

	if (HttpURLConnection.HTTP_OK != returnErrorCode) {
	    throw new Exception("Failed to download script file " + url);
	}

	return writer.toString();
    }

    public void insertServiceParameter(ProvisioningSettings settings) throws Exception {
	LOG.trace("Script before patching:\n" + script);
	Configuration config = new Configuration(settings);

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
	parameters.add(buildParameterCommand("INSTANCE_ID", config.getSetting(INSTANCE_ID)));

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
	return "$" + key + "=\"" + value + "\"" + NEW_LINE;
    }
}

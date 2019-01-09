/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

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

    public String loadLocalScript(String pathfile) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(pathfile)).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        } catch (FileNotFoundException e) {
            LOG.error("Failed to load local script from " + pathfile + " - file not found. " + e );
            throw e;
        }
    }

    public String loadExternalScript(String url) throws Exception {
        try (Scanner scanner = new Scanner(getConnectionStream(url),"UTF-8").useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            if (url.startsWith("https")) {
                LOG.error("Failed to download content file " + url + " " + e);
                throw new Exception("Failed to load script from URL. The server might be " +
                        "unreachable or the SSL certificate is not trusted. Exception: " +
                        e.getMessage());
            } else {
                LOG.error("Failed to download content file " + url + " " + e);
                throw e;
            }
        }
    }

    private InputStream getConnectionStream(String url) throws Exception {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            return httpResponse.getEntity().getContent();
        } catch (Exception e) {
            LOG.error("Failed to open connection stream to resource: " + url);
            throw e;
        }
    }

    public void insertProvisioningSettings(ProvisioningSettings settings) {

        LOG.trace("Script before patching:\n" + content);

        TreeSet<String> parameters = new TreeSet<>();

        insertBasicParameters(parameters, settings);
        insertServiceParameters(parameters,settings);
        insertUDAs(parameters, settings);

        String firstLine = content.substring(0, content.indexOf(NEW_LINE));
        String rest = content.substring(content.indexOf(NEW_LINE) + 1, content.length());
        String patchedScript = join("", parameters) + NEW_LINE + firstLine + NEW_LINE + rest;

        LOG.trace("Patched content:\n" + patchedScript);
        content = patchedScript;
    }

    private void insertBasicParameters(TreeSet<String> scriptParameters,
                                       ProvisioningSettings settings){

        scriptParameters.add(buildParameterCommand("SUBSCRIPTION_ID", settings.getSubscriptionId()));
        if (settings.getRequestingUser() != null) {
            scriptParameters.add(buildParameterCommand("REQUESTING_USER",
                    settings.getRequestingUser().getUserId()));
        }
        scriptParameters.add(buildParameterCommand("REQUESTING_ORGANIZATION_ID",
                settings.getOrganizationId()));
        scriptParameters.add(buildParameterCommand("REFERENCE_ID", settings.getReferenceId()));
    }

    private void insertServiceParameters(TreeSet<String> scriptParameters,
                                         ProvisioningSettings settings){

        HashMap<String, Setting> params = settings.getParameters();

        for (String key : params.keySet()) {
            String parameterCommand = buildParameterCommand(key, params.get(key).getValue());
            scriptParameters.add(parameterCommand);
        }
    }

    private void insertUDAs(TreeSet<String> scriptParameters, ProvisioningSettings settings){

        HashMap<String, Setting> params = settings.getAttributes();

        for (String key : params.keySet()) {
            String parameterCommand = buildParameterCommand(key, params.get(key).getValue());
            scriptParameters.add(parameterCommand);
        }
    }

    public void insertOperationId(Configuration config) {

        String firstLine = content.substring(0, content.indexOf(NEW_LINE));
        String rest = content.substring(content.indexOf(NEW_LINE) + 1, content.length());
        content = buildParameterCommand("OPERATION", config.getSetting(OPERATIONS_ID)) +
                NEW_LINE + firstLine + NEW_LINE + rest;
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

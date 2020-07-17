/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 2016-05-24
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.shell.business.script;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static java.lang.String.join;
import static org.oscm.app.shell.business.ConfigurationKey.OPERATIONS_ID;

public abstract class Script {

  private static final Logger LOGGER = LoggerFactory.getLogger(Script.class);
  private static final String LOCAL_SCRIPT_LOCATION = "/opt/scripts/";
  private static final String EXTERNAL_SCRIPT_PREFIXES = "http:https";

  static final String NEW_LINE = System.getProperty("line.separator");

  private String scriptContent;
  private String scriptPath;
  private boolean external;

  String scriptFile;
  ScriptType scriptType;

  public static Script getInstance(String scriptFile) throws Exception {

    if (scriptFile.endsWith("sh")) {
      return new ShellScript(scriptFile);
    } else if (scriptFile.endsWith("ps1")) {
      return new PowerShellScript(scriptFile);
    } else {
      throw new Exception("Script type of " + scriptFile + " not supported");
    }
  }

  public void initialize() throws Exception {
    if (isExternalPath(scriptPath)) {
      this.external = true;
      this.scriptContent = loadExternalScript(scriptPath);
    } else {
      this.scriptPath = LOCAL_SCRIPT_LOCATION + scriptPath;
      this.external = false;
      this.scriptContent = loadLocalScript(scriptPath);
    }
  }

  public String getScriptContent() {
    return scriptContent;
  }

  public String getScriptPath() {
    return scriptPath;
  }

  public ScriptType getScriptType() {
    return scriptType;
  }

  public boolean isExternal() {
    return external;
  }

  public String getScriptActionType(ProvisioningSettings settings) throws Exception {

    String smStateMachine = settings.getParameters().get("SM_STATE_MACHINE").getValue();
    String actionType;
    switch (smStateMachine) {
      case "assign_user.xml":
        actionType = "ASSIGN_USER_SCRIPT";
        break;
      case "deassign_user.xml":
        actionType = "DEASSIGN_USER_SCRIPT";
        break;
      case "deprovision.xml":
        actionType = "DEPROVISIONING_SCRIPT";
        break;
      case "operation.xml":
        actionType = "OPERATION_SCRIPT";
        break;
      case "provision.xml":
        actionType = "PROVISIONING_SCRIPT";
        break;
      case "update.xml":
        actionType = "UPDATE_SCRIPT";
        break;
      case "update_user.xml":
        actionType = "UPDATE_USER_SCRIPT";
        break;
      default:
        actionType = "UNRESOLVED";
        LOGGER.error("SM_STATE_MACHINE not recognizable!");
        throw new Exception("SM_STATE_MACHINE was not recognized!");
    }
    return actionType;
  }

  private String loadLocalScript(String pathfile) throws FileNotFoundException {
    try (Scanner scanner = new Scanner(new File(pathfile)).useDelimiter("\\A")) {
      return scanner.hasNext() ? scanner.next() : "";
    } catch (FileNotFoundException e) {
      LOGGER.error("Failed to load local script from " + pathfile + " - file not found. " + e);
      throw e;
    }
  }

  private String loadExternalScript(String url) throws Exception {
    try (Scanner scanner = new Scanner(getConnectionStream(url), "UTF-8").useDelimiter("\\A")) {
      return scanner.hasNext() ? scanner.next() : "";
    } catch (Exception e) {
      if (url.startsWith("https")) {
        LOGGER.error("Failed to download content file " + url + " " + e);
        throw new Exception(
            "Failed to load script from URL. The server might be "
                + "unreachable or the SSL certificate is not trusted. Exception: "
                + e.getMessage());
      } else {
        LOGGER.error("Failed to download content file " + url + " " + e);
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
      LOGGER.error("Failed to open connection stream to resource: " + url);
      throw e;
    }
  }

  public void insertProvisioningSettings(ProvisioningSettings settings) {

    LOGGER.trace("Script before patching:\n" + scriptContent);

    TreeSet<String> parameters = new TreeSet<>();

    insertBasicParameters(parameters, settings);
    addToScriptParameters(parameters, settings.getParameters());
    addToScriptParameters(parameters, settings.getAttributes());
    addToScriptParameters(parameters, settings.getCustomAttributes());

    String firstLine = scriptContent.substring(0, scriptContent.indexOf(NEW_LINE));
    String rest = scriptContent.substring(scriptContent.indexOf(NEW_LINE) + 1);
    String patchedScript = join("", parameters) + NEW_LINE + firstLine + NEW_LINE + rest;

    LOGGER.trace("Patched content:\n" + patchedScript);
    scriptContent = patchedScript;
  }

  private void insertBasicParameters(
      TreeSet<String> scriptParameters, ProvisioningSettings settings) {

    scriptParameters.add(buildParameterCommand("SUBSCRIPTION_ID", settings.getSubscriptionId()));
    if (settings.getRequestingUser() != null) {
      scriptParameters.add(
          buildParameterCommand("REQUESTING_USER", settings.getRequestingUser().getUserId()));
    }
    scriptParameters.add(
        buildParameterCommand("REQUESTING_ORGANIZATION_ID", settings.getOrganizationId()));
    scriptParameters.add(
        buildParameterCommand("REQUESTING_ORGANIZATION_NAME", settings.getOrganizationName()));
    scriptParameters.add(buildParameterCommand("REFERENCE_ID", settings.getReferenceId()));
  }

  private void addToScriptParameters(
      TreeSet<String> scriptParameters, HashMap<String, Setting> settings) {

    settings.keySet().stream()
        .map(key -> buildParameterCommand(key, settings.get(key).getValue()))
        .forEach(scriptParameters::add);
  }

  public void insertOperationId(Configuration config) {

    String firstLine = scriptContent.substring(0, scriptContent.indexOf(NEW_LINE));
    String rest = scriptContent.substring(scriptContent.indexOf(NEW_LINE) + 1);
    scriptContent =
        buildParameterCommand("OPERATION", config.getSetting(OPERATIONS_ID))
            + NEW_LINE
            + firstLine
            + NEW_LINE
            + rest;
  }

  private boolean isExternalPath(String scriptPath) {

    Optional<String> optionalUrl =
        Arrays.stream(EXTERNAL_SCRIPT_PREFIXES.split(":")).filter(scriptPath::startsWith).findAny();

    return optionalUrl.isPresent();
  }

  public abstract String buildParameterCommand(String key, String value);
}

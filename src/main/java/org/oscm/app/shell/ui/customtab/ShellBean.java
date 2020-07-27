/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2017-10-17
 *
 *******************************************************************************/
package org.oscm.app.shell.ui.customtab;

import org.apache.commons.codec.binary.Base64;
import org.oscm.app.shell.business.api.Shell;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellStatus;
import org.oscm.app.shell.business.api.json.ShellResult;
import org.oscm.app.shell.business.api.json.ShellResultData;
import org.oscm.app.shell.business.script.Script;
import org.oscm.app.shell.business.script.ScriptType;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;

import static org.oscm.app.shell.business.Configuration.CONTROLLER_ID;
import static org.oscm.app.shell.business.api.Shell.STATUS_OK;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

/** Bean for showing server information. */
@ManagedBean(name = "shellBean")
@ViewScoped
public class ShellBean implements Serializable {

  private static final long serialVersionUID = -5835894219559699861L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ShellBean.class);
  private static final String EMPTY_JSON_DATA = "Empty 'data' field in resulting json";

  private String subscriptionId;
  private String organizationId;
  private String instanceId;

  @PostConstruct
  public void init() {

    String lang =
        FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lang");

    if (lang != null) {
      FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.forLanguageTag(lang));
    }
  }

  public String getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = new String(Base64.decodeBase64(subscriptionId));
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = new String(Base64.decodeBase64(organizationId));
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = new String(Base64.decodeBase64(instanceId));
  }

  public String getStatus() {

    Shell shell = null;
    try {
      ProvisioningSettings settings = getProvisioningSettings();
      String statusScript = settings.getParameters().get("CHECK_STATUS_SCRIPT").getValue();
      Script script = Script.getInstance(statusScript);
      script.initialize();
      script.insertProvisioningSettings(settings);
      ScriptType scriptType = script.getScriptType();
      LOGGER.debug("Status script details: " + script.getScriptContent());

      ShellCommand command = new ShellCommand();
      command.init(script.getScriptContent());
      command.setScriptType(scriptType);

      shell = new Shell(scriptType);
      shell.lockShell(instanceId);
      shell.runCommand(instanceId, command);

      ShellStatus status;
      do {
        status = shell.consumeOutput(instanceId);
        Thread.sleep(1000);
      } while (status == RUNNING);

      ShellResult result = shell.getResult();

      LOGGER.debug("Status script result: " + result.getStatus());
      LOGGER.debug("Status script message: " + result.getMessage());

      if (STATUS_OK.equals(result.getStatus())) {
        Optional<ShellResultData> data = result.getData();
        if (data.isPresent()) {
          return data.get().getOutput();
        }

        LOGGER.error(EMPTY_JSON_DATA);
        return EMPTY_JSON_DATA;
      } else {
        return result.getMessage();
      }

    } catch (Exception e) {
      LOGGER.error(
          String.format(
              "Failed to get status of shell provisioning. orgId: %s, instanceId: %s, subId: %s",
              organizationId, instanceId, subscriptionId),
          e);
      return "Failed to get status of shell provisioning: " + e.getMessage();
    } finally {
      if (shell != null) {
        shell.close();
      }
    }
  }

  private ProvisioningSettings getProvisioningSettings() throws Exception {
    APPlatformService app = APPlatformServiceFactory.getInstance();
    return app.getServiceInstanceDetails(CONTROLLER_ID, instanceId, subscriptionId, organizationId);
  }
}

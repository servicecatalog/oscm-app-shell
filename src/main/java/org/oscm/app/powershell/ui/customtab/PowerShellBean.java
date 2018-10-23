/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  AWS controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2013-10-17                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.powershell.ui.customtab;

import static org.oscm.app.powershell.business.Configuration.CONTROLLER_ID;
import static org.oscm.app.powershell.business.api.PowershellStatus.RUNNING;
import static org.oscm.app.powershell.business.api.PowershellStatus.SUCCESS;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.codec.binary.Base64;
import org.oscm.app.powershell.business.Script;
import org.oscm.app.powershell.business.api.Powershell;
import org.oscm.app.powershell.business.api.PowershellCommand;
import org.oscm.app.powershell.business.api.PowershellStatus;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean for showing server information.
 */
@ManagedBean(name = "powershellBean")
@ViewScoped
public class PowerShellBean implements Serializable {

    private static final long serialVersionUID = -5835894219559699861L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerShellBean.class);

    private String subscriptionId;
    private String organizationId;
    private String instanceId;

    @PostConstruct
    public void init() {
	String lang = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lang");
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

    public List<? extends String> getStatus() {
	PowershellCommand command = new PowershellCommand();
	try (Powershell shell = new Powershell();) {
	    ProvisioningSettings settings = getProvisioningSettings();

	    Script script = new Script(getValue(settings, "CHECK_STATUS_SCRIPT"));
	    script.insertServiceParameter(settings);
	    LOGGER.debug("script: " + script.get());

	    command.init(script.get());
	    shell.lockPowerShell(instanceId);
	    shell.runCommand(instanceId, command);

	    PowershellStatus rc;
	    do {
		rc = shell.consumeOutput(instanceId);
		Thread.sleep(1000);
	    } while (rc == RUNNING);

	    if (rc == SUCCESS) {
		return command.getOutput();
	    }

	    return command.getError();
	} catch (Exception e) {
	    LOGGER.error(String.format(
		    "Failed to get status of powershell provisioning. orgId: %s, instanceId: %s, subscId: %s",
		    organizationId, instanceId, subscriptionId), e);
	    LOGGER.debug("Powershell error output: " + command.getError());
	    return command.getError();
	}
    }

    private String getValue(ProvisioningSettings settings, String paramId) {
	if (settings.getParameters().containsKey(paramId)) {
	    return settings.getParameters().get(paramId).getValue();
	}
	return "";
    }

    private ProvisioningSettings getProvisioningSettings() throws Exception {
	APPlatformService app = APPlatformServiceFactory.getInstance();
	return app.getServiceInstanceDetails(CONTROLLER_ID, instanceId, subscriptionId, organizationId);
    }

}

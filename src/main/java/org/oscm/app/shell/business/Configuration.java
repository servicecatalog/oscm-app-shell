/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 21.01.2016
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import static org.oscm.app.shell.business.ConfigurationKey.REQUESTING_USER_ID;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the <code>ProvisioningSettings</code>, provides access to service
 * instance parameters (SIP) and controller configuration settings (CTL).
 * <p>
 * Service instance parameters can be updated by the controller. Controller
 * configuration settings cannot be updated!
 * <p>
 * Configuration settings are generic settings of the controller and relate to
 * all service instances whereas service instance settings relate to one service
 * only. Typically SIP are specified per technical service within the
 * TechnicalService.xml and CTL are specified via the APP/Controller
 * configuration UI.
 * <p>
 * All non-user configurable settings are of type SIP.
 *
 * @author kulle
 *
 */
public class Configuration {

    public static final String CONTROLLER_ID = "ess.shell";

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private final ProvisioningSettings settings;

    public Configuration(ProvisioningSettings settings) {
	this.settings = settings;
    }

    /**
     * Loads either controller configuration settings (CTL) or service instance
     * parameters (SIP) from the wrapped ProvisioningSettings object.
     *
     * @param key
     *            which identifies the value to load
     * @return the value or null if the key does not exist
     */
    public String getSetting(ConfigurationKey key) {
	if (key.isInternalSetting()) {
	    return getParameterValue(key);
	}
	return loadConfigurableSetting(key);
    }

    private String getParameterValue(ConfigurationKey key) {
	if (settings.getParameters().containsKey(key.name())) {
	    return settings.getParameters().get(key.name()).getValue();
	}

	LOG.warn(String.format("The instance, for subscription %s, does not has a parameter with key %s",
		settings.getSubscriptionId(), key.name()));
	return "";
    }

    private String loadConfigurableSetting(ConfigurationKey key) {
	if (settings.getParameters().containsKey(key.name())) {
	    return settings.getParameters().get(key.name()).getValue();
	} else if (settings.getConfigSettings().containsKey(key.name())) {
	    return settings.getConfigSettings().get(key.name()).getValue();
	}

	LOG.warn(String.format("Key '%s' not found in instance parameters or configuration settings.", key.name()));
	if (key.getDefaultValue() != null) {
	    return key.getDefaultValue();
	}
	return "";
    }

    /**
     * Calls {@code getSetting(ConfigurationKey key)} and casts the value to an
     * {@code Integer}.
     */
    public Integer getIntegerSetting(ConfigurationKey key) {
	return Integer.valueOf(getSetting(key));
    }

    /**
     * Please be aware that you can update service instance parameters only. To
     * update database settings please use the controller UI or other means.
     */
    public void setSetting(ConfigurationKey key, String value) {
	settings.getParameters().put(key.name(), new Setting(key.name(), value));
    }

    public ProvisioningSettings getProvisioningSettings() {
	return settings;
    }

    public ServiceUser getRequestingUser() {
	return settings.getRequestingUser();
    }

    public String getSubscriptionId() {
	return settings.getSubscriptionId();
    }

    public String getOrganizationId() {
	return settings.getOrganizationId();
    }

    public String getOrganizationName() {
	return settings.getOrganizationName();
    }

    /**
     * Explicitly setting requesting user because APP does not save this user
     * otherwise
     */
    public void setRequestingUser() {
	setSetting(REQUESTING_USER_ID, settings.getRequestingUser().getUserId());
    }

    /**
     * Removes all existing users and then adds the provided users and their roles
     * as parameters.
     */
    public void addUsersToParameter(List<ServiceUser> users) {
	List<String> tobeRemoved = new ArrayList<String>();
	for (String key : settings.getParameters().keySet()) {
	    if (key.startsWith("USER_")) {
		tobeRemoved.add(key);
	    }
	}
	tobeRemoved.forEach(k -> settings.getParameters().remove(k));

	for (int i = 0; i < users.size(); i++) {
	    String idKey = String.format("USER_%d_ID", i);
	    settings.getParameters().put(idKey, new Setting(idKey, users.get(i).getUserId()));

	    if (users.get(i).getRoleIdentifier() != null) {
		String roleKey = String.format("USER_%d_ROLE", i);
		settings.getParameters().put(roleKey, new Setting(roleKey, users.get(i).getRoleIdentifier()));
	    }
	}
    }

    public List<ServiceUser> getUsers() {
	List<ServiceUser> result = new ArrayList<>();
	for (String key : settings.getParameters().keySet()) {
	    if (key.matches("USER_\\d+_ID")) {
		String id = settings.getParameters().get(key).getValue();

		String role = null;
		String roleKey = key.replace("ID", "ROLE");
		if (settings.getParameters().containsKey(roleKey)) {
		    role = settings.getParameters().get(roleKey).getValue();
		}

		result.add(newServiceUser(id, role));
	    }
	}
	return result;
    }

    private ServiceUser newServiceUser(String userId, String roleIdentifier) {
	ServiceUser user = new ServiceUser();
	user.setUserId(userId);
	if (roleIdentifier != null) {
	    user.setRoleIdentifier(roleIdentifier);
	}
	return user;
    }

}

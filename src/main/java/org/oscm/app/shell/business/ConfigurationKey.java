/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 21.01.2016
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kulle
 *
 */
public enum ConfigurationKey {

    SM_STATE("INTERNAL"),
    SM_STATE_HISTORY("INTERNAL"),
    SM_STATE_MACHINE("INTERNAL"),
    SM_ERROR_MESSAGE("INTERNAL"),
    REQUESTING_USER_ID("INTERNAL"),
    OPERATIONS_ID("INTERNAL"),
    INSTANCE_ID("INTERNAL"),

    /**
     * Location of shell script that will be executed. This can be a absolute
     * filesystem path or a URL.
     */
    SCRIPT_FILE("INTERNAL"),

    /**
     * Number of users which are modified with <code>updateUsers</code> provisioning
     * call.
     */
    USER_COUNT("INTERNAL"),

    BSS_USER_ID("CONFIGURABLE"),
    BSS_USER_KEY("CONFIGURABLE"),
    BSS_USER_PWD("CONFIGURABLE"),

    /**
     * Location of shell console configuration script. This is a absolute
     * filesystem path.
     */
    CONSOLE_FILE("CONFIGURABLE"),

    PROVISIONING_SCRIPT("CONFIGURABLE"),
    DEPROVISIONING_SCRIPT("CONFIGURABLE"),
    UPDATE_SCRIPT("CONFIGURABLE"),
    ASSIGN_USER_SCRIPT("CONFIGURABLE"),
    DEASSIGN_USER_SCRIPT("CONFIGURABLE"),
    CHECK_STATUS_SCRIPT("CONFIGURABLE"),
    OPERATIONS_SCRIPT("CONFIGURABLE"),
    UPDATE_USER_SCRIPT("CONFIGURABLE"),

    /**
     * Runs verifications in the synchronous part of the createInstance method, e.g.
     * could check if a VM with the same name already exists.
     */
    VERIFICATION_SCRIPT("CONFIGURABLE"),

    VERIFICATION_TIMEOUT("CONFIGURABLE", "5000");

    private String type;

    private String defaultValue;

    private ConfigurationKey(String type) {
	if (!type.equals("CONFIGURABLE") && !type.equals("INTERNAL")) {
	    throw new IllegalArgumentException("Type of enum is wrong, use either CONFIGURABLE or INTERNAL");
	}
	this.type = type;
    }

    private ConfigurationKey(String type, String defaultValue) {
	if (!type.equals("CONFIGURABLE") && !type.equals("INTERNAL")) {
	    throw new IllegalArgumentException("Type of enum is wrong, use either CONFIGURABLE or INTERNAL");
	}
	this.type = type;
	this.defaultValue = defaultValue;
    }

    public boolean isInternalSetting() {
	return type.equals("INTERNAL");
    }

    public static List<ConfigurationKey> configurableSettings() {
	List<ConfigurationKey> result = new ArrayList<>();
	for (ConfigurationKey c : ConfigurationKey.values()) {
	    if (!c.isInternalSetting()) {
		result.add(c);
	    }
	}
	return result;
    }

    public String getDefaultValue() {
	return defaultValue;
    }

}

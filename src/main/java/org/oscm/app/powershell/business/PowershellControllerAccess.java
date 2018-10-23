/*******************************************************************************
 *
 *  COPYRIGHT (C) 2017 FUJITSU Limited - ALL RIGHTS RESERVED.
 *
 *  Creation Date: 03.02.2017
 *
 *******************************************************************************/

package org.oscm.app.powershell.business;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.APPlatformServiceFactory;
import org.oscm.app.data.ControllerSettings;
import org.oscm.app.exceptions.APPlatformException;
import org.oscm.app.i18n.Messages;
import org.oscm.app.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowershellControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = 2952229574072161694L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PowershellControllerAccess.class);
    private ControllerSettings settings;

    @Override
    public String getControllerId() {
	return Configuration.CONTROLLER_ID;
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
	return Messages.get(locale, key, arguments);
    }

    @Override
    public List<String> getControllerParameterKeys() {
	List<String> result = new ArrayList<>();
	for (ConfigurationKey c : ConfigurationKey.configurableSettings()) {
	    result.add(c.name());
	}
	return result;
    }

    public ControllerSettings getSettings() {
	if (settings == null) {
	    try {
		APPlatformServiceFactory.getInstance().requestControllerSettings(getControllerId());
		LOGGER.debug("Settings were NULL. Requested from APP and got {}", settings);
	    } catch (APPlatformException e) {
		LOGGER.error("Error while ControllerAcces was requesting controller setting from APP", e);
	    }
	}
	return settings;
    }

    public void storeSettings(ControllerSettings settings) {
	this.settings = settings;
    }

}

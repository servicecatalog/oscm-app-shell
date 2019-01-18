/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 03.02.2017
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.i18n.Messages;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ShellControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = 2952229574072161694L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellControllerAccess.class);
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
                requestControllerSettings();
                LOGGER.debug("Settings were NULL. Requested from APP and got {}", settings);
            } catch (APPlatformException e) {
                LOGGER.error("Error while ControllerAccess was requesting " +
                        "controller setting from APP ", e);
            }
        }
        return settings;
    }

    void requestControllerSettings() throws APPlatformException{
        APPlatformServiceFactory.getInstance().requestControllerSettings(getControllerId());
    }

    public void storeSettings(ControllerSettings settings) {
        this.settings = settings;
    }

}

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 16.11.2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.interceptor;

import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

@ProvisioningSettingsLogger
@Interceptor
public class ProvisioningSettingsInterceptor implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisioningSettingsInterceptor.class);

    @AroundInvoke
    public Object logIncomingSettings(InvocationContext context) throws Exception {

        Object[] parameters = context.getParameters();
        Arrays.stream(parameters).forEach(parameter -> {

            if (parameter instanceof ProvisioningSettings) {

                LOGGER.debug("Current method: " + context.getMethod().getName());

                ProvisioningSettings settings = (ProvisioningSettings) parameter;
                LOGGER.debug("Following provisioning settings are available:");

                HashMap<String, Setting> params = settings.getParameters();
                LOGGER.debug("Parameters (" + params.size() + " counted):");
                logSettingsMap(params);

                HashMap<String, Setting> attributes = settings.getAttributes();
                LOGGER.debug("Attributes (" + attributes.size() + "counted):");
                logSettingsMap(attributes);

                HashMap<String, Setting> customAttributes = settings.getCustomAttributes();
                LOGGER.debug("Custom attributes (" + customAttributes.size() + "counted):");
                logSettingsMap(customAttributes);

                HashMap<String, Setting> configSettings = settings.getConfigSettings();
                LOGGER.debug("Configuration settings (" + configSettings.size() + "counted):");
                logSettingsMap(configSettings);
            }
        });

        return context.proceed();
    }

    private void logSettingsMap(HashMap<String, Setting> map) {

        map.forEach((key, value) -> {
            LOGGER.debug(key + ":" + value.getKey() + "[" + value.getValue() + "]");
        });
    }
}

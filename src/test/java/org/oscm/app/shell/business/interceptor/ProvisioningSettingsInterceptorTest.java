/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-01-14
 *
 *******************************************************************************/

package org.oscm.app.shell.business.interceptor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.oscm.app.shell.business.ConfigurationKey.ASSIGN_USER_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.CHECK_STATUS_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.DEASSIGN_USER_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.DEPROVISIONING_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.PROVISIONING_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.UPDATE_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.USAGEDATA_SCRIPT;

import java.util.HashMap;

import javax.interceptor.InvocationContext;

import org.junit.Before;
import org.junit.Ignore;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;

@Ignore
public class ProvisioningSettingsInterceptorTest {

    @Mock
    Logger LOGGER;

    @Mock
    InvocationContext invocationContext;

    @InjectMocks
    @Spy
    ProvisioningSettingsInterceptor provisioningSettingsInterceptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore // Need to find a way to define behaviour for the final Method class...
    public void testLogIncomingSettings() throws Exception {

        //given
        ProvisioningSettings provisioningSettings = getProvisioningSettings();
        Object objectArray[] = {provisioningSettings};
        when(invocationContext.getParameters()).thenReturn(objectArray);

        //when
        provisioningSettingsInterceptor.logIncomingSettings(invocationContext);

        //then
        verify(LOGGER, times(10)).debug(anyString());
    }

    private ProvisioningSettings getProvisioningSettings() {

        String sampleScriptPath = "/some/path/script.sh";

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put(PROVISIONING_SCRIPT.name(), new Setting(PROVISIONING_SCRIPT.name(), sampleScriptPath));
        parameters.put(UPDATE_SCRIPT.name(), new Setting(UPDATE_SCRIPT.name(), sampleScriptPath));
        parameters.put(DEPROVISIONING_SCRIPT.name(), new Setting(DEPROVISIONING_SCRIPT.name(), sampleScriptPath));
        parameters.put(ASSIGN_USER_SCRIPT.name(), new Setting(ASSIGN_USER_SCRIPT.name(), sampleScriptPath));
        parameters.put(DEASSIGN_USER_SCRIPT.name(), new Setting(DEASSIGN_USER_SCRIPT.name(), sampleScriptPath));
        parameters.put(CHECK_STATUS_SCRIPT.name(), new Setting(CHECK_STATUS_SCRIPT.name(), sampleScriptPath));
        parameters.put(USAGEDATA_SCRIPT.name(), new Setting(USAGEDATA_SCRIPT.name(), sampleScriptPath));
        
        HashMap<String, Setting> emptyMap = new HashMap<>();

        ProvisioningSettings settings = new ProvisioningSettings(parameters, emptyMap, emptyMap,
                emptyMap, "en");

        ServiceUser user = new ServiceUser();
        user.setUserId("supplier");
        settings.setRequestingUser(user);

        return settings;
    }

}

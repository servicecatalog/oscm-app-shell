/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-01-10
 *
 *******************************************************************************/


package org.oscm.app.shell;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.v2_0.data.ServiceUser;

import static org.mockito.Mockito.*;

public class ScriptLoggerTest {

    @Mock
    Configuration configuration;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLogScriptConfiguration_logsScriptConfigurationForNullUser_ifCalled() {
        //given
        String scriptName = "scriptName";
        String script = "/path/to/script";
        when(configuration.getRequestingUser()).thenReturn(null);

        //when
        ScriptLogger.logScriptConfiguration(configuration, scriptName, script);

        //then
        verify(configuration, times(1)).getRequestingUser();
        verify(configuration, times(1)).getOrganizationId();
        verify(configuration, times(1)).getOrganizationName();
        verify(configuration, times(1)).getSubscriptionId();
    }

    @Test
    public void testLogScriptConfiguration_logsScriptConfigurationForExistingUser_ifCalled() {
        //given
        String scriptName = "scriptName";
        String script = "/path/to/script";
        ServiceUser serviceUser = mock(ServiceUser.class);
        when(configuration.getRequestingUser()).thenReturn(serviceUser);

        //when
        ScriptLogger.logScriptConfiguration(configuration, scriptName, script);

        //then
        verify(configuration, times(3)).getRequestingUser();
        verify(configuration, times(1)).getOrganizationId();
        verify(configuration, times(1)).getOrganizationName();
        verify(configuration, times(1)).getSubscriptionId();
    }

}

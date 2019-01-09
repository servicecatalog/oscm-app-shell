/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-01-10
 *
 *******************************************************************************/


package org.oscm.app.shell;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.v2_0.data.ServiceUser;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ScriptLoggerTest {

    @Mock
    Logger LOG;

    @Mock
    Configuration configuration;

    //Null pointery...

    @Ignore
    public void testLogScriptConfiguration_logsScriptConfigurationForNullUser_ifCalled() {
        //given

        //when
        when(configuration.getRequestingUser()).thenReturn(null);
        ScriptLogger.logScriptConfiguration(configuration, anyString(), anyString());

        //then
        verify(LOG, times(4)).info(anyString());
    }

    @Ignore
    public void testLogScriptConfiguration_logsScriptConfigurationForExistingUser_ifCalled() {
        //given
        ServiceUser serviceUser = spy(new ServiceUser());

        //when
        when(configuration.getRequestingUser()).thenReturn(serviceUser);
        ScriptLogger.logScriptConfiguration(configuration, anyString(), anyString());

        //then
        verify(LOG, atLeast(4)).info(anyString());
    }

}

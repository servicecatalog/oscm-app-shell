/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-01-14
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.oscm.app.shell.ShellController;
import org.oscm.app.v2_0.data.ControllerSettings;

import java.util.List;

import static org.mockito.Mockito.*;

public class ShellControllerAccessTest {

    @Test
    public void testGetControllerParameterKeys() {

        //given
        ShellControllerAccess shellControllerAccess = new ShellControllerAccess();
        int size = ConfigurationKey.configurableSettings().size();

        //when
        List<String> list = shellControllerAccess.getControllerParameterKeys();

        //then
        Assert.assertEquals(size, list.size());
    }

    @Test
    public void testGetSettings_skipRequestControllerSettings_ifSettingsNotNull() throws Exception {

        //given
        ShellControllerAccess shellControllerAccess = mock(ShellControllerAccess.class);
        ControllerSettings controllerSettings = mock(ControllerSettings.class);
        Whitebox.setInternalState(shellControllerAccess, "settings", controllerSettings);

        //when
        doNothing().when(shellControllerAccess).requestControllerSettings();
        shellControllerAccess.getSettings();

        //then
        verify(shellControllerAccess, times(0)).requestControllerSettings();
    }

    @Ignore
    public void testGetSettings_requestControllerSettings_ifSettingsNull() throws Exception {

        //given
        ShellControllerAccess shellControllerAccess = mock(ShellControllerAccess.class);
        Whitebox.setInternalState(shellControllerAccess, "settings", null);

        //when
        shellControllerAccess.getSettings();

        //then
        verify(shellControllerAccess, times(1)).requestControllerSettings();
    }

}

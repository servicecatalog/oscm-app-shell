/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-01-14
 *
 *******************************************************************************/

package org.oscm.app.shell.business;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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

}

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Jan 09, 2019
 *
 *******************************************************************************/

package org.oscm.app.shell.business.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.v2_0.data.ProvisioningSettings;

import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.oscm.app.shell.business.actions.StatemachineEvents.FAILED;

public class OperationActionsTest {

    @Spy
    OperationActions operationActions;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteScript_returnsStateFailed_ifScriptFileIsNull() {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        // Script file is empty. However if its not empty still exception. FIXME with PowerMockito?
        when(operationActions.getActions()).thenThrow(Exception.class);

        //when
        String state = operationActions.executeScript(instanceId, settings, null);

        //then
        Assert.assertEquals("State returned is " + state, FAILED, state);
    }
}

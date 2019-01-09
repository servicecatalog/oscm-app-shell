/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
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
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;

import java.util.HashMap;

import static org.oscm.app.shell.business.actions.StatemachineEvents.FAILED;

public class OperationActionsTest {

    @Spy
    OperationActions operationActions;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteScript_returnsStateFailed_ifFailsToExecuteScript() {

        //given
        String instanceId = "Instance_4343434";
        ProvisioningSettings settings = new ProvisioningSettings(new HashMap<>(), new HashMap<>(), "en");
        InstanceStatus status = null;

        //when
        String state = operationActions.executeScript(instanceId, settings, status);

        //then
        Assert.assertEquals("State returned is " + state, FAILED, state);
    }
}

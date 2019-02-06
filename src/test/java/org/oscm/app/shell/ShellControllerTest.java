/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/
package org.oscm.app.shell;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.shell.business.ScriptValidator;
import org.oscm.app.shell.business.ShellControllerAccess;
import org.oscm.app.shell.business.api.ShellPool;
import org.oscm.app.v2_0.data.*;
import org.oscm.app.v2_0.exceptions.APPlatformException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.oscm.app.shell.business.ConfigurationKey.*;

public class ShellControllerTest {

    @Spy
    @InjectMocks
    private ShellController controller;

    @Mock
    ShellControllerAccess controllerAccess;

    @Mock
    private ScriptValidator validator;

    @Mock
    private ShellPool pool;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateInstance() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();

        // when
        InstanceDescription id = controller.createInstance(settings);

        //then
        verify(controller, times(1)).runVerificationScript(any(Configuration.class));
        verify(validator, times(6)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), id.getChangedParameters());
        assertFalse(id.isReady());
    }


    @Test
    public void testModifyInstance() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();

        // when
        InstanceStatus status = controller.modifyInstance("instance_4332312", settings, settings);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), status.getChangedParameters());
        assertFalse(status.isReady());
    }

    @Test
    public void testDeleteInstance() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();

        // when
        InstanceStatus status = controller.deleteInstance("instance_4332312", settings);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), status.getChangedParameters());
        assertFalse(status.isReady());
    }

    @Test
    public void testCreateUsers() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();
        List<ServiceUser> users = getUsers();

        // when
        InstanceStatusUsers status = controller.createUsers("instance_432432423", settings, users);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), status.getChangedParameters());
        assertEquals(users, status.getChangedUsers());
        assertFalse(status.isReady());
    }

    @Test
    public void testDeleteUsers() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();
        List<ServiceUser> users = getUsers();

        // when
        InstanceStatus status = controller.deleteUsers("instance_432432423", settings, users);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), status.getChangedParameters());
        assertFalse(status.isReady());
    }

    @Test
    public void testUpdateUsers() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();
        List<ServiceUser> users = getUsers();

        // when
        InstanceStatus status = controller.updateUsers("instance_432432423", settings, users);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), status.getChangedParameters());
        assertFalse(status.isReady());
    }

    @Test
    public void testExecuteServiceOperation() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();
        List<OperationParameter> parameters = Collections.emptyList();

        // when
        InstanceStatus status = controller.executeServiceOperation("supplier", "instance123", "trans123", "op123", parameters, settings);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertEquals(settings.getParameters(), status.getChangedParameters());
        assertFalse(status.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void testGetInstanceStatus() throws Exception {

        // given
        ProvisioningSettings settings = getProvisioningSettings();
        settings.getParameters().put(SM_STATE_MACHINE.name(), new Setting(SM_STATE_MACHINE.name(), "provision.xml"));
        settings.getParameters().put(SM_STATE.name(), new Setting(SM_STATE.name(), "END"));
        settings.getParameters().put(SM_STATE_HISTORY.name(), new Setting(SM_STATE_HISTORY.name(), "BEGIN, EXECUTING"));
        doNothing().when(pool).terminateShell(anyString());

        // when
        InstanceStatus status = controller.getInstanceStatus("instance_4332312", settings);

        //then - exception is thrown as no state machine action is not executed without proper context
    }

    @Test
    public void testSetControllerSettings() {

        // when
        controller.setControllerSettings(any(ControllerSettings.class));

        //then
        verify(controllerAccess, times(1)).storeSettings(any(ControllerSettings.class));
    }

    @Test
    public void testGatherUsageData() throws Exception {

        ProvisioningSettings settings = getProvisioningSettings();
        String startTime = "2019-02-06 07:00:00";
        String endTime = "2019-02-06 08:00:00";
        String instanceId = "Instance_1232132133";

        // when
        boolean result = controller.gatherUsageData("ess.shell", instanceId, startTime, endTime, settings);

        //then
        verify(validator, times(1)).validate(any(Configuration.class), any(ConfigurationKey.class));
        assertFalse(result);
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

    private List<ServiceUser> getUsers() {

        List<ServiceUser> users = new ArrayList<>();

        ServiceUser broker = new ServiceUser();
        broker.setUserId("broker");

        ServiceUser reseller = new ServiceUser();
        reseller.setUserId("reseller");

        users.add(broker);
        users.add(reseller);

        return users;
    }

}

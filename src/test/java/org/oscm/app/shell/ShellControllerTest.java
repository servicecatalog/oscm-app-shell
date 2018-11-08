/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.shell;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.oscm.app.shell.business.ConfigurationKey.ASSIGN_USER_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.CHECK_STATUS_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.CONSOLE_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.DEASSIGN_USER_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.DEPROVISIONING_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.INSTANCE_ID;
import static org.oscm.app.shell.business.ConfigurationKey.PROVISIONING_SCRIPT;
import static org.oscm.app.shell.business.ConfigurationKey.SCRIPT_FILE;
import static org.oscm.app.shell.business.ConfigurationKey.UPDATE_SCRIPT;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.shell.ShellController;
import org.oscm.app.shell.business.Configuration;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;

public class ShellControllerTest {

    private ShellController controller;

    private HashMap<String, Setting> params;
    private HashMap<String, Setting> config;
    private ProvisioningSettings settings;

    @Before
    public void before() throws Exception {
	String batchFile = new File(ShellControllerTest.class.getResource("/batch.ps1").getFile())
		.getAbsolutePath();
	controller = spy(new ShellController());
	doReturn(true).when(controller).doesFileExist(anyString());
	doNothing().when(controller).updateProvisioningSettings(anyString(), anyObject(), anyObject());
	doReturn(null).when(controller).getInteractiveCommand(anyString());

	params = new HashMap<>();
	params.put(INSTANCE_ID.name(), new Setting(INSTANCE_ID.name(), "1-2-3-4"));
	params.put(SCRIPT_FILE.name(), new Setting(SCRIPT_FILE.name(), batchFile));
	params.put(PROVISIONING_SCRIPT.name(), new Setting(PROVISIONING_SCRIPT.name(), batchFile));
	params.put(UPDATE_SCRIPT.name(), new Setting(UPDATE_SCRIPT.name(), batchFile));
	params.put(DEPROVISIONING_SCRIPT.name(), new Setting(DEPROVISIONING_SCRIPT.name(), batchFile));
	params.put(ASSIGN_USER_SCRIPT.name(), new Setting(ASSIGN_USER_SCRIPT.name(), batchFile));
	params.put(DEASSIGN_USER_SCRIPT.name(), new Setting(DEASSIGN_USER_SCRIPT.name(), batchFile));
	params.put(CHECK_STATUS_SCRIPT.name(), new Setting(CHECK_STATUS_SCRIPT.name(), batchFile));

	config = new HashMap<>();
	config.put(CONSOLE_FILE.name(), new Setting(CONSOLE_FILE.name(), "value"));

	settings = spy(new ProvisioningSettings(params, config, "en"));
    }

    @Test
    public void createInstance() throws Exception {
	// given
	ServiceUser user = new ServiceUser();
	user.setUserId("myuserid");
	doReturn(user).when(settings).getRequestingUser();
	doNothing().when(controller).runVerificationScript(any(Configuration.class));
	InstanceDescription id = controller.createInstance(settings);

	// when
	controller.getInstanceStatus(id.getInstanceId(), settings);
    }

    @Test
    public void modifyInstance() throws Exception {
	// given
	ServiceUser user = new ServiceUser();
	user.setUserId("myuserid");
	doReturn(user).when(settings).getRequestingUser();

	// when
	controller.modifyInstance("id", settings, settings);
    }

    @Test
    public void deleteInstance() throws Exception {
	// given
	ServiceUser user = new ServiceUser();
	user.setUserId("myuserid");
	doReturn(user).when(settings).getRequestingUser();

	// when
	controller.deleteInstance("id", settings);
    }

    @Test
    public void createUsers() throws Exception {
	// given
	List<ServiceUser> users = new ArrayList<>();
	ServiceUser user = new ServiceUser();
	user.setUserId("myuserid");
	users.add(user);
	doReturn(user).when(settings).getRequestingUser();

	// when
	controller.createUsers("id", settings, users);
    }

    @Test
    public void deleteUsers() throws Exception {
	// given
	List<ServiceUser> users = new ArrayList<>();
	ServiceUser user = new ServiceUser();
	user.setUserId("myuserid");
	users.add(user);
	doReturn(user).when(settings).getRequestingUser();

	// when
	controller.deleteUsers("id", settings, users);
    }

}

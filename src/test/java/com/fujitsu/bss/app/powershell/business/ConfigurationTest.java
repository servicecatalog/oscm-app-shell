package com.fujitsu.bss.app.powershell.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.powershell.business.Configuration;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;

public class ConfigurationTest {

    private Configuration configuration;
    private ProvisioningSettings settings;
    private HashMap<String, Setting> parameters;

    @Before
    public void before() {
	settings = mock(ProvisioningSettings.class);
	configuration = new Configuration(settings);
	parameters = new HashMap<>();
	doReturn(parameters).when(settings).getParameters();
    }

    private Set<String> getUserIds(List<ServiceUser> users) {
	Set<String> result = new HashSet<>();
	users.forEach(u -> result.add(u.getUserId()));
	return result;
    }

    private Set<String> getRoleIds(List<ServiceUser> users) {
	Set<String> result = new HashSet<>();
	users.forEach(u -> {
	    if (u.getRoleIdentifier() != null) {
		result.add(u.getRoleIdentifier());
	    }
	});
	return result;
    }

    @Test
    public void getUsers() throws Exception {
	// given
	parameters.put("USER_1_ID", new Setting("USER_1_ID", "userId1"));
	parameters.put("USER_1_ROLE", new Setting("USER_1_ROLE", "userRole1"));
	parameters.put("USER_2_ID", new Setting("USER_2_ID", "userId2"));
	parameters.put("USER_2_ROLE", new Setting("USER_2_ROLE", "userRole2"));

	// when
	List<ServiceUser> users = configuration.getUsers();

	// then
	Set<String> uids = getUserIds(users);
	Set<String> rids = getRoleIds(users);
	assertEquals(2, users.size());
	assertTrue(uids.contains("userId1"));
	assertTrue(rids.contains("userRole1"));
	assertTrue(uids.contains("userId2"));
	assertTrue(rids.contains("userRole2"));
    }

    @Test
    public void getUsers_noRoles() throws Exception {
	// given
	parameters.put("USER_1_ID", new Setting("USER_1_ID", "userId1"));
	parameters.put("USER_2_ID", new Setting("USER_2_ID", "userId2"));

	// when
	List<ServiceUser> users = configuration.getUsers();

	// then
	Set<String> uids = getUserIds(users);
	Set<String> rids = getRoleIds(users);
	assertEquals(2, users.size());
	assertTrue(uids.contains("userId1"));
	assertTrue(uids.contains("userId2"));
	assertTrue(rids.isEmpty());
    }

    @Test
    public void getUsers_mixed() throws Exception {
	// given
	parameters.put("USER_1_ID", new Setting("USER_1_ID", "userId1"));
	parameters.put("USER_1_ROLE", new Setting("USER_1_ROLE", "userRole1"));
	parameters.put("USER_2_ID", new Setting("USER_2_ID", "userId2"));

	// when
	List<ServiceUser> users = configuration.getUsers();

	// then
	Set<String> uids = getUserIds(users);
	Set<String> rids = getRoleIds(users);
	assertEquals(2, users.size());
	assertEquals(1, rids.size());
	assertTrue(uids.contains("userId1"));
	assertTrue(rids.contains("userRole1"));
	assertTrue(uids.contains("userId2"));
    }

}

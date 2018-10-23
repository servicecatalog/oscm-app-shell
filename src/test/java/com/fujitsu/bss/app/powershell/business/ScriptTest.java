/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/

package com.fujitsu.bss.app.powershell.business;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.oscm.app.powershell.business.ConfigurationKey.INSTANCE_ID;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.powershell.business.Script;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;

public class ScriptTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private Script script;

    @Before
    public void before() throws Exception {
	script = spy(new Script());
	script.script = "echo hallo" + NEW_LINE + "echo END_OF_SCRIPT";
    }

    @Test
    public void getScript() throws Exception {
	// given
	HashMap<String, Setting> serviceParams = new HashMap<>();
	serviceParams.put("key", new Setting("key", "value"));
	serviceParams.put(INSTANCE_ID.name(), new Setting(INSTANCE_ID.name(), "1-2-3-4"));
	ProvisioningSettings setting = new ProvisioningSettings(serviceParams, new HashMap<>(), "");

	// when
	script.insertServiceParameter(setting);

	// then
	assertTrue(script.get().contains("$key"));
    }

}

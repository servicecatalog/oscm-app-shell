/*******************************************************************************
 *
 *  COPYRIGHT (C) 2016 FUJITSU Limited - ALL RIGHTS RESERVED.
 *
 *  Creation Date: 16.09.2016
 *
 *******************************************************************************/

package org.oscm.app.shell.remote.app;

import static org.oscm.app.shell.business.Configuration.CONTROLLER_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * @author kulle
 *
 */
@Stateless
@Local
public class AppDatabaseClient {

    private static final String DATASOURCE = "BSSAppDS";

    private static final String ID_APP = "PROXY";

    private DataSource ds;
    private HashMap<String, String> shellSettings;
    private HashMap<String, String> appSettings;

    public String loadShellSetting(String key) throws Exception {
	if (shellSettings == null) {
	    shellSettings = loadControllerSettings(CONTROLLER_ID);
	}
	return shellSettings.get(key);
    }

    public boolean isSSOenabled() throws Exception {
	String authMode = loadAppSetting("BSS_AUTH_MODE");
	return !"INTERNAL".equals(authMode);
    }

    public String loadAppSetting(String key) throws Exception {
	if (appSettings == null) {
	    appSettings = loadControllerSettings(ID_APP);
	}
	return appSettings.get(key);
    }

    public HashMap<String, String> loadShellSettings() throws Exception {
	return loadControllerSettings(CONTROLLER_ID);
    }

    public HashMap<String, String> loadAppSettings() throws Exception {
	return loadControllerSettings(ID_APP);
    }

    private HashMap<String, String> loadControllerSettings(String controllerId) throws Exception {

	String sql = "SELECT settingkey,settingvalue FROM configurationsetting WHERE controllerid = ?";

	HashMap<String, String> settings = new HashMap<>();
	try (Connection con = getDatasource().getConnection(); PreparedStatement stmt = con.prepareStatement(sql);) {

	    stmt.setString(1, controllerId);

	    @SuppressWarnings("resource")
	    ResultSet rs = stmt.executeQuery();

	    while (rs.next()) {
		settings.put(rs.getString("settingkey"), rs.getString("settingvalue"));
	    }
	}

	return settings;
    }

    protected DataSource getDatasource() throws Exception {
	if (ds == null) {
	    try {
		final Properties ctxProperties = new Properties();
		ctxProperties.putAll(System.getProperties());
		Context namingContext = getNamingContext(ctxProperties);
		ds = (DataSource) namingContext.lookup(DATASOURCE);
	    } catch (Exception e) {
		throw new Exception("Datasource " + DATASOURCE + " not found.", e);
	    }
	}
	return ds;
    }

    protected Context getNamingContext(Properties ctxProperties) throws Exception {
	return new InitialContext(ctxProperties);
    }

}

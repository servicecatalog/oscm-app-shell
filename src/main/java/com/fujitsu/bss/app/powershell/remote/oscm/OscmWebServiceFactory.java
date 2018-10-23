package com.fujitsu.bss.app.powershell.remote.oscm;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.inject.Inject;

import org.oscm.vo.VOUserDetails;

import com.fujitsu.bss.app.powershell.remote.app.AesEncrypterWithKeyFile;
import com.fujitsu.bss.app.powershell.remote.app.AppDatabaseClient;
import com.fujitsu.bss.client.WebServiceFactory;

public class OscmWebServiceFactory {

    public static final String OSCM_ONBEHALF_PASSWORD = "AijfZur637eRgswhwd6347G345735hddfFsdf";
    public static final String OSCM_ONBEHALF_USER = "ONBEHALF_USER";

    private static final String PREFIX_CRYPT = "_crypt:";

    @Inject
    private AppDatabaseClient appClient;

    <T> T getWebServiceAsControllerOwner(Class<T> webService) throws Exception {

	WebServiceFactory factory = newFactory();
	HashMap<String, String> settings = appClient.loadPowershellSettings();
	String userId = settings.get("BSS_USER_ID");
	String userKey = settings.get("BSS_USER_KEY");
	String password = decrypt(settings.get("BSS_USER_PWD"));
	if (appClient.isSSOenabled()) {
	    return factory.getWebService(webService, userId, password);
	} else {
	    return factory.getWebService(webService, Long.valueOf(userKey), password);
	}
    }

    /**
     * The APP Owner must also be the controller owner (technology provider) and
     * supplier.
     *
     */
    <T> T getWebServiceAsAppOwner(Class<T> webService) throws Exception {

	WebServiceFactory factory = newFactory();

	HashMap<String, String> appSettings = appClient.loadAppSettings();
	String password = decrypt(appSettings.get("BSS_USER_PWD"));

	if (appClient.isSSOenabled()) {
	    String userid = appSettings.get("BSS_USER_ID");
	    return factory.getWebService(webService, userid, password);
	}

	String userkey = appSettings.get("BSS_USER_KEY");
	return factory.getWebService(webService, Long.valueOf(userkey), password);
    }

    <T> T getWebServiceAsOnbehalfUser(Class<T> webService, VOUserDetails onBehalfUser) throws Exception {

	WebServiceFactory factory = newFactory();
	if (appClient.isSSOenabled()) {
	    String userid = onBehalfUser.getUserId();
	    return factory.getWebService(webService, userid, OSCM_ONBEHALF_PASSWORD);
	}

	long userkey = onBehalfUser.getKey();
	return factory.getWebService(webService, Long.valueOf(userkey), OSCM_ONBEHALF_PASSWORD);
    }

    private WebServiceFactory newFactory() throws Exception {
	HashMap<String, String> appSettings = appClient.loadAppSettings();
	String wsdlUrl = appSettings.get("BSS_WEBSERVICE_WSDL_URL");
	wsdlUrl = wsdlUrl.substring(0, wsdlUrl.indexOf("{") - 1);
	String apiVersion = wsdlUrl.substring(wsdlUrl.indexOf("oscm") + 5, wsdlUrl.length());
	String besUrl = wsdlUrl.substring(0, wsdlUrl.indexOf("oscm") - 1);

	if (appClient.isSSOenabled()) {
	    return newSsoFactory(apiVersion, besUrl);
	} else {
	    return newFactory(apiVersion, besUrl);
	}
    }

    private WebServiceFactory newSsoFactory(String apiVersion, String besUrl)
	    throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException,
	    IOException, KeyManagementException, UnrecoverableKeyException {

	return new WebServiceFactory(besUrl, apiVersion).withSso();
    }

    private WebServiceFactory newFactory(String apiVersion, String besUrl) {
	return new WebServiceFactory(besUrl, apiVersion);
    }

    private String decrypt(String value) throws GeneralSecurityException {
	if (value.startsWith(PREFIX_CRYPT)) {
	    return value.substring(7);
	}
	return AesEncrypterWithKeyFile.decrypt(value);
    }

}

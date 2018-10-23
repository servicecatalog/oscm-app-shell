package org.oscm.app.powershell.remote.oscm;

import java.security.GeneralSecurityException;
import java.util.HashMap;

import javax.inject.Inject;

import org.oscm.app.BSSWebServiceFactory;
import org.oscm.app.data.PasswordAuthentication;
import org.oscm.app.powershell.remote.app.AesEncrypterWithKeyFile;
import org.oscm.app.powershell.remote.app.AppDatabaseClient;

public class OscmWebServiceFactory {

    public static final String OSCM_ONBEHALF_PASSWORD = "AijfZur637eRgswhwd6347G345735hddfFsdf";
    public static final String OSCM_ONBEHALF_USER = "ONBEHALF_USER";

    private static final String PREFIX_CRYPT = "_crypt:";

    @Inject
    private AppDatabaseClient appClient;

    /**
     * FIXME LG: Not applicable?
     * 
     * The APP Owner must also be the controller owner (technology provider) and
     * supplier.
     * 
     */
    <T> T getWebServiceAsAppOwner(Class<T> webService) throws Exception {

        HashMap<String, String> appSettings = appClient.loadAppSettings();
        String password = decrypt(appSettings.get("BSS_USER_PWD"));

        String userid = appSettings.get("BSS_USER_ID");

        return getWebService(webService,
                new PasswordAuthentication(userid, password));
    }
   
    /**
     * Returns service interfaces for BSS web service calls.
     */
    public <T> T getWebService(Class<T> serviceClass, PasswordAuthentication pw)
            throws Exception {
        return BSSWebServiceFactory.getBSSWebService(serviceClass, pw);
    }

    private String decrypt(String value) throws GeneralSecurityException {
        if (value.startsWith(PREFIX_CRYPT)) {
            return value.substring(7);
        }
        return AesEncrypterWithKeyFile.decrypt(value);
    }

}

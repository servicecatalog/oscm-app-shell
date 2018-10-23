package org.oscm.app.powershell.remote.oscm;

import javax.enterprise.inject.spi.CDI;

import org.oscm.intf.SubscriptionService;
import org.oscm.vo.VOSubscriptionDetails;

public class SubscriptionSvc {

    private OscmWebServiceFactory getFactory() throws Exception {
	return CDI.current().select(OscmWebServiceFactory.class).get();
    }

    public VOSubscriptionDetails getSubscription(String orgId, String subId) throws Exception {
	SubscriptionService service = getFactory().getWebServiceAsAppOwner(SubscriptionService.class);
	return service.getSubscriptionForCustomer(orgId, subId);
    }

}

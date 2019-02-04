/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019                                           
 *
 *  Creation Date: 29.01.2019                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business.usagedata;

import org.oscm.app.shell.business.api.json.ShellResultUsageData;
import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.intf.EventService;
import org.oscm.types.exceptions.DuplicateEventException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOGatheredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Set;

/**
 * @author goebel
 */
public class UsageHandler {


    private static final Logger LOGGER = LoggerFactory
            .getLogger(UsageHandler.class);

    private ProvisioningSettings settings;
    private UsageConnector connector;

    public UsageHandler(ProvisioningSettings settings) {
        this.settings = settings;
        this.connector = new UsageConnector(settings);
    }

    public void registerUsageEvents(long startTime, long endTime)
            throws ConfigurationException, MalformedURLException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            ValidationException {

        Set<ShellResultUsageData> usageData = connector.getData(startTime, endTime);

        for (ShellResultUsageData data : usageData) {
            submit(data.getEventId(), data.getMultiplier(), endTime);
        }
    }

    void submit(String eventId, long multiplier, long occurrence)
            throws ConfigurationException, MalformedURLException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            ValidationException {

        LOGGER.info("Submitting event[" + eventId + "] initialized");

        if (multiplier <= 0) {
            LOGGER.error("Submitting event [" + eventId + "] failed,  invalid value: " + multiplier);
            return;
        }

        VOGatheredEvent event = new VOGatheredEvent();

        event.setActor(settings.getAuthentication().getUserName());
        event.setEventId(eventId);
        event.setMultiplier(multiplier);
        event.setOccurrenceTime(occurrence);
        event.setUniqueId(eventId + "_" + occurrence);

        Setting tsId = settings.getParameters().get("TECHNICAL_SERVICE_ID");
        Setting instanceId = settings.getParameters().get("INSTANCE_ID");

        try {
            EventService svc = BSSWebServiceFactory.getBSSWebService(
                    EventService.class, settings.getAuthentication());
            svc.recordEventForInstance(tsId.getValue(), instanceId.getValue(), event);

        } catch (DuplicateEventException e) {
            LOGGER.error("Submitting event [" + eventId + "] failed", e);
        }

        LOGGER.info("Submitting event [" + eventId + "] finished successfully");
    }
}

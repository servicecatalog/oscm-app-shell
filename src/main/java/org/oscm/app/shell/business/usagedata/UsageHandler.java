/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 29.01.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.usagedata;

import java.math.BigDecimal;
import java.net.MalformedURLException;

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

/**
 * @author goebel
 *
 */
public class UsageHandler {

    static final String EVENT_DISK = "EVENT_DISK_GIGABYTE_HOURS";
    static final String EVENT_CPU = "EVENT_CPU_HOURS";
    static final String EVENT_RAM = "EVENT_RAM_MEGABYTE_HOURS";
    static final String EVENT_TOTAL = "EVENT_TOTAL_HOURS";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UsageHandler.class);

    ProvisioningSettings settings;

    private UsageHandler() {

    }

    public UsageHandler(ProvisioningSettings settings) {
        this.settings = settings;
    }

    protected UsageConnector getConnector() {
        return new UsageConnector(settings);
    }

    public void registerUsageEvents(long startTime, long endTime)
            throws ConfigurationException, MalformedURLException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            ValidationException {

        UsageResultData ud = getConnector().getData(startTime, endTime);
        

        if (ud.getTotalHours() != null) {
            long totalHours = new BigDecimal(ud.getTotalHours()).longValue();
            submit(EVENT_TOTAL, totalHours, endTime);
        }

        if (ud.getTotalMemoryMbUsage() != null) {
            long totalMemory = Long.valueOf(ud.getTotalMemoryMbUsage()).longValue();
            submit(EVENT_RAM, totalMemory, endTime);
        }

        if (ud.getTotalVcpusUsage() != null) {
            long totalCpu = Long.valueOf(ud.getTotalVcpusUsage()).longValue();
            submit(EVENT_CPU, totalCpu, endTime);
        }

        if (ud.getTotalVcpusUsage() != null) {
            long totalGb = Long.valueOf(ud.getTotalVcpusUsage()).longValue();
            submit(EVENT_DISK, totalGb, endTime);
        }

    }

    void submit(String eventId, long multiplier, long occurence)
            throws ConfigurationException, MalformedURLException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            ValidationException {

        if (multiplier <= 0) {
            return;
        }

        VOGatheredEvent event = new VOGatheredEvent();

        event.setActor(settings.getAuthentication().getUserName());
        event.setEventId(eventId);
        event.setMultiplier(multiplier);
        event.setOccurrenceTime(occurence);
        event.setUniqueId(eventId + "_" + occurence);

        Setting tsId = settings.getParameters().get("TECHNICAL_SERVICE_ID");
        Setting instanceId = settings.getParameters().get("INSTANCE_ID");

        try {

            EventService svc = BSSWebServiceFactory.getBSSWebService(
                    EventService.class, settings.getAuthentication());
            svc.recordEventForInstance(tsId.getValue(), instanceId.getValue(),
                    event);
        } catch (DuplicateEventException e) {
            LOGGER.debug("Event already inserted");
        }
    }
}

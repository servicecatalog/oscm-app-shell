/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 01.02.2019
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api.json;

import java.util.Objects;

public class ShellResultUsageData {

    private String eventId;

    private long multiplier;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(long multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ShellResultUsageData)) return false;
        ShellResultUsageData that = (ShellResultUsageData) o;
        return Objects.equals(getEventId(), that.getEventId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }

    @Override
    public String toString() {
        return "ShellResultUsageData{" +
                "eventId='" + eventId + '\'' +
                ", multiplier=" + multiplier +
                '}';
    }
}

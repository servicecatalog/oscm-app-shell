/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 18, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api.json;

import java.util.Optional;
import java.util.Set;

public class ShellResult {

    public ShellResult() {
    }

    public ShellResult(String status, String message) {
        this.status = status;
        this.message = message;
    }

    private String status;
    private String message;
    private ShellResultData data;
    private Set<ShellResultUsageData> usageData;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Optional<ShellResultData> getData() {
        return Optional.ofNullable(this.data);
    }

    public void setData(ShellResultData data) {
        this.data = data;
    }

    public Set<ShellResultUsageData> getUsageData() {
        return usageData;
    }

    public void setUsageData(Set<ShellResultUsageData> usageData) {
        this.usageData = usageData;
    }
}

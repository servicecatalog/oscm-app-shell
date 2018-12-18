/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 18, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import java.util.Optional;

public class ShellResult {

    private String status;

    private String message;

    private ShellResultData data;

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

    public void setData(ShellResultData data) {
        this.data = data;
    }

    public Optional<ShellResultData> getData(){
        return Optional.ofNullable(this.data);
    }
}

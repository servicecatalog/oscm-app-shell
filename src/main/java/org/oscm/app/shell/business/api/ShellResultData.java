/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 18, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

public class ShellResultData {

    private String accessInfo;

    private String output;

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String accessInfo) {

        this.accessInfo = accessInfo;
    }

    public String getOutput() {

        return output;
    }

    public void setOutput(String output) {

        this.output = output;
    }
}

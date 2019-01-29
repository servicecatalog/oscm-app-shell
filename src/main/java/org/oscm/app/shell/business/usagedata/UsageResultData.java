/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 29.01.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.usagedata;

import org.oscm.app.shell.business.api.ShellResultData;

/**
 * @author goebel
 *
 */
public class UsageResultData extends ShellResultData {

    private String totalHours;
    private String totalMemUsage;
    private String totalVCPUUsage;

    public String getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(String totalHours) {
        this.totalHours = totalHours;
    }

    public String getTotalMemoryMbUsage() {
        return totalVCPUUsage;
    }
    
    public void setTotalMemoryMbUsage(String totalMemUsage) {
        this.totalMemUsage = totalMemUsage;
    }
    public String getTotalVcpusUsage() {
        return totalVCPUUsage;
    }
    
    public void setTotalVcpusUsage(String totalVCPUUsage) {
        this.totalVCPUUsage = totalVCPUUsage;
    }
}

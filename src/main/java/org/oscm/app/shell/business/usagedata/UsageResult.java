/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 29.01.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.usagedata;

import java.util.Optional;

import org.oscm.app.shell.business.api.ShellResult;

/**
 * @author goebel
 *
 */
public class UsageResult extends ShellResult {

    public void setUsageData(UsageResultData data) {
        super.setData(data);
    }

    public Optional<UsageResultData> getUsageData() {
        return Optional.ofNullable((UsageResultData) data);
    }
}

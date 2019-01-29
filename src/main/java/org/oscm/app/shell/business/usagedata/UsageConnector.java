/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 29.01.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.usagedata;

import static org.oscm.app.shell.business.api.Shell.STATUS_OK;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

import java.util.Optional;

import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.shell.business.Script;
import org.oscm.app.shell.business.api.Shell;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author goebel
 *
 */
public class UsageConnector {
    private static final Logger LOG = LoggerFactory
            .getLogger(UsageConnector.class);

    private ProvisioningSettings settings;

    protected UsageConnector(ProvisioningSettings settings) {
        this.settings = settings;
    }

    protected UsageResultData getData(long start, long end) {

        try (Shell shell = new Shell()) {
            String usageScript;

            Script script = getUsageScript(settings);

            // log

            ShellCommand command = new ShellCommand();
            command.init(script.getContent());

            String instanceId = settings.getParameters().get("INSTANCE_ID")
                    .getValue();
            shell.lockShell(instanceId);
            shell.runCommand(instanceId, command);

            ShellStatus status;
            do {
                status = shell.consumeOutput(instanceId);
                Thread.sleep(1000);
            } while (status == RUNNING);

            UsageResult result = shell.getResult(UsageResult.class);

            if (STATUS_OK.equals(result.getStatus())) {

                Optional<UsageResultData> data = result.getUsageData();
                if (data.isPresent()) {
                    return data.get();
                }

                // logging
            }

        } catch (Exception e) {
            LOG.warn("Error retrieving usage data: " + e.getMessage());
        }
        return new UsageResultData();
    }

    protected Script getUsageScript(ProvisioningSettings settings)
            throws Exception {

        String usageScript;
        usageScript = settings.getParameters()
                .get(ConfigurationKey.USAGEDATA_SCRIPT.name()).getValue();
        Script script = new Script(usageScript);
        script.loadContent();
        script.insertProvisioningSettings(settings);
        return script;
    }
}

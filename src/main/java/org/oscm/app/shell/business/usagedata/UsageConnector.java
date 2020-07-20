/*******************************************************************************
 *
 * Copyright FUJITSU LIMITED 2019
 *
 * Creation Date: 29.01.2019
 *
 *******************************************************************************/
package org.oscm.app.shell.business.usagedata;

import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.shell.business.api.Shell;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellResultException;
import org.oscm.app.shell.business.api.ShellStatus;
import org.oscm.app.shell.business.api.json.ShellResult;
import org.oscm.app.shell.business.api.json.ShellResultUsageData;
import org.oscm.app.shell.business.script.Script;
import org.oscm.app.shell.business.script.ScriptType;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.oscm.app.shell.business.api.Shell.STATUS_OK;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

/** @author goebel */
public class UsageConnector {

  private static final Logger LOG = LoggerFactory.getLogger(UsageConnector.class);

  private ProvisioningSettings settings;

  public UsageConnector(ProvisioningSettings settings) {
    this.settings = settings;
  }

  public Set<ShellResultUsageData> getData(long start, long end) throws Exception {

    Script script = getUsageScript(settings);

    ScriptType scriptType = script.getScriptType();

    try (Shell shell = new Shell(scriptType)) {

      settings.getParameters().put("START_TIME", new Setting("START_TIME", Long.toString(start)));
      settings.getParameters().put("END_TIME", new Setting("END_TIME", Long.toString(end)));

      ShellCommand command = new ShellCommand();
      command.init(script.getScriptContent());
      command.setScriptType(scriptType);

      String instanceId = settings.getParameters().get("INSTANCE_ID").getValue();
      shell.lockShell(instanceId);
      shell.runCommand(instanceId, command);

      ShellStatus status;
      do {
        status = shell.consumeOutput(instanceId);
        Thread.sleep(1000);
      } while (status == RUNNING);

      ShellResult result = shell.getResult();

      if (STATUS_OK.equals(result.getStatus())) {
        return result.getUsageData();
      } else {
        throw new ShellResultException(result.getMessage());
      }

    } catch (Exception e) {
      LOG.error("Error retrieving usage data: " + e.getMessage(), e);
      throw e;
    }
  }

  private Script getUsageScript(ProvisioningSettings settings) throws Exception {

    String scriptName = ConfigurationKey.USAGEDATA_SCRIPT.name();
    String script = settings.getParameters().get(scriptName).getValue();

    Script usageScript = Script.getInstance(script);
    usageScript.initialize();
    usageScript.insertProvisioningSettings(settings);

    return usageScript;
  }
}

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020
 *
 *  Creation Date: 16.07.2020
 *
 *******************************************************************************/
package org.oscm.app.shell.business.script;

public class PowerShellScript extends Script {

  public PowerShellScript(String scriptFile) {
    this.scriptFile = scriptFile;
    this.scriptType = ScriptType.POWERSHELL;
  }

  @Override
  public String buildParameterCommand(String key, String value) {
    return "$" + key + "=\"" + value + "\"" + NEW_LINE;
  }
}

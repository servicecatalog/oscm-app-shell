/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020
 *
 *  Creation Date: 16.07.2020
 *
 *******************************************************************************/
package org.oscm.app.shell.business.script;

public class ShellScript extends Script {

  public ShellScript(String scriptFile) {
    this.scriptFile = scriptFile;
    this.scriptType = ScriptType.SHELL;
  }

  @Override
  public String buildParameterCommand(String key, String value) {
    return key + "=\"" + value + "\"" + NEW_LINE;
  }
}

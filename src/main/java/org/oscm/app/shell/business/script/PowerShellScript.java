package org.oscm.app.shell.business.script;

public class PowerShellScript extends Script {

  public PowerShellScript(String scriptFile) {
    this.scriptFile = scriptFile;
  }

  @Override
  public String buildParameterCommand(String key, String value) {
    return "$" + key + "=\"" + value + "\"" + NEW_LINE;
  }
}

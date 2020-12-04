/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Dec 18, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.shell.business.api.json;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class ShellResult {

  public ShellResult() {}

  public ShellResult(String status, String message) {
    this.status = status;
    this.message = message;
  }

  private String status;
  private String message;
  private ShellResultData data;
  private Set<ShellResultUsageData> usageData;
  private Set<ShellResultParameter> parameters;

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
    return usageData != null ? usageData : Collections.emptySet();
  }

  public void setUsageData(Set<ShellResultUsageData> usageData) {
    this.usageData = usageData;
  }

  public Set<ShellResultParameter> getParameters() {
    return parameters != null ? parameters : Collections.emptySet();
  }

  public void setParameters(Set<ShellResultParameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return "ShellResult{"
        + "status='"
        + status
        + '\''
        + ", message='"
        + message
        + '\''
        + ", data="
        + data
        + ", usageData="
        + usageData
        + ", parameters="
        + parameters
        + '}';
  }
}

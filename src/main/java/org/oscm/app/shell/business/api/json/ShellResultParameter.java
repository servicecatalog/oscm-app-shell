/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 03.12.2020
 *
 *******************************************************************************/
package org.oscm.app.shell.business.api.json;

public class ShellResultParameter {

  private String key;
  private String value;

  public ShellResultParameter(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Aug 2, 2017
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.shell;

import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.shell.business.ScriptValidator;
import org.oscm.app.shell.business.ShellControllerAccess;
import org.oscm.app.shell.business.actions.StateMachineId;
import org.oscm.app.shell.business.api.Shell;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellPool;
import org.oscm.app.shell.business.api.ShellStatus;
import org.oscm.app.shell.business.api.json.ShellResult;
import org.oscm.app.shell.business.interceptor.ProvisioningSettingsLogger;
import org.oscm.app.shell.business.script.Script;
import org.oscm.app.shell.business.script.ScriptType;
import org.oscm.app.shell.business.usagedata.UsageHandler;
import org.oscm.app.statemachine.StateMachine;
import org.oscm.app.statemachine.api.StateMachineException;
import org.oscm.app.v2_0.data.*;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.time.LocalDateTime.parse;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.oscm.app.shell.business.Configuration.CONTROLLER_ID;
import static org.oscm.app.shell.business.ConfigurationKey.*;
import static org.oscm.app.shell.business.api.Shell.STATUS_ERROR;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

@Remote(APPlatformController.class)
@Stateless(mappedName = "bss/app/controller/" + CONTROLLER_ID)
@ProvisioningSettingsLogger
public class ShellController implements APPlatformController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShellController.class);

  private static final String STATEMACHINE_PROVISION = "provision.xml";
  private static final String STATEMACHINE_DEPROVISION = "deprovision.xml";
  private static final String STATEMACHINE_UPDATE = "update.xml";
  private static final String STATEMACHINE_ASSIGN_USER = "assign_user.xml";
  private static final String STATEMACHINE_DEASSIGN_USER = "deassign_user.xml";
  private static final String STATEMACHINE_OPERATION = "operation.xml";
  private static final String STATEMACHINE_UPDATE_USER = "update_user.xml";

  private ShellControllerAccess controllerAccess;

  @Inject private ScriptValidator validator;

  @Inject ShellPool pool;

  @Inject
  public void setControllerAccess(final ControllerAccess access) {
    this.controllerAccess = (ShellControllerAccess) access;
  }

  @Override
  public InstanceStatus activateInstance(String instanceId, ProvisioningSettings settings) {

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(settings.getParameters());
    return result;
  }

  @Override
  public InstanceDescription createInstance(ProvisioningSettings settings)
      throws APPlatformException {

    InstanceDescription id = new InstanceDescription();
    id.setInstanceId(UUID.randomUUID().toString());
    id.setChangedParameters(settings.getParameters());
    id.setChangedAttributes(settings.getAttributes());

    Configuration config = new Configuration(settings);
    config.setRequestingUser();
    config.setSetting(INSTANCE_ID, id.getInstanceId());

    validateAllScripts(config);

    runVerificationScript(config);
    initStateMachine(settings, PROVISIONING_SCRIPT, STATEMACHINE_PROVISION);

    return id;
  }

  private void validateAllScripts(Configuration config) throws APPlatformException {

    ConfigurationKey[] scriptKeys = {
      PROVISIONING_SCRIPT,
      DEPROVISIONING_SCRIPT,
      UPDATE_SCRIPT,
      ASSIGN_USER_SCRIPT,
      DEASSIGN_USER_SCRIPT,
      CHECK_STATUS_SCRIPT
    };

    for (ConfigurationKey scriptKey : scriptKeys) {
      validator.validate(config, scriptKey);
    }

    LOGGER.info("All scripts were successfully validated");
  }

  void runVerificationScript(Configuration config) throws APPlatformException {

    String verificationScript = config.getSetting(VERIFICATION_SCRIPT);

    if (verificationScript.isEmpty()) {
      return;
    }

    validator.validate(config, VERIFICATION_SCRIPT);

    String instanceId = config.getSetting(INSTANCE_ID);
    long timeout = Long.parseLong(config.getSetting(VERIFICATION_TIMEOUT));
    Shell shell = null;
    try {

      Script script = Script.getInstance(verificationScript);
      script.initialize();
      script.insertProvisioningSettings(config.getProvisioningSettings());
      ScriptType scriptType = script.getScriptType();

      ShellCommand command = new ShellCommand(script.getScriptContent());

      command.setScriptType(scriptType);
      shell = new Shell(scriptType);
      shell.lockShell(instanceId);
      shell.runCommand(instanceId, command);

      ShellStatus status;
      long startTime = currentTimeMillis();
      do {
        status = shell.consumeOutput(instanceId);
        Thread.sleep(1000);
      } while (status == RUNNING && currentTimeMillis() - startTime < timeout);

      ShellResult result = shell.getResult();
      String message = result.getMessage();

      if (STATUS_ERROR.equals(result.getStatus())) {
        throw new APPlatformException(message);
      }
      LOGGER.info("Verification has been finished successfully:" + message);

    } catch (Exception exception) {
      LOGGER.error("Verification failed: ", exception);
      throw new APPlatformException("Verification failed: " + exception.getMessage());
    } finally {
      if (shell != null) {
        shell.close();
      }
    }
  }

  private void initStateMachine(
      ProvisioningSettings settings, ConfigurationKey serviceParamKey, String stateMachineFilename)
      throws APPlatformException {

    try {
      Configuration config = new Configuration(settings);
      Setting setting = new Setting(SCRIPT_FILE.name(), config.getSetting(serviceParamKey));
      settings.getParameters().put(SCRIPT_FILE.name(), setting);
      StateMachine.initializeProvisioningSettings(settings, stateMachineFilename);

    } catch (Exception e) {
      LOGGER.error("Failed to initialize state machine " + stateMachineFilename + ". ", e);
      throw new APPlatformException(e.getMessage(), e);
    }
  }

  @Override
  public InstanceStatusUsers createUsers(
      String instanceId, ProvisioningSettings settings, List<ServiceUser> users)
      throws APPlatformException {

    Configuration config = new Configuration(settings);
    config.setRequestingUser();
    validator.validate(config, ASSIGN_USER_SCRIPT);
    initStateMachine(settings, ASSIGN_USER_SCRIPT, STATEMACHINE_ASSIGN_USER);

    config.addUsersToParameter(users);

    InstanceStatusUsers result = new InstanceStatusUsers();
    result.setChangedUsers(users);
    result.setChangedParameters(settings.getParameters());
    result.setIsReady(false);
    return result;
  }

  @Override
  public InstanceStatus deactivateInstance(String instanceId, ProvisioningSettings settings) {

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(settings.getParameters());
    return result;
  }

  @Override
  public InstanceStatus deleteInstance(String instanceId, ProvisioningSettings settings)
      throws APPlatformException {

    Configuration config = new Configuration(settings);
    validator.validate(config, DEPROVISIONING_SCRIPT);
    initStateMachine(settings, DEPROVISIONING_SCRIPT, STATEMACHINE_DEPROVISION);

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(settings.getParameters());
    return result;
  }

  @Override
  public InstanceStatus deleteUsers(
      String instanceId, ProvisioningSettings settings, List<ServiceUser> users)
      throws APPlatformException {

    Configuration config = new Configuration(settings);
    config.setRequestingUser();
    validator.validate(config, DEASSIGN_USER_SCRIPT);
    initStateMachine(settings, DEASSIGN_USER_SCRIPT, STATEMACHINE_DEASSIGN_USER);

    config.addUsersToParameter(users);

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(settings.getParameters());
    result.setIsReady(false);
    return result;
  }

  @Override
  public InstanceStatus executeServiceOperation(
      String userId,
      String instanceId,
      String transactionId,
      String operationId,
      List<OperationParameter> parameters,
      ProvisioningSettings settings)
      throws APPlatformException {

    Configuration config = new Configuration(settings);
    config.setSetting(REQUESTING_USER_ID, userId);
    validator.validate(config, OPERATIONS_SCRIPT);
    config.setSetting(OPERATIONS_ID, operationId);
    initStateMachine(settings, OPERATIONS_SCRIPT, STATEMACHINE_OPERATION);

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(settings.getParameters());
    return result;
  }

  @Override
  public List<LocalizedText> getControllerStatus(ControllerSettings settings) {

    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public InstanceStatus getInstanceStatus(String instanceId, ProvisioningSettings settings)
      throws APPlatformException {

    Configuration config = new Configuration(settings);
    InstanceStatus status = new InstanceStatus();
    StateMachine stateMachine;

    try {
      stateMachine = new StateMachine(settings);
      stateMachine.executeAction(settings, instanceId, status);

      StateMachineId stateId = StateMachineId.valueOf(stateMachine.getStateId());

      if (StateMachineId.ERROR.equals(stateId)) {
        String errorMsg = config.getSetting(SM_ERROR_MESSAGE);
        LOGGER.error(
            "Script execution for Instance ["
                + instanceId
                + "] "
                + "resulted with error: "
                + errorMsg);
        handleErrorSituation(instanceId, errorMsg);
      }

      config.setSetting(SM_STATE_HISTORY, stateMachine.getHistory());
      config.setSetting(SM_STATE, stateMachine.getStateId());

    } catch (StateMachineException e) {

      LOGGER.error("Failed to getInstanceStatus for Instance [" + instanceId + "]", e);
      handleErrorSituation(instanceId, e.getMessage());
    }

    status.setChangedParameters(settings.getParameters());

    return status;
  }

  private void handleErrorSituation(String instanceId, String message) throws APPlatformException {

    pool.terminateShell(instanceId);
    throw new APPlatformException(message);
  }

  @Override
  public List<OperationParameter> getOperationParameters(
      String arg0, String arg1, String arg2, ProvisioningSettings arg3) {

    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public InstanceStatus modifyInstance(
      String instanceId, ProvisioningSettings settings, ProvisioningSettings newSettings)
      throws APPlatformException {

    Configuration config = new Configuration(newSettings);
    validator.validate(config, UPDATE_SCRIPT);
    config.setSetting(REQUESTING_USER_ID, settings.getRequestingUser().getUserId());
    initStateMachine(newSettings, UPDATE_SCRIPT, STATEMACHINE_UPDATE);

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(newSettings.getParameters());
    return result;
  }

  @Override
  public InstanceStatus notifyInstance(String arg0, ProvisioningSettings arg1, Properties arg2) {
    return null;
  }

  @Override
  public InstanceStatus updateUsers(
      String instanceId, ProvisioningSettings settings, List<ServiceUser> users)
      throws APPlatformException {

    Configuration config = new Configuration(settings);
    config.setRequestingUser();
    validator.validate(config, UPDATE_USER_SCRIPT);
    initStateMachine(settings, UPDATE_USER_SCRIPT, STATEMACHINE_UPDATE_USER);

    config.addUsersToParameter(users);
    config.setSetting(USER_COUNT, valueOf(users.size()));

    InstanceStatus result = new InstanceStatus();
    result.setChangedParameters(settings.getParameters());
    result.setIsReady(false);
    return result;
  }

  @Override
  public void setControllerSettings(ControllerSettings settings) {
    if (controllerAccess != null) {
      controllerAccess.storeSettings(settings);
    }
  }

  /** */
  @Override
  public boolean gatherUsageData(
      String controllerId,
      String instanceId,
      String startTime,
      String endTime,
      ProvisioningSettings settings) {

    LOGGER.info("Gathering usage data for instance [" + instanceId + "] started");
    Configuration config = new Configuration(settings);

    if (settings.getRequestingUser() != null) {
      config.setSetting(REQUESTING_USER_ID, settings.getRequestingUser().getUserId());
    }

    try {
      validator.validate(config, USAGEDATA_SCRIPT);

      final long start = parse(startTime, ISO_LOCAL_DATE_TIME).toInstant(UTC).toEpochMilli();
      final long end = parse(endTime, ISO_LOCAL_DATE_TIME).toInstant(UTC).toEpochMilli();

      new UsageHandler(settings).registerUsageEvents(start, end);

      LOGGER.info(
          String.format(
              "Usage data for %s from %s to %s retrieved successfully",
              instanceId, startTime, endTime));
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to retrieve usage data for instance [" + instanceId + "]", e);
      return false;
    }
  }
}

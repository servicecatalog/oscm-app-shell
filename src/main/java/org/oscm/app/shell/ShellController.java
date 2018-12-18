/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Aug 2, 2017
 *
 *******************************************************************************/

package org.oscm.app.shell;

import org.oscm.app.shell.business.*;
import org.oscm.app.shell.business.api.Shell;
import org.oscm.app.shell.business.api.ShellCommand;
import org.oscm.app.shell.business.api.ShellPool;
import org.oscm.app.shell.business.api.ShellStatus;
import org.oscm.app.shell.business.interceptor.ProvisioningSettingsLogger;
import org.oscm.app.statemachine.StateMachine;
import org.oscm.app.statemachine.api.StateMachineException;
import org.oscm.app.v2_0.data.*;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;
import static org.oscm.app.shell.business.Configuration.CONTROLLER_ID;
import static org.oscm.app.shell.business.ConfigurationKey.*;
import static org.oscm.app.shell.business.api.Shell.VERIFICATION_MESSAGE;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;

@Remote(APPlatformController.class)
@Stateless(mappedName = "bss/app/controller/" + CONTROLLER_ID)
@ProvisioningSettingsLogger
public class ShellController implements APPlatformController {

    private static final Logger LOG = LoggerFactory
            .getLogger(ShellController.class);

    private static final String ASYNC_ERROR = "ASYNC_ERROR";
    private static final String SYNC_ERROR = "ERROR";
    private static final String STATEMACHINE_PROVISION = "provision.xml";
    private static final String STATEMACHINE_DEPROVISION = "deprovision.xml";
    private static final String STATEMACHINE_UPDATE = "update.xml";
    private static final String STATEMACHINE_ASSIGN_USER = "assign_user.xml";
    private static final String STATEMACHINE_DEASSIGN_USER = "deassign_user.xml";
    private static final String STATEMACHINE_OPERATION = "operation.xml";
    private static final String STATEMACHINE_UPDATE_USER = "update_user.xml";

    private ShellControllerAccess controllerAccess;

    @Inject
    private ScriptValidator validator;

    @Inject
    ShellPool pool;

    @Inject
    public void setControllerAccess(final ControllerAccess access) {
        this.controllerAccess = (ShellControllerAccess) access;
    }

    @Override
    public InstanceStatus activateInstance(String instanceId,
                                           ProvisioningSettings settings) throws APPlatformException {
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
        LOG.info("All scripts are correct");
        runVerificationScript(config);
        initStateMachine(settings, PROVISIONING_SCRIPT,
                STATEMACHINE_PROVISION);

        return id;
    }

    private void validateAllScripts(Configuration config) throws APPlatformException {

        ConfigurationKey[] scriptKeys = {PROVISIONING_SCRIPT, DEPROVISIONING_SCRIPT,
                UPDATE_SCRIPT, ASSIGN_USER_SCRIPT, DEASSIGN_USER_SCRIPT, CHECK_STATUS_SCRIPT};

        for (ConfigurationKey scriptKey : scriptKeys) {
            validator.validate(config, scriptKey);
        }
    }

    void runVerificationScript(Configuration config) throws APPlatformException {

        String verificationScript = config.getSetting(VERIFICATION_SCRIPT);

        if (verificationScript.isEmpty()) {
            return;
        }

        Script script;
        ScriptLogger scriptLogger = new ScriptLogger();
        try {
            script = new Script(verificationScript);
            script.loadContent();
            script.insertProvisioningSettings(
                    config.getProvisioningSettings());
        } catch (Exception e) {
            throw new APPlatformException(e.getMessage());
        }

        try {
            ShellCommand command = new ShellCommand(
                    script.getContent());

            try (Shell shell = new Shell()) {
                shell.lockShell(config.getSetting(INSTANCE_ID));
                shell.runCommand(config.getSetting(INSTANCE_ID),
                        command);
                long ref = currentTimeMillis();
                ShellStatus rc;
                do {
                    rc = shell.consumeOutput(
                            config.getSetting(INSTANCE_ID));
                    sleep(100);
                } while (rc == RUNNING
                        && currentTimeMillis() - ref < valueOf(
                        config.getSetting(
                                VERIFICATION_TIMEOUT)));
                String shellOutput = shell.getOutput(
                );
                //JsonObject jsonOutput = shell.getResult();
                //LOG.warn("Json output : " + jsonOutput);
                scriptLogger.logOutputFromScript(config,
                        "VERIFICATION_SCRIPT", shellOutput);
                Pattern p = compile(format(".*%s=(.*?)$",
                        VERIFICATION_MESSAGE),
                        MULTILINE);
                Matcher matcher = p.matcher(shellOutput);
                if (matcher.find()) {
                    throw new APPlatformException(
                            "Verification failed: "
                                    + matcher.group(1));
                }
            }
        } catch (APPlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new APPlatformException(
                    "Verification failed because of an exception",
                    e);
        }
    }

    private void initStateMachine(ProvisioningSettings settings,
                                  ConfigurationKey serviceParamKey, String statemachineFilename)
            throws APPlatformException {
        try {
            Configuration config = new Configuration(settings);
            Setting setting = new Setting(SCRIPT_FILE.name(),
                    config.getSetting(serviceParamKey));
            settings.getParameters()
                    .put(SCRIPT_FILE.name(), setting);
            StateMachine.initializeProvisioningSettings(settings,
                    statemachineFilename);
        } catch (Exception e) {
            LOG.error("Failed to initialize state machine "
                    + statemachineFilename + ". ", e);
            throw new APPlatformException(e.getMessage(), e);
        }
    }

    @Override
    public InstanceStatusUsers createUsers(String instanceId,
                                           ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setRequestingUser();
        validator.validate(config, ASSIGN_USER_SCRIPT);
        initStateMachine(settings, ASSIGN_USER_SCRIPT,
                STATEMACHINE_ASSIGN_USER);

        config.addUsersToParameter(users);

        InstanceStatusUsers result = new InstanceStatusUsers();
        result.setChangedUsers(users);
        result.setChangedParameters(settings.getParameters());
        result.setIsReady(false);
        return result;
    }

    @Override
    public InstanceStatus deactivateInstance(String instanceId,
                                             ProvisioningSettings settings) throws APPlatformException {

        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        return result;
    }

    @Override
    public InstanceStatus deleteInstance(String instanceId,
                                         ProvisioningSettings settings) throws APPlatformException {
        LOG.debug("instanceId: {}", instanceId);
        Configuration config = new Configuration(settings);
        validator.validate(config,
                ConfigurationKey.DEPROVISIONING_SCRIPT);
        initStateMachine(settings,
                ConfigurationKey.DEPROVISIONING_SCRIPT,
                STATEMACHINE_DEPROVISION);
        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        return result;
    }

    @Override
    public InstanceStatus deleteUsers(String instanceId,
                                      ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setRequestingUser();
        validator.validate(config, DEASSIGN_USER_SCRIPT);
        initStateMachine(settings, DEASSIGN_USER_SCRIPT,
                STATEMACHINE_DEASSIGN_USER);

        config.addUsersToParameter(users);

        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        result.setIsReady(false);
        return result;
    }

    @Override
    public InstanceStatus executeServiceOperation(String userId,
                                                  String instanceId, String transactionId, String operationId,
                                                  List<OperationParameter> parameters,
                                                  ProvisioningSettings settings)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setSetting(REQUESTING_USER_ID, userId);
        validator.validate(config, OPERATIONS_SCRIPT);
        config.setSetting(OPERATIONS_ID, operationId);
        initStateMachine(settings, OPERATIONS_SCRIPT,
                STATEMACHINE_OPERATION);
        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        return result;
    }

    @Override
    public List<LocalizedText> getControllerStatus(ControllerSettings arg0)
            throws APPlatformException {

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InstanceStatus getInstanceStatus(String instanceId,
                                            ProvisioningSettings settings) throws APPlatformException {

        ScriptLogger logger = new ScriptLogger();
        Configuration config = new Configuration(settings);
        InstanceStatus status = new InstanceStatus();
        StateMachine stateMachine;
        try {
            stateMachine = new StateMachine(settings);
            stateMachine
                    .executeAction(settings, instanceId, status);
        } catch (StateMachineException e) {
            LOG.error("Failed to getContent instance status", e);
            pool.terminateShell(instanceId);
            throw new APPlatformException(
                    "Failed to getContent instance status. " + e
                            .getMessage());
        }
        updateProvisioningSettings(instanceId, config, stateMachine);
        status.setChangedParameters(settings.getParameters());

        return status;
    }

    void updateProvisioningSettings(String instanceId, Configuration config,
                                    StateMachine stateMachine)
            throws SuspendException, APPlatformException {

        String nextState = stateMachine.getStateId();
        switch (nextState) {
            case ASYNC_ERROR:
                StringBuilder sb = new StringBuilder();
                sb.append("Asynchronous task failed.");
                if (config.getSetting(SM_ERROR_MESSAGE) != null) {
                    sb.append(" Error message: "
                            + config.getSetting(SM_ERROR_MESSAGE));
                }
                pool.terminateShell(instanceId);
                throw new APPlatformException(sb.toString());
            case SYNC_ERROR:
                pool.terminateShell(instanceId);
                throw new SuspendException(
                        config.getSetting(SM_ERROR_MESSAGE));
            default:
                config.setSetting(SM_STATE_HISTORY,
                        stateMachine.getHistory());
                config.setSetting(SM_STATE, nextState);
                break;
        }
    }

    @Override
    public List<OperationParameter> getOperationParameters(String arg0,
                                                           String arg1, String arg2, ProvisioningSettings arg3)
            throws APPlatformException {

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InstanceStatus modifyInstance(String instanceId,
                                         ProvisioningSettings settings, ProvisioningSettings newSettings)
            throws APPlatformException {

        LOG.debug("instanceId: {}", instanceId);
        Configuration config = new Configuration(newSettings);
        validator.validate(config, UPDATE_SCRIPT);
        config.setSetting(REQUESTING_USER_ID,
                settings.getRequestingUser().getUserId());
        initStateMachine(newSettings, UPDATE_SCRIPT,
                STATEMACHINE_UPDATE);
        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(newSettings.getParameters());
        return result;
    }

    @Override
    public InstanceStatus notifyInstance(String arg0,
                                         ProvisioningSettings arg1,
                                         Properties arg2) throws APPlatformException {
        return null;
    }

    @Override
    public InstanceStatus updateUsers(String instanceId,
                                      ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setRequestingUser();
        validator.validate(config, UPDATE_USER_SCRIPT);
        initStateMachine(settings, UPDATE_USER_SCRIPT,
                STATEMACHINE_UPDATE_USER);

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

}

/*******************************************************************************
 *
 *  COPYRIGHT (C) 2017 FUJITSU Limited - ALL RIGHTS RESERVED.
 *
 *  Creation Date: 02.02.2017
 *
 *******************************************************************************/

package org.oscm.app.powershell;

import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;
import static org.oscm.app.powershell.business.Configuration.CONTROLLER_ID;
import static org.oscm.app.powershell.business.ConfigurationKey.ASSIGN_USER_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.CHECK_STATUS_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.DEASSIGN_USER_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.DEPROVISIONING_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.INSTANCE_ID;
import static org.oscm.app.powershell.business.ConfigurationKey.OPERATIONS_ID;
import static org.oscm.app.powershell.business.ConfigurationKey.OPERATIONS_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.PROVISIONING_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.REQUESTING_USER_ID;
import static org.oscm.app.powershell.business.ConfigurationKey.SCRIPT_FILE;
import static org.oscm.app.powershell.business.ConfigurationKey.SM_ERROR_MESSAGE;
import static org.oscm.app.powershell.business.ConfigurationKey.SM_STATE;
import static org.oscm.app.powershell.business.ConfigurationKey.SM_STATE_HISTORY;
import static org.oscm.app.powershell.business.ConfigurationKey.UPDATE_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.UPDATE_USER_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.USER_COUNT;
import static org.oscm.app.powershell.business.ConfigurationKey.VERIFICATION_SCRIPT;
import static org.oscm.app.powershell.business.ConfigurationKey.VERIFICATION_TIMEOUT;
import static org.oscm.app.powershell.business.api.Powershell.VERIFICATION_MESSAGE;
import static org.oscm.app.powershell.business.api.PowershellStatus.RUNNING;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.oscm.app.powershell.business.Configuration;
import org.oscm.app.powershell.business.ConfigurationKey;
import org.oscm.app.powershell.business.PowershellControllerAccess;
import org.oscm.app.powershell.business.Script;
import org.oscm.app.powershell.business.api.Powershell;
import org.oscm.app.powershell.business.api.PowershellCommand;
import org.oscm.app.powershell.business.api.PowershellPool;
import org.oscm.app.powershell.business.api.PowershellStatus;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.InstanceStatusUsers;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.OperationParameter;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.bss.app.statemachine.StateMachine;
import com.fujitsu.bss.app.statemachine.api.StateMachineException;

@Remote(APPlatformController.class)
@Stateless(mappedName = "bss/app/controller/" + CONTROLLER_ID)
public class PowershellController implements APPlatformController {

    private static final Logger LOG = LoggerFactory
            .getLogger(PowershellController.class);

    private static final String ASYNC_ERROR = "ASYNC_ERROR";
    private static final String SYNC_ERROR = "ERROR";
    private static final String STATEMACHINE_PROVISION = "provision.xml";
    private static final String STATEMACHINE_DEPROVISION = "deprovision.xml";
    private static final String STATEMACHINE_UPDATE = "update.xml";
    private static final String STATEMACHINE_ASSIGN_USER = "assign_user.xml";
    private static final String STATEMACHINE_DEASSIGN_USER = "deassign_user.xml";
    private static final String STATEMACHINE_OPERATION = "operation.xml";
    private static final String STATEMACHINE_UPDATE_USER = "update_user.xml";

    private PowershellControllerAccess controllerAccess;

    @Inject
    PowershellPool pool;

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
        runVerificationScript(config);

        initStateMachine(settings, PROVISIONING_SCRIPT, STATEMACHINE_PROVISION);

        return id;
    }

    private void validateAllScripts(Configuration config)
            throws APPlatformException {
        ConfigurationKey[] scriptKeys = { PROVISIONING_SCRIPT,
                DEPROVISIONING_SCRIPT, UPDATE_SCRIPT, ASSIGN_USER_SCRIPT,
                DEASSIGN_USER_SCRIPT, CHECK_STATUS_SCRIPT };
        for (ConfigurationKey scriptKey : scriptKeys) {
            validateScript(config, scriptKey);
        }
    }

    private void validateScript(Configuration config,
            ConfigurationKey scriptKey) throws APPlatformException {
        String scriptFilename = "";
        try {
            scriptFilename = config.getSetting(scriptKey);
        } catch (Exception e) {
            throw new APPlatformException(
                    "Failed to read service parameter " + scriptKey);
        }

        if (!doesFileExist(scriptFilename)) {
            throw new APPlatformException(
                    "Script does not exist: " + scriptFilename);
        }

        String text;
        String interactiveCommand;
        try {
            Script script = new Script(scriptFilename);
            script.insertServiceParameter(config.getProvisioningSettings());
            text = script.get();
            interactiveCommand = getInteractiveCommand(text);
        } catch (Exception e) {
            throw new APPlatformException(e.getMessage());
        }

        if (interactiveCommand != null) {
            throw new APPlatformException(
                    "Interactive command \"" + interactiveCommand
                            + "\" found in script " + scriptFilename);
        }

        if (text.indexOf("END_OF_SCRIPT") < 0) {
            throw new APPlatformException(
                    "Missing output \"END_OF_SCRIPT\" in " + scriptFilename);
        }
    }

    void runVerificationScript(Configuration config)
            throws APPlatformException {
        Script script;
        try {
            script = new Script(config.getSetting(VERIFICATION_SCRIPT));
            script.insertServiceParameter(config.getProvisioningSettings());
        } catch (Exception e) {
            return;
        }

        try {
            PowershellCommand command = new PowershellCommand(script.get());

            try (Powershell shell = new Powershell();) {
                shell.lockPowerShell(config.getSetting(INSTANCE_ID));
                shell.runCommand(config.getSetting(INSTANCE_ID), command);
                long ref = currentTimeMillis();
                PowershellStatus rc;
                do {
                    rc = shell.consumeOutput(config.getSetting(INSTANCE_ID));
                    sleep(100);
                } while (rc == RUNNING && currentTimeMillis() - ref < valueOf(
                        config.getSetting(VERIFICATION_TIMEOUT)));
                String shellOutput = shell
                        .getOutput(config.getSetting(INSTANCE_ID));
                Pattern p = compile(format(".*%s=(.*?)$", VERIFICATION_MESSAGE),
                        MULTILINE);
                Matcher matcher = p.matcher(shellOutput);
                if (matcher.find()) {
                    throw new APPlatformException(
                            "Verification failed: " + matcher.group(1));
                }
            }
        } catch (APPlatformException e) {
            throw e;
        } catch (Exception e) {
            throw new APPlatformException(
                    "Verification failed because of an exception", e);
        }
    }

    private void initStateMachine(ProvisioningSettings settings,
            ConfigurationKey serviceParamKey, String statemachineFilename)
            throws APPlatformException {

        try {
            Configuration config = new Configuration(settings);
            Setting setting = new Setting(SCRIPT_FILE.name(),
                    config.getSetting(serviceParamKey));
            settings.getParameters().put(SCRIPT_FILE.name(), setting);
            StateMachine.initializeProvisioningSettings(settings,
                    statemachineFilename);
        } catch (Exception e) {
            LOG.error("Failed to initialize state machine "
                    + statemachineFilename + ". ", e);
            throw new APPlatformException(e.getMessage(), e);
        }
    }

    String getInteractiveCommand(String text) throws Exception {
        if (text.indexOf("Out-GridView") >= 0) {
            return "Out-GridView";
        }

        if (text.indexOf("Read-Host") >= 0) {
            return "Read-Host";
        }

        if (text.indexOf("ShowDialog") >= 0) {
            return "ShowDialog";
        }

        if (text.indexOf("PromptForChoice") >= 0) {
            return "PromptForChoice";
        }

        if (text.indexOf("Prompt") >= 0) {
            return "Prompt";
        }

        return null;
    }

    boolean doesFileExist(String filename) {
        if (filename.startsWith("http")) {
            return doesUrlExist(filename);
        }

        File f = new File(filename);
        return f.exists();
    }

    private boolean doesUrlExist(String url) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url)
                    .openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HTTP_OK);
        } catch (Exception e) {
            LOG.error("", e);
            return false;
        }
    }

    @Override
    public InstanceStatusUsers createUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setRequestingUser();
        validateScript(config, ASSIGN_USER_SCRIPT);
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
        validateScript(config, ConfigurationKey.DEPROVISIONING_SCRIPT);
        initStateMachine(settings, ConfigurationKey.DEPROVISIONING_SCRIPT,
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
        validateScript(config, DEASSIGN_USER_SCRIPT);
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
            List<OperationParameter> parameters, ProvisioningSettings settings)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setSetting(REQUESTING_USER_ID, userId);
        validateScript(config, OPERATIONS_SCRIPT);
        config.setSetting(OPERATIONS_ID, operationId);
        initStateMachine(settings, OPERATIONS_SCRIPT, STATEMACHINE_OPERATION);
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

        Configuration config = new Configuration(settings);
        InstanceStatus status = new InstanceStatus();
        StateMachine stateMachine;
        try {
            stateMachine = new StateMachine(settings);
            stateMachine.executeAction(settings, instanceId, status);
        } catch (StateMachineException e) {
            LOG.error("Failed to get instance status", e);
            pool.terminateShell(instanceId);
            throw new APPlatformException(
                    "Failed to get instance status. " + e.getMessage());
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
            throw new SuspendException(config.getSetting(SM_ERROR_MESSAGE));
        default:
            config.setSetting(SM_STATE_HISTORY, stateMachine.getHistory());
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
        validateScript(config, UPDATE_SCRIPT);
        config.setSetting(REQUESTING_USER_ID,
                settings.getRequestingUser().getUserId());
        initStateMachine(newSettings, UPDATE_SCRIPT, STATEMACHINE_UPDATE);
        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(newSettings.getParameters());
        return result;
    }

    @Override
    public InstanceStatus notifyInstance(String arg0, ProvisioningSettings arg1,
            Properties arg2) throws APPlatformException {
        return null;
    }

    @Override
    public InstanceStatus updateUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {

        Configuration config = new Configuration(settings);
        config.setRequestingUser();
        validateScript(config, UPDATE_USER_SCRIPT);
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

    @Inject
    public void setControllerAccess(final ControllerAccess access) {
        this.controllerAccess = (PowershellControllerAccess) access;
    }

}

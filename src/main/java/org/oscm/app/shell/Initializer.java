/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

@Singleton
@Startup
public class Initializer {

    private static final Logger LOG = LoggerFactory.getLogger(Initializer.class);

    private String LOG4J_TEMPLATE = "log4j2.properties.template";

    private long TIMER_DELAY_VALUE = 60000;

    private static final String CATALINA_HOME = "catalina.home";

    @Resource
    private TimerService timerService;

    private File logFile;

    private long logFileLastModified = 0;

    private boolean logFileWarning = false;

    public boolean isLogFileWarning() {
        return logFileWarning;
    }

    public long getLogFileLastModified() {
        return logFileLastModified;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    @Inject
    private ControllerAccess controllerAccess;

    public void setControllerAccess(final ControllerAccess controllerAccess) {
        this.controllerAccess = controllerAccess;
    }

    @PostConstruct
    private void postConstruct() {
        try {
            String instanceRoot = System.getProperty(CATALINA_HOME);
            if (instanceRoot != null) {
                File root = new File(instanceRoot);
                if (root.isDirectory()) {
                    String filePath = "/conf/log4j2." + controllerAccess.getControllerId() + ".properties";
                    logFile = new File(root, filePath);
                    if (!logFile.exists()) {
                        publishTemplateFile();
                    }
                    handleOnChange(logFile);
                    LOG.debug("Enable timer service for monitoring modification of " + logFile.getPath());
                    initializeTimerService();
                } else {
                    LOG.error("Failed to initialize log file: invalid instanceRoot " + instanceRoot);
                    logFile = null;
                }
            } else {
                LOG.error("Failed to initialize log file: missing system property '"+CATALINA_HOME+"'");
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize log file", e);
            logFile = null;
        }
    }

    private void initializeTimerService() {
        Collection<?> timers = timerService.getTimers();
        if (timers.isEmpty()) {
            timerService.createTimer(0, TIMER_DELAY_VALUE, null);
        }
    }

    private void publishTemplateFile() {
        try (InputStream is = controllerAccess.getClass().getClassLoader().getResourceAsStream(LOG4J_TEMPLATE)) {
            if (is == null) {
                LOG.warn("Template file not found: " + LOG4J_TEMPLATE);
            } else if (logFile.getParentFile().exists()) {
                FileUtils.writeByteArrayToFile(logFile, IOUtils.toByteArray(is));
            }
        } catch (Exception e) {
            // ignore
            LOG.error("Failed to publish template file from " + LOG4J_TEMPLATE + " to " + logFile.getAbsolutePath(), e);
        }
    }

    @Timeout
    public void handleTimer(@SuppressWarnings("unused") Timer timer) {
        if (logFile != null) {
            handleOnChange(logFile);
        }
    }

    void handleOnChange(File logFile) {
        try {
            long lastModified = logFile.lastModified();
            if (lastModified > logFileLastModified) {
                logFileLastModified = lastModified;
                LOG.debug("Reload log4j configuration from " + logFile.getAbsolutePath());
                final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                ctx.setConfigLocation(logFile.toURI());
                logFileWarning = false;
            }
        } catch (Exception e) {
            if (!logFileWarning) {
                logFileWarning = true;
                LOG.error(logFile.getAbsolutePath(), e);
            }
        }
    }
}

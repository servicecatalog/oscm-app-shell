package org.oscm.app.shell;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class Initializer {

    private static final Logger LOG = LoggerFactory.getLogger(Initializer.class);

    private String LOG4J_TEMPLATE = "log4j.properties.template";

    private long TIMER_DELAY_VALUE = 60000;

    @Resource
    private TimerService timerService;

    private File logFile;
    private long logFileLastModified = 0;
    private boolean logFileWarning = false;

    @Inject
    private ControllerAccess controllerAccess;

    public void setControllerAccess(final ControllerAccess controllerAccess) {
	this.controllerAccess = controllerAccess;
    }

    @PostConstruct
    private void postConstruct() {
	try {
	    String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
	    if (instanceRoot != null) {
		File root = new File(instanceRoot);
		if (root.isDirectory()) {
		    String filePath = "/config/log4j." + controllerAccess.getControllerId() + ".properties";
		    logFile = new File(root, filePath);
		    if (!logFile.exists()) {
			publishTemplateFile();
		    }
		    handleOnChange(logFile);
		    LOG.debug("Enable timer service for monitoring modification of " + logFile.getPath());
		    initialzeTimerService();
		} else {
		    LOG.error("Failed to initialize log file: invalid instanceRoot " + instanceRoot);
		    logFile = null;
		}
	    } else {
		LOG.error("Failed to initialize log file: missing system property 'com.sun.aas.instanceRoot'");
	    }
	} catch (Exception e) {
	    LOG.error("Failed to initialize log file", e);
	    logFile = null;
	}
    }

    private void initialzeTimerService() {
	Collection<?> timers = timerService.getTimers();
	if (timers.isEmpty()) {
	    timerService.createTimer(0, TIMER_DELAY_VALUE, null);
	}
    }

    private void publishTemplateFile() throws Exception {
	try (InputStream is = controllerAccess.getClass().getClassLoader().getResourceAsStream(LOG4J_TEMPLATE);) {
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
	    long lastModif = logFile.lastModified();
	    if (lastModif > logFileLastModified) {
		logFileLastModified = lastModif;
		LOG.debug("Reload log4j configuration from " + logFile.getAbsolutePath());
		new PropertyConfigurator().doConfigure(logFile.getAbsolutePath(), LogManager.getLoggerRepository());
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

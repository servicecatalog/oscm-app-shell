package org.oscm.app.shell;

import org.oscm.app.shell.business.Configuration;
import org.oscm.app.shell.business.ConfigurationKey;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;



public class ShellControllerLogger {

    //private static final Logger LOG = LoggerFactory.getLogger(ShellController.class);
    Logger extAppLogger = LoggerFactory.getLogger("ExternalAppLogger");



    private static String PATH_LOGS =   "/opt/apache-tomee/logs/app-shell/";

    public void safeLogsToFile(Configuration config, String scriptKey, String output){


        String fileName = scriptKey + "_LOGS";
        extAppLogger.warn("Jestem w aextAPPLogger");
        extAppLogger.info("***safeLogsToFile --> "+fileName +"***");
        extAppLogger.warn("output = " + output);
        BufferedWriter bw = null;
        try {
            File file = new File(PATH_LOGS + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(fileName);
            bw.write("Date 14/11/2018 10:57");
            bw.write("");
            bw.write("First log to ");
            bw.write(output);
            bw.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

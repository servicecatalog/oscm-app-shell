/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.powershell.business.api;

import java.util.ArrayList;

/**
 * PowerShell command output validation
 */
public class PowershellCommand {

    private static final String EMPTY_CMD = "";

    private ArrayList<String> output;
    private ArrayList<String> errorOutput;

    private PowershellStatus status;

    private String command;

    public PowershellCommand() {
	output = new ArrayList<>();
	errorOutput = new ArrayList<>();
	init(EMPTY_CMD);
    }

    public PowershellCommand(String cmnd) {
	output = new ArrayList<>();
	errorOutput = new ArrayList<>();
	init(cmnd);
    }

    public void init(String cmnd) {
	command = cmnd;
	output.clear();
	errorOutput.clear();
    }

    public String getCommand() {
	return command;
    }

    public ArrayList<String> getOutput() {
	return output;
    }

    public ArrayList<String> getError() {
	return errorOutput;
    }

    public PowershellStatus getReturnCode() {
	return status;
    }

    protected void addOutputLine(String line) {
	output.add(line);
    }

    protected void addErrorLine(String line) {
	errorOutput.add(line);
    }

    protected void setReturnCode(PowershellStatus code) {
	status = code;
    }
}

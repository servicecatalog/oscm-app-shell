/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.oscm.app.shell.business.script.ScriptType;

import java.util.ArrayList;

/**
 * Shell command output validation
 */
public class ShellCommand {

    private static final String EMPTY_CMD = "";

    private ArrayList<String> output;
    private ArrayList<String> errorOutput;

    private ShellStatus status;

    private String command;
    private ScriptType scriptType;

    public ShellCommand() {
        output = new ArrayList<>();
        errorOutput = new ArrayList<>();
        init(EMPTY_CMD);
    }

    public ShellCommand(String cmnd) {
        output = new ArrayList<>();
        errorOutput = new ArrayList<>();
        init(cmnd);
    }

    public void init(String cmnd) {
        command = cmnd;
        output.clear();
        errorOutput.clear();
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
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

    public ShellStatus getReturnCode() {
        return status;
    }

    protected void addOutputLine(String line) {
        output.add(line);
    }

    protected void addErrorLine(String line) {
        errorOutput.add(line);
    }

    protected void setReturnCode(ShellStatus code) {
        status = code;
    }
}

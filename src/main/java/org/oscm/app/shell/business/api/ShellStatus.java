/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.shell.business.api;

public enum ShellStatus {

    /**
     * If the script execution reached the last line
     */
    SUCCESS,

    /**
     * If command execution started and the last script line is not reached
     */
    RUNNING,

    /**
     * If the Shell could not write to the console, e.g. happens if a script
     * fails and the next command gets this broken Shell console.
     */
    STDIN_CLOSED,

    CALLERID_DOES_NOT_MATCH,

    PSSHELL_ERROR;

}

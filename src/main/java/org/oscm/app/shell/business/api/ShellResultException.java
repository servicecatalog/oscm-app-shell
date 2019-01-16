/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Dec 20, 2018
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

public class ShellResultException extends Exception {

    public ShellResultException(String message) {
        super(message);
    }

    public ShellResultException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *
 *  Creation Date: Aug 2, 2017                                                      
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import static javax.ejb.ConcurrencyManagementType.CONTAINER;
import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;
import static org.oscm.app.shell.business.api.ShellStatus.RUNNING;
import static org.oscm.app.shell.business.api.ShellStatus.STDIN_CLOSED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Lock;
import javax.ejb.Singleton;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a number of Shell sessions. The pool can grow up to 100 shells.
 */
@Singleton
@ConcurrencyManagement(CONTAINER)
public class ShellPool {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPool.class);
    private static final int MAX_NUM_SHELLS = 100;
    private static final String CODE_NO_SHELL_FOR_LOCK = "NO_SHELL_FOR_CALLERID";
    private static final String CODE_NO_FREE_SHELL_IN_POOL = "NO_FREE_SHELL_IN_POOL";

    ArrayList<Shell> shellPool;
    Semaphore shellctrl;

    @PostConstruct
    void initializeResources() {
        shellctrl = new Semaphore(MAX_NUM_SHELLS);
        shellPool = new ArrayList<>();
    }

    @PreDestroy
    void releaseResources() {
        for (Shell shell : shellPool) {
            shell.close();
            shellctrl.release();
        }

        shellPool.clear();
    }

    /**
     * Starts a Shell script in the first available Shell from a shell
     * pool. If all shells are busy a new shell is created and added to the pool.
     *
     * @param command          contains the script to be executed
     * @param lockId           identifies a shell in the pool
     * @param shellConsoleFile absolute filesystem path to Shell configuration file
     */
    @Lock(WRITE)
    public ShellStatus runCommand(ShellCommand command, String lockId, String shellConsoleFile)
            throws ShellPoolException, IOException, APPlatformException {
        for (Shell shell : shellPool) {

            if (shell.lockShell(lockId)) {
               
                ShellStatus result = shell.runCommand(lockId, command);

                if (result == STDIN_CLOSED) {
                    LOG.info("lockId: " + lockId + " remove shell from pool because stdin was closed");
                    shell.close();
                    shellPool.remove(shell);
                    shellctrl.release(1);
                    continue;
                }

                return RUNNING;
            }
        }

        if (shellctrl.tryAcquire()) {
            Shell newshell = new Shell(shellConsoleFile);
            newshell.lockShell(lockId);
            shellPool.add(newshell);
            LOG.debug("new shell created and locked for " + newshell.isLockedFor());

            return newshell.runCommand(lockId, command);
        }

        LOG.info("Couldn't acquire a Shell, maximum number of shells reached");
        throw new ShellPoolException(CODE_NO_FREE_SHELL_IN_POOL);
    }

    /**
     * Get all output that a shell has produced so far.
     */
    @Lock(READ)
    public String getShellOutput(String lockId) {
        for (Shell shell : shellPool) {
            if (lockId.equals(shell.isLockedFor())) {
                return shell.getOutput(lockId);
            }
        }

        LOG.warn(String.format("No shell found for lockId %s to read output", lockId));
        return "";
    }

    /**
     * Read the shell's output.
     */
    @Lock(WRITE)
    public ShellStatus consumeShellOutput(String lockId) throws ShellPoolException {
        for (Shell shell : shellPool) {
            if (lockId.equals(shell.isLockedFor())) {
                return shell.consumeOutput(lockId);
            }
        }

        LOG.warn("lockId: " + lockId + ", no shell found to consume output from.");
        throw new ShellPoolException(CODE_NO_SHELL_FOR_LOCK);
    }

    /**
     * Make the shell available for a new script execution.
     *
     * @param lockId identifies a shell in the pool
     */
    @Lock(WRITE)
    public void unlockShell(String lockId) {
        for (Shell shell : shellPool) {
            if (lockId.equals(shell.isLockedFor())) {
                shell.unlock();
                return;
            }
        }

        LOG.debug("No shell locked with lockId: " + lockId);
    }

    @Lock(WRITE)
    public void terminateShell(String lockId) {
        // TODO check: iterating and deleting?
        for (Shell shell : shellPool) {
            if (lockId.equals(shell.isLockedFor())) {
                shell.close();
                shellPool.remove(shell);
                shellctrl.release();

                LOG.debug(String.format("Closing shell with lockId %s and removing from pool", lockId));
                return;
            }
        }
    }

    @Lock(READ)
    public String getShellErrorOutput(String lockId) {
        for (Shell shell : shellPool) {
            if (lockId.equals(shell.isLockedFor())) {
                return shell.getErrorOutput(lockId);
            }
        }

        LOG.warn(String.format("No shell found for lockId %s to read error output", lockId));
        return "";
    }
}

/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Aug 2, 2017
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.shell.business.api;

import com.google.gson.Gson;
import org.oscm.app.shell.ScriptLogger;
import org.oscm.app.shell.business.api.json.ShellResult;
import org.oscm.app.shell.business.script.ScriptType;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.String.join;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.oscm.app.shell.business.api.ShellStatus.*;

/** Shell runtime used to execute Shell scripts */
public class Shell implements AutoCloseable {

  public static final String STATUS_ERROR = "error";
  public static final String STATUS_OK = "ok";

  private static final String INVALID_JSON_MESSAGE = "Invalid JSON returned after script execution";
  private static final Logger LOG = LoggerFactory.getLogger(Shell.class);

  private final Process shell;

  /**
   * Indicates if a shell is in use. A shell is free if the callerid is null. Otherwise an APP
   * instance set any callerid to lock the shell and additionally to aquire the shell again by the
   * callerid.
   */
  private volatile String lockId;

  /** Input stream of Shell console */
  private final BufferedWriter stdIn;

  /** Output of the Shell console */
  private final StreamGobbler stdOut;

  /** Error stream of Shell console */
  private final StreamGobbler stdErr;

  private ShellCommand command;

  public String getLockId() {
    return lockId;
  }

  public void setLockId(String lockId) {
    this.lockId = lockId;
  }

  public Shell(ScriptType scriptType) throws IOException, APPlatformException {

    switch (scriptType) {
      case SHELL:
        shell = Runtime.getRuntime().exec("sh");
        break;
      case POWERSHELL:
        shell = Runtime.getRuntime().exec("pwsh -Command -");
        break;
      default:
        throw new APPlatformException("Script type " + scriptType.name() + " not supported");
    }

    stdIn = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
    stdOut = new StreamGobbler(shell.getInputStream());
    stdErr = new StreamGobbler(shell.getErrorStream());

    if (!stdErr.buffer.isEmpty()) {
      throw new APPlatformException(
          "Shell initialization problem, error stream not empty: " + join("", stdErr.buffer));
    }

    if (!stdOut.buffer.isEmpty()) {
      stdOut.buffer.clear();
    }

    lockId = null;
  }

  public ShellStatus runCommand(final String lockId, final ShellCommand command) {
    if (!lockId.equals(this.lockId)) {
      LOG.error("shell called by " + lockId + ", but locked for " + this.lockId);
      return CALLERID_DOES_NOT_MATCH;
    }
    this.command = command;
    try {
      LOG.debug(String.format("lockId: %s, command:\n%s", lockId, command.getCommand()));
      ScriptLogger.logScriptCommand(command);
      stdIn.write(command.getCommand());
      stdIn.newLine();
      stdIn.flush();
    } catch (IOException e) {
      LOG.error("lockId: " + lockId + " failed to write command to shell stdin", e);
      return STDIN_CLOSED;
    }

    return RUNNING;
  }

  public String getOutput() {
    StringBuffer sb = new StringBuffer();
    for (String line : command.getOutput()) {
      sb.append(line);
      sb.append("\n\t");
    }
    String output = sb.toString();
    LOG.trace("lockId: " + lockId + " found shell with output: " + output);
    return output;
  }

  public String getErrorOutput() {
    StringBuffer sb = new StringBuffer();
    for (String line : command.getError()) {
      sb.append(line);
      sb.append("\n\t");
    }
    String output = sb.toString();
    LOG.trace("lockId: " + lockId + " found shell with error output: " + output);
    return output;
  }

  public ShellResult getResult() throws ShellResultException {

    if (!command.getError().isEmpty()) {
      ShellResult shellResult = new ShellResult();
      shellResult.setStatus(STATUS_ERROR);
      shellResult.setMessage(getErrorOutput());
      return shellResult;
    } else {
      ArrayList<String> output = command.getOutput();
      String jsonOutput = String.join("", output);
      try {
        Gson json = new Gson();
        ShellResult shellResult = json.fromJson(jsonOutput, ShellResult.class);
        validateJsonResult(shellResult);
        return shellResult;

      } catch (ShellResultException exception) {
        throw exception;
      } catch (Exception exception) {
        throw new ShellResultException(
            INVALID_JSON_MESSAGE + ": " + exception.getMessage(), exception);
      }
    }
  }

  private void validateJsonResult(ShellResult result) throws ShellResultException {
    if (result.getStatus() == null || result.getMessage() == null) {
      throw new ShellResultException(
          INVALID_JSON_MESSAGE + ": [status] and [message] fields are mandatory");
    }
  }

  public ShellStatus consumeOutput(String lockId) {
    if (!lockId.equals(this.lockId)) {
      LOG.error("shell called by " + lockId + ", but locked for " + this.lockId);
      return CALLERID_DOES_NOT_MATCH;
    }
    if (command.getReturnCode() == SUCCESS) {
      return SUCCESS;
    }

    return getCmdOutput();
  }

  public void unlock() {
    LOG.trace("callerid: " + lockId + " shell has been unlocked");
    lockId = null;
  }

  /**
   * returns lock status of Shell runtime
   *
   * @return lock status: true, if shell is locked / false, if shell is free
   */
  public boolean isLocked() {
    return (lockId == null ? false : true);
  }

  /**
   * returns lock status of Shell runtime utilizing id of calling command from API class
   *
   * @return caller id, if shell is locked / empty string, if shell is free
   */
  public String isLockedFor() {
    return (lockId == null ? "" : lockId);
  }

  /**
   * locks Shell runtime, if unlocked
   *
   * @return lock status: true, if shell has been free and is now locked / false, if shell was
   *     already locked and could not be locked
   */
  public boolean lockShell(String lockId) {
    if (this.lockId == null) {
      this.lockId = lockId;
      return true;
    }

    return false;
  }

  /**
   * Flushes all pipes (StdIn, StdOut, StdErr), interrupts gobbler threads and terminates Shell
   * runtime
   */
  @Override
  public void close() {
    try {
      stdIn.write("exit;");
      stdIn.flush();
    } catch (IOException ioe) {
      // ignore
    } finally {
      silentlyCloseStdIn();
    }

    stdOut.interrupt();
    stdErr.interrupt();
    shell.destroy();
  }

  private void silentlyCloseStdIn() {
    try {
      stdIn.close();
    } catch (IOException e) {
      // ignore
    }
  }

  /** Capture Shell script output. */
  private ShellStatus getCmdOutput() {
    ShellStatus status = RUNNING;

    while (!stdOut.buffer.isEmpty()) {
      String line = stdOut.buffer.remove(0);
      LOG.trace(String.format("lockId=%s, shell line:=%s ", lockId, line));

      if ("END_OF_SCRIPT".equals(line)) {
        if (!hasErrors()) {
          status = SUCCESS;
        } else {
          status = PSSHELL_ERROR;
        }
      } else {
        command.addOutputLine(line);
      }
    }

    if (!stdErr.buffer.isEmpty()) {
      while (!stdErr.buffer.isEmpty()) {
        String errorLine = stdErr.buffer.remove(0);
        LOG.trace("CallerId: " + lockId + " error line: " + errorLine);
        command.addErrorLine(errorLine);
      }
      status = PSSHELL_ERROR;
    }

    command.setReturnCode(status);
    return status;
  }

  private boolean hasErrors() {

    ExecutorService executor = Executors.newFixedThreadPool(1);
    Callable<Integer> readTask = () -> shell.getErrorStream().read();

    List<Integer> bytes = new ArrayList<>();
    int readByte = 1;
    try {
      while (readByte >= 0) {
        Future<Integer> future = executor.submit(readTask);
        readByte = future.get(500, MILLISECONDS);
        bytes.add(readByte);
      }
    } catch (TimeoutException e) {
      // ignore
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
    return bytes.size() > 0;
  }
}

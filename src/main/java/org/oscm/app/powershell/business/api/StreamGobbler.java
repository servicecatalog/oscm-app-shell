/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2017                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.powershell.business.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream gobbler for handling of StdOut/StdErr pipes on Java runtime
 * 
 * @author giese
 */
public class StreamGobbler extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream inputStream;

    protected final CopyOnWriteArrayList<String> buffer;

    public StreamGobbler(InputStream inputStream) {
	this.inputStream = inputStream;
	setDaemon(true);
	buffer = new CopyOnWriteArrayList<String>();
	start();
    }

    @Override
    public void run() {
	try (BufferedReader stream = new BufferedReader(new InputStreamReader(inputStream));) {
	    while (!isInterrupted()) {
		if (!stream.ready()) {
		    sleep(100);
		    continue;
		}

		final String line = stream.readLine();
		LOG.trace(line);
		buffer.add(line);
	    }
	} catch (IOException e) {
	    LOG.error("Interrupting thread due to an exception", e);
	} catch (InterruptedException e) {
	    // interrupting
	}
    }

}

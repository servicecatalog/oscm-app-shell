/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2017-10-17
 *
 *******************************************************************************/
package org.oscm.app.shell.ui.customtab;

import org.apache.commons.codec.binary.Base64;
import org.oscm.app.shell.business.api.json.ShellResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ContentHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentHandler.class);
    ShellResultData data;
    final static String BASE64_TOKEN = "base64:";

    ContentHandler(ShellResultData data) {
        this.data = data;
    }

    String decodeOutput() {
        final String content = data.getOutput();
        if (content.startsWith(BASE64_TOKEN)) {
            return getDecoded(content);
        }
        return content;
    }

    private String getDecoded(String content) {
        byte[] bytes = Base64.decodeBase64(content.substring(BASE64_TOKEN.length()));
        final String plain = new String(bytes, StandardCharsets.UTF_8);
        logForDebug(plain);
        return plain;
    }

    private void logForDebug(String txt) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(txt);
        }
    }

}

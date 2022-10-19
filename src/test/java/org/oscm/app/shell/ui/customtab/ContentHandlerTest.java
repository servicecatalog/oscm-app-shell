/*
 ******************************************************************************

 <p>Copyright FUJITSU LIMITED 2022

 <p>*****************************************************************************
*/
package org.oscm.app.shell.ui.customtab;

import org.junit.Test;
import org.oscm.app.shell.business.api.json.ShellResultData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author @goebel
 */
public class ContentHandlerTest {

    @Test
    public void getDecoded() {

        // given
        final String html = "<html><head><title>Sample</tile></head><body><h1>This is some HTML content!</h1></body></html>";
        final ShellResultData content = givenBase64Output(html);

        // when
        String txt = content.getOutput();
        String decoded = new ContentHandler(content).decodeOutput();

        // then
        assertTrue(txt.startsWith(ContentHandler.BASE64_TOKEN));
        assertEquals(html, decoded);
    }

    private ShellResultData givenPlainOutput() {
        String txt = "This is plain text!";
        ShellResultData data = new ShellResultData();
        data.setOutput("txt");
        return data;
    }

    private ShellResultData givenBase64Output(String html) {
        byte[] encoded = Base64.getEncoder().encode(html.getBytes(StandardCharsets.UTF_8));
        String str = new String(encoded, StandardCharsets.UTF_8);
        ShellResultData data = new ShellResultData();
        data.setOutput(ContentHandler.BASE64_TOKEN + str);
        return data;
    }
}

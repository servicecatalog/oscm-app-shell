/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: Jan 09, 2019
 *
 *******************************************************************************/

package org.oscm.app.shell.business.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.InputStream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StreamGobblerTest {

    @Mock
    InputStream inputStream;

    @InjectMocks
    @Spy
    StreamGobbler streamGobbler;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStreamGobbler_executesRunMethod_ifCreated() {

        //given

        //when

        //then
        verify(streamGobbler, times(1)).run();

    }
}

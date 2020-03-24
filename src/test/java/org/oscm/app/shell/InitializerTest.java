package org.oscm.app.shell;

/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Jan 09, 2019
 *
 *******************************************************************************/

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.ejb.Timer;
import java.io.File;

import static org.mockito.Mockito.*;

public class InitializerTest {

    @Mock
    File logFile;

    @Spy
    Initializer initializer;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleTimer_triggersHandleOnChange_ifLogFileNotNull() {

        //given
        Timer timer = mock(Timer.class);
        File logFile = mock(File.class);

        //when
        initializer.setLogFile(logFile);
        initializer.handleTimer(timer);

        //then
        verify(initializer, times(1)).handleOnChange(logFile);
    }

    @Test
    public void testHandleTimer_doesNothing_ifLogFileIsNull() {

        //given
        Timer timer = mock(Timer.class);

        //when
        initializer.setLogFile(null);
        initializer.handleTimer(timer);

        //then
        verify(initializer, times(0)).handleOnChange(logFile);
    }

    @Test
    public void testHandleOnChange_doesNothing_ifNothingChanged() {

        //given
        when(logFile.lastModified()).thenReturn((long) 1000);
        //doNothing().when(initializer).configurePropertyConfigurator(any(File.class));

        //when
        initializer.handleOnChange(logFile);

        //then
        Assert.assertEquals(1000, initializer.getLogFileLastModified());
        Assert.assertFalse(initializer.isLogFileWarning());
    }

    @Test
    public void testHandleOnChange_reloadsConfiguration_ifLogFileModified() {

        //given
        when(logFile.lastModified()).thenThrow(Exception.class);
        //doNothing().when(initializer).configurePropertyConfigurator(any(File.class));

        //when
        initializer.handleOnChange(logFile);

        //then
        Assert.assertTrue(initializer.isLogFileWarning());
    }

}

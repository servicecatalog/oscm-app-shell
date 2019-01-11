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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.FieldSetter;

import javax.ejb.Init;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class InitializerTest {

    @Mock
    File logFile;

    @InjectMocks
    @Spy
    Initializer initializer;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Ignore
    public void testInitializeTimerService_callsCreateTimer_ifCollectionEmpty()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        //given
        TimerService timerService = mock(TimerService.class);
        Method postConstructor = Initializer.class.getDeclaredMethod("postConstruct",null);
        postConstructor.setAccessible(true);

        //when
        when(timerService.getTimers()).thenReturn(mock(Collection.class));
        postConstructor.invoke(initializer);

        //then
        verify(timerService, times(1)).createTimer(anyInt(), any(), null);
    }

    @Ignore
    public void testInitializeTimerService_doesNotCallCreateTimer_ifCollectionNotEmpty()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        //given
        TimerService timerService = mock(TimerService.class);
        Collection<Timer> timers = mock(Collection.class);
        Method postConstructor = Initializer.class.getDeclaredMethod("postConstruct",null);
        postConstructor.setAccessible(true);

        //when
        timers.add(mock(Timer.class));
        when(timerService.getTimers()).thenReturn(timers);
        postConstructor.invoke(initializer);

        //then
        verify(timerService, never()).createTimer(anyInt(), any(), null);
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

    @Ignore
    public void testHandleTimer_doesNothing_ifLogFileIsNull() {

        //given
        Timer timer = mock(Timer.class);

        //when
        logFile = null;
        initializer.handleTimer(timer);

        //then
        verify(initializer, times(0)).handleOnChange(logFile);
    }

    @Ignore
    public void testHandleOnChange_doesNothing_ifNothingChanged() {

        //given
        Timer timer = mock(Timer.class);

        //when
        initializer.handleTimer(timer);

        //then
        //Assert.assertFalse(logFileWarning);
    }

    @Ignore
    public void testHandleOnChange_reloadsConfiguration_ifLogFileModified() {

        //given
        Timer timer = mock(Timer.class);

        //when
        initializer.handleTimer(timer);

        //then
        //Assert.assertTrue(logFileWarning);
    }

}

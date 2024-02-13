package com.extractor.xml.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogUtilTest {

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        listAppender = new ListAppender<>();
        listAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(listAppender);
    }

    @Test
    void calculateDurationAndGenerateLog() {
        long startTime = System.currentTimeMillis();

        LogUtil.calculateDurationAndGenerateLog(startTime);

        List<ILoggingEvent> logList = listAppender.list;
        assertEquals(1, logList.size());

        ILoggingEvent loggingEvent = logList.get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertTrue(loggingEvent.getMessage().startsWith("Method execution time: "));
    }
}

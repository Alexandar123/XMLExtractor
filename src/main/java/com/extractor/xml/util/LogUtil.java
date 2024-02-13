package com.extractor.xml.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class LogUtil {

    public static void calculateDurationAndGenerateLog(long startTime) {
        long endTime = System.currentTimeMillis();
        double executionTimeInSeconds = (endTime - startTime) / 1000.0;
        log.info("Method execution time: " + executionTimeInSeconds + " seconds");
    }
}

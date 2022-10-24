package com.turnitin.aysecrets;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger implements LambdaLogger {

    private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);

    public void log(final String message) {
        logger.info(message);
    }

    public void log(final byte[] message) {
        logger.info(new String(message));
    }
}

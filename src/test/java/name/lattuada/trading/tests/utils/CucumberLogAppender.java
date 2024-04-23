package name.lattuada.trading.tests.utils;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

import java.io.ByteArrayOutputStream;

public class CucumberLogAppender extends OutputStreamAppender<ILoggingEvent> {
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Override
    public void start() {
        setOutputStream(outContent);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"); // Set your desired log pattern here
        encoder.start();
        setEncoder(encoder);
        super.start();
    }

    public static String getLog() {
        return outContent.toString();
    }

    public static void clearLog() {
        outContent.reset();
    }
}

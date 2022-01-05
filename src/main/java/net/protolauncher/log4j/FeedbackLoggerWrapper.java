package net.protolauncher.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FeedbackLoggerWrapper {
    
    // Variables
    private static final List<ILogListener> listeners = new ArrayList<>();
    private final Logger logger;
    
    // Constructor
    public FeedbackLoggerWrapper(String name) {
        this.logger = LogManager.getLogger(name);
    }

    /**
     * Gets the wrapped logger.
     * Should not be used due to it not calling the listeners;
     * if a method is missing from this class, add it.
     * @return The wrapped {@link Logger}.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * {@link Logger#debug(String)} passthrough.
     * @see Logger#debug(String) 
     */
    public void debug(String message) {
        this.sendToLogListeners(message);
        logger.debug(message);
    }

    /**
     * {@link Logger#info(String)} passthrough.
     * @see Logger#info(String)
     */
    public void info(String message) {
        this.sendToLogListeners(message);
        logger.info(message);
    }

    /**
     * {@link Logger#error(String)} passthrough.
     * @see Logger#error(String)
     */
    public void error(String message) {
        this.sendToLogListeners(message);
        logger.error(message);
    }

    /**
     * {@link Logger#warn(String)} passthrough.
     * @see Logger#warn(String)
     */
    public void warn(String message) {
        this.sendToLogListeners(message);
        logger.warn(message);
    }

    /**
     * {@link Logger#fatal(String)} passthrough.
     * @see Logger#fatal(String)
     */
    public void fatal(String message) {
        this.sendToLogListeners(message);
        logger.fatal(message);
    }

    /**
     * {@link Logger#trace(String)} passthrough.
     * @see Logger#trace(String)
     */
    public void trace(String message) {
        this.sendToLogListeners(message);
        logger.trace(message);
    }

    /**
     * Sends the given message to all log listeners.
     * @param message The message to send.
     */
    private void sendToLogListeners(String message) {
        for (ILogListener listener : listeners) {
            listener.onLog(message);
        }
    }

    /**
     * Registers a new log listener.
     * @param listener The listener to add.
     */
    public static void registerListener(ILogListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an existing log listener.
     * @param listener The listener to remove.
     */
    public static void removeListener(ILogListener listener) {
        listeners.remove(listener);
    }
    
}

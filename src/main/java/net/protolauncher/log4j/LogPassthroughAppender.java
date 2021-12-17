package net.protolauncher.log4j;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is absolutely insane for a simple log passthrough.
 * Should just use a simple event system. Thanks, Log4j.
 */
@Plugin(name = "LogPassthroughAppender", category = "Core", elementType = "appender", printObject = true)
public final class LogPassthroughAppender extends AbstractAppender {

    // Listeners
    private static final List<ILogListener> listeners = new ArrayList<>();

    // Constructor
    private LogPassthroughAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    // AbstractAppender Implementation
    @Override
    public void append(LogEvent event) {
        for (ILogListener listener : listeners) {
            listener.onLog(event);
        }
    }

    // Plugin Creator
    @PluginFactory
    public static LogPassthroughAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new LogPassthroughAppender(name, filter, layout, true, Property.EMPTY_ARRAY);
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

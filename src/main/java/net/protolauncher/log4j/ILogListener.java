package net.protolauncher.log4j;

import org.apache.logging.log4j.core.LogEvent;

public interface ILogListener {

    void onLog(LogEvent event);

}

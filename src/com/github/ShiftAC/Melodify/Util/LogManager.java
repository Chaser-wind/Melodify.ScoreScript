package com.github.ShiftAC.Melodify.Util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager
{
    PrintStream dest;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");

    public static final int LOG_MESSAGE = 0;
    public static final int LOG_WARNING = 1;
    public static final int LOG_ERROR = 2;
    public static final int LOG_FATAL = 3;

    public int debug = 0;
    public int verbose = 0;

    public boolean timestamp = false;

    public LogManager(OutputStream os)
    {
        dest = new PrintStream(os);
    }

    public void logDebug(String msg, int level, int debugLevel)
    {
        if (debug >= debugLevel)
        {
            log(msg, level);
        }
    }

    public void logVerbose(String msg, int level, int verboseLevel)
    {
        if (verbose >= verboseLevel)
        {
            log(msg, level);
        }
    }

    public void logDebug(String who, String msg, int level, int debugLevel)
    {
        if (debug > debugLevel)
        {
            log(who, msg, level);
        }
    }

    public void logVerbose(String who, String msg, int level, int verboseLevel)
    {
        if (verbose >= verboseLevel)
        {
            log(who, msg, level);
        }
    }

    protected String generatePrefix(int level)
    {
        StringBuilder prefix = new StringBuilder();
        if (timestamp)
        {
            prefix.append("[");
            prefix.append(formatter.format(new Date()));
            prefix.append("]: ");
        }
        switch (level)
        {
        case LOG_MESSAGE:
            prefix.append("[Message]: ");
            break;
        case LOG_WARNING:
            prefix.append("[Warning]: ");
            break;
        case LOG_ERROR:
            prefix.append("[Error]: ");
            break;
        case LOG_FATAL:
            prefix.append("[Fatal Error]: ");
            break;
        }
        return prefix.toString();
    }

    public void log(String who, String msg, int level)
    {
        log("[" + who + "]: " + msg, level);
    }

    public void log(String msg, int level)
    {
        dest.println(generatePrefix(level) + msg);
    }
}
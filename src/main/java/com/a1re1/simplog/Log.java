package com.a1re1.simplog;

import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Log extends PrintStream {
    private static final Log LOG = new Log(Log.class);

    public static final int TRACE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;

    private final String clazzName;

    public Log(Class clazz) {
        super(System.out);
        this.clazzName = clazz.getName();
    }

    public interface Source { Object getData(); }

    //
    // 0 - ALL; 1 - DEBUG; 2 - INFO; 3 - WARN
    //
    private static int logLevel = 2; // Info Default Level

    public static void setLevel(int level) {
        logLevel = level;
        LOG.info("Log level set to {}.", level);
    }

    public static void setLevel(String level) {
        level = level.toLowerCase();
        if (level.equals("trace")) {
            logLevel = TRACE;
        } else if (level.equals("debug")) {
            logLevel = DEBUG;
        } else if (level.equals("info")) {
            logLevel = INFO;
        } else if (level.equals("warn")) {
            logLevel = WARN;
        } else if (level.equals("error")) {
            logLevel = ERROR;
        } else {
            logLevel = INFO;
            LOG.warn("Level not recognized: {}, falling back to INFO", level);
        }

        LOG.info("Log level set to {}.", level);
    }

    public void setVerbose(boolean enabled) {
        if (enabled) {
            setLevel(DEBUG);
        } else {
            setLevel(INFO);
        }
    }

    public void trace(String str, Source... srcs) {
        if (logLevel > TRACE) {
            return;
        }

        Object[] log = new Object[srcs.length + 1];
        log[0] = str;
        System.arraycopy(Arrays.stream(srcs)
                        .map(Source::getData).toArray(),
                0, log, 1, srcs.length);

        trace(log);
    }

    public void trace(Object... args) {
        if (logLevel > TRACE) {
            return;
        }

        logSout("[TRACE]", args);
    }

    public void debug(String str, Source... srcs) {
        if (logLevel > DEBUG) {
            return;
        }

        Object[] log = new Object[srcs.length + 1];
        log[0] = str;
        System.arraycopy(Arrays.stream(srcs)
                        .map(Source::getData).toArray(),
                0, log, 1, srcs.length);

        debug(log);
    }

    public void debug(Object... args) {
        if (logLevel > DEBUG) {
            return;
        }

        logSout("[DEBUG]", args);
    }

    public void info(String str, Source... srcs) {
        if (logLevel > INFO) {
            return;
        }

        Object[] log = new Object[srcs.length + 1];
        log[0] = str;
        System.arraycopy(Arrays.stream(srcs)
                        .map(Source::getData).toArray(),
                0, log, 1, srcs.length);

        info(log);
    }

    public void info(Object... args) {
        if (logLevel > INFO) {
            return;
        }

        logSout("[INFO]", args);
    }

    public void warn(String str, Source... srcs) {
        if (logLevel > WARN) {
            return;
        }

        Object[] log = new Object[srcs.length + 1];
        log[0] = str;
        System.arraycopy(Arrays.stream(srcs)
                        .map(Source::getData).toArray(),
                0, log, 1, srcs.length);

        warn(log);
    }

    public void warn(Object... args) {
        if (logLevel > WARN) {
            return;
        }

        logSout("[WARN]", args);
    }

    public void error(String str, Source... srcs) {
        if (logLevel > ERROR) {
            return;
        }

        Object[] log = new Object[srcs.length + 1];
        log[0] = str;
        System.arraycopy(Arrays.stream(srcs)
                        .map(Source::getData).toArray(),
                0, log, 1, srcs.length);

        error(log);
    }

    public void error(Object... args) {
        if (logLevel > ERROR) {
            return;
        }

        logErr("[ERROR]", args);
        for (Object o : args) {
            if (o instanceof Exception) {
                ((Exception) o).printStackTrace();
            }
        }
    }

    private void logSout(String tag, Object... args) {
        log(tag, System.out, args);
    }

    private void logErr(String tag, Object... arg) {
        log(tag, System.err, arg);
    }

    private static int countMatches(String str) {
        return (str.split( Pattern.quote("{}"), -1).length) - 1;
    }

    private void log(String label, PrintStream printStream, Object... args) {
        StringBuilder format = new StringBuilder(args[0].toString());

        int count = countMatches(format.toString());
        int arguments = args.length - 1;

        if (count < arguments) {
            format.append(", {}".repeat(java.lang.Math.max(0, arguments - count)));
        } else if (arguments < count) {
            System.err.println(getTag(label) + "Please supply positional arguments for all {}.");
            new Exception("Bad Arguments").printStackTrace();
            return;
        }

        format = new StringBuilder(format.toString().replace("{}", "%s"));

        List<Object> tmp  = Arrays.asList(args).subList(1, args.length);
        List<Exception> exceptions = new ArrayList<>();
        String[] pos = tmp.stream().map(obj -> {
            if (obj instanceof Exception) {
                exceptions.add((Exception) obj);
                return ((Exception) obj).getMessage();
            }
            if (obj == null) return "null";
            return obj.toString();
        }).toArray(String[]::new);

        printStream.printf(getTag(label) + format + "\n", pos);
        exceptions.forEach(Exception::printStackTrace);
    }

    private String getTag(String label) {
        return label + " | "
                + Instant.now()
                + " | "
                + clazzName
                + " | ";
    }

    @Override
    public void println(Object x) {
        LOG.info(x);
    }

    @Override
    public void println(String x) {
        LOG.info(x);
    }
}

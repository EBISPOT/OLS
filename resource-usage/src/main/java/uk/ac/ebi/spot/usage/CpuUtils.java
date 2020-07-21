package uk.ac.ebi.spot.usage;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpuUtils {
    public static void logCpuTime(Logger logger, String msg) {
        logger.debug(msg + " - Cpu time: " + getCpuTime());
    }

    /** Get CPU time in nanoseconds. */
    public static long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadCpuTime( ) : 0L;
    }

    public static void logUserTime(Logger logger, String msg) {
        logger.debug(msg + " - User time: " + getUserTime());
    }

    /** Get user time in nanoseconds. */
    public static long getUserTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadUserTime( ) : 0L;
    }

    public static void logSystemTime(Logger logger, String msg) {
        logger.debug(msg + " - User time: " + getSystemTime());
    }

    /** Get system time in nanoseconds. */
    public static long getSystemTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                (bean.getCurrentThreadCpuTime( ) - bean.getCurrentThreadUserTime( )) : 0L;
    }

    public static void logAllTime(Logger logger, String msg) {
        logCpuTime(logger, msg);
        logUserTime(logger, msg);
        logSystemTime(logger, msg);
    }
}

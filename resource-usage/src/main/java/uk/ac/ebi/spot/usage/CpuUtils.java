package uk.ac.ebi.spot.usage;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import org.slf4j.Logger;

public class CpuUtils {
    private static long nanoSecondToSecond(long nanoSecond) {
        return nanoSecond/1000/1000/1000;
    }

    public static void logCpuTime(Logger logger, String marker, String msg, String separator) {
        logger.info(marker + msg + separator + "Cpu time" + separator +
                nanoSecondToSecond(getCpuTime()) + " seconds.");
    }

    /** Get CPU time in nanoseconds. */
    public static long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadCpuTime( ) : 0L;
    }

    public static void logUserTime(Logger logger, String marker, String msg, String separator) {
        logger.info(marker + msg + separator + "User time" + separator +
                nanoSecondToSecond(getUserTime()) + " seconds.");
    }

    /** Get user time in nanoseconds. */
    public static long getUserTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                bean.getCurrentThreadUserTime( ) : 0L;
    }

    public static void logSystemTime(Logger logger, String marker, String msg, String separator) {
        logger.info(marker + msg + separator + "System time" + separator +
                nanoSecondToSecond(getSystemTime()) + " seconds.");
    }

    /** Get system time in nanoseconds. */
    public static long getSystemTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
                (bean.getCurrentThreadCpuTime( ) - bean.getCurrentThreadUserTime( )) : 0L;
    }

    public static void logAllTime(Logger logger, String marker, String msg, String separator) {
        logCpuTime(logger, marker, msg, separator);
        logUserTime(logger, marker, msg, separator);
        logSystemTime(logger, marker, msg, separator);
    }
}

package uk.ac.ebi.spot.usage;

import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryUtils {
    private static long bytesToMegaBytes(long bytes) {
        return bytes/1024/1024;
    }

    public static void logRuntimeMemoryUsage(Logger logger, String marker, String msg, String separator) {
        Runtime runtime = Runtime.getRuntime();
        logger.info(marker + msg + separator + "Runtime memory usage" + separator +
                bytesToMegaBytes(runtime.totalMemory() - runtime.freeMemory()) + " MB");
    }

    public static void logHeapAndNonHeapMemoryUsage(Logger logger, String marker, String msg, String separator) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        logger.info(marker + msg + separator + "Heap memory usage" + separator  +
                bytesToMegaBytes(memoryMXBean.getHeapMemoryUsage().getUsed()) + " MB");
        logger.info(marker + msg + separator + "Non-Heap memory usage" + separator +
                bytesToMegaBytes(memoryMXBean.getNonHeapMemoryUsage().getUsed()) + " MB");
    }
}

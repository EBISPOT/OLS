package uk.ac.ebi.spot.usage;

import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryUtils {
    private static long bytesToMegaBytes(long bytes) {
        return bytes/1024/1024;
    }

    public static void logRuntimeMemoryUsage(Logger logger, String msg) {
        Runtime runtime = Runtime.getRuntime();
        logger.debug(msg + " - Runtime memory usage: " +
                bytesToMegaBytes(runtime.totalMemory() - runtime.freeMemory()) + " MB");
    }

    public static void logHeapAndNonHeapMemoryUsage(Logger logger, String msg) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        logger.debug(msg + " - Heap memory usage: " +
                bytesToMegaBytes(memoryMXBean.getHeapMemoryUsage().getUsed()) + " MB");
        logger.debug(msg + " - Non-Heap memory usage: " +
                bytesToMegaBytes(memoryMXBean.getNonHeapMemoryUsage().getUsed()) + " MB");
    }
}

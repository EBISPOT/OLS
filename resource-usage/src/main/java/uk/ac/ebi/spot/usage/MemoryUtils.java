package uk.ac.ebi.spot.usage;

import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryUtils {
    public static void logRuntimeMemoryUsage(Logger logger, String msg) {
        Runtime runtime = Runtime.getRuntime();
        logger.debug(msg + " - Memory usage: " + ((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024) + " MB");
    }

    public static void logHeapAndNonHeapMemoryUsage(Logger logger, String msg) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        logger.debug(msg + " - Heap memory usage: " + memoryMXBean.getHeapMemoryUsage().getUsed());
        logger.debug(msg + " - Non-Heap memory usage: " + memoryMXBean.getNonHeapMemoryUsage().getUsed());
    }
}

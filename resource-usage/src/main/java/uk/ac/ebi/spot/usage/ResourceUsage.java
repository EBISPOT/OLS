package uk.ac.ebi.spot.usage;

import org.slf4j.Logger;

public class ResourceUsage {
    public static void logUsage(Logger logger, String marker, String msg, String separator) {
        MemoryUtils.logHeapAndNonHeapMemoryUsage(logger, marker, msg, separator);
        MemoryUtils.logRuntimeMemoryUsage(logger, marker, msg, separator);
        CpuUtils.logAllTime(logger, marker, msg, separator);
    }
}

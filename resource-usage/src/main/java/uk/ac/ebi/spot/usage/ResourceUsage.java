package uk.ac.ebi.spot.usage;

import org.slf4j.Logger;

public class ResourceUsage {
    public static void logUsage(Logger logger, String msg) {
        MemoryUtils.logHeapAndNonHeapMemoryUsage(logger, msg);
        MemoryUtils.logRuntimeMemoryUsage(logger, msg);
        CpuUtils.logAllTime(logger, msg);
    }
}

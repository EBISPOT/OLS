package uk.ac.ebi.spot.ols.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
	
	private TestUtils() {
		// TODO Auto-generated constructor stub
	}

	
	
    static void deleteTestDirectory(String rootDir) {
		File rootDirAsFile = new File(FileSystems.getDefault().getPath(rootDir).toString());
		try {
			FileUtils.deleteDirectory(rootDirAsFile);
		} catch (IOException e) {
			logger.debug(rootDir + " directory could not be deleted", e);
		}
	}	
}

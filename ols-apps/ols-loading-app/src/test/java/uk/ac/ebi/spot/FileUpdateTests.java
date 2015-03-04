package uk.ac.ebi.spot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.spot.ols.exception.FileUpdateServiceException;
import uk.ac.ebi.spot.ols.util.FileUpdater;

import java.net.URI;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FileUpdater.class)
public class FileUpdateTests {

    @Autowired
    FileUpdater updater;

    @Autowired
    private Environment environment;


    @Test
    public void testFileUpdater () {

        updater.setPath("/Users/jupp/Dropbox/dev/ols/ols-loader/src/test/resources/tmp");

        FileUpdater.FileStatus status = null;
        try {
            status = updater.getFile("ORDO", URI.create("http://www.orphadata.org/data/ORDO/ordo_orphanet.owl.zip"));
        } catch (FileUpdateServiceException e) {
            e.printStackTrace();
        }

        System.out.println(status.isNew());


    }


}

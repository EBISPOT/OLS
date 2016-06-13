package uk.ac.ebi.spot.ols;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.spot.ols.xrefs.OboDatabaseImpl;
import uk.ac.ebi.spot.ols.xrefs.OboXrefLoader;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Simon Jupp
 * @date 13/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OboXrefLoaderTest  extends TestCase {

    OboXrefLoader databaseService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        databaseService = new OboXrefLoader("db-xrefs.yaml");

    }

    @Test
    public void testNumberOfDatabases () {

        assertEquals(databaseService.getDatabases().size(), 218);

    }

    @Test
    public void testProDom () {

        assertTrue(databaseService.findByName("ProDom").isPresent());
        assertEquals(databaseService.findByName("ProDom").get().getDatabaseId(), "ProDom");
        assertEquals(databaseService.findByName("ProDom").get().getDatabaseName(), "ProDom protein domain families");
        try {
            assertEquals(databaseService.findByName("ProDom").get().getUrlForId("test").toString(), new URL("http://prodom.prabi.fr/prodom/current/cgi-bin/request.pl?question=DBEN&query=test").toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }
}

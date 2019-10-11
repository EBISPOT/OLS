package uk.ac.ebi.spot.ols;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ebi.spot.ols.xrefs.OboXrefLoader;

/**
 * @author Simon Jupp
 * @date 13/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class OboXrefLoaderTest {

    OboXrefLoader databaseService;

    @BeforeEach
    public void setUp() throws Exception {
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

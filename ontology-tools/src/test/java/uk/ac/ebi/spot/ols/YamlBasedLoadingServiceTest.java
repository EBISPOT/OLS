package uk.ac.ebi.spot.ols;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.spot.ols.config.YamlBasedLoadingService;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 29/09/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class YamlBasedLoadingServiceTest extends TestCase {

    private LinkedHashMap ontology;
    private YamlBasedLoadingService yamlBasedLoadingService;

    @Before
    public void setUp() throws Exception {

        this.ontology = new LinkedHashMap();
        ontology.put("id", "foo");
        ontology.put("title", "foo ontology");
        ontology.put("preferredPrefix", "FoO");

    }


    @Test
    public void testTitleEmpty () {

        ontology.remove("title");
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("foo", yamlBasedLoadingService.getConfiguration().getTitle());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testTitle () {

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("foo ontology", yamlBasedLoadingService.getConfiguration().getTitle());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testPrefixEmpty () {
        ontology.remove("preferredPrefix");

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("FOO", yamlBasedLoadingService.getConfiguration().getPreferredPrefix());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testPrefix () {

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("FoO", yamlBasedLoadingService.getConfiguration().getPreferredPrefix());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testUri () {

        ontology.put("uri", "http://example1.com/foo.owl");

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("http://example1.com/foo.owl", yamlBasedLoadingService.getConfiguration().getId());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testBase () {

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("http://foobar.com/foo.owl", yamlBasedLoadingService.getConfiguration().getId());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testBase2 () {

        LinkedHashMap productMap =  new LinkedHashMap<>();
        productMap.put("id", "bar.owl");
        productMap.put("is_canonical", true);
        List products = new ArrayList();
        products.add(productMap);
        ontology.put("products", products);

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("http://foobar.com/bar.owl", yamlBasedLoadingService.getConfiguration().getId());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testBase3 () {

        LinkedHashMap productMap =  new LinkedHashMap<>();
        productMap.put("id", "foobar.owl");
        productMap.put("is_canonical", false);
        List products = new ArrayList();
        products.add(productMap);
        ontology.put("products", products);

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("http://foobar.com/foobar.owl", yamlBasedLoadingService.getConfiguration().getId());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    public void testLocation () {

        ontology.put("ontology_purl", "http://foobar.com/foobar.owl");

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertEquals("http://foobar.com/foobar.owl", yamlBasedLoadingService.getConfiguration().getFileLocation().toString());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testLocation1 () {

        LinkedHashMap productMap =  new LinkedHashMap<>();
        productMap.put("id", "http://foobar.com/location/foobar.owl");
        productMap.put("is_canonical", false);
        List products = new ArrayList();
        products.add(productMap);
        ontology.put("products", products);

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, null,  false);
        try {
            assertEquals("http://foobar.com/location/foobar.owl", yamlBasedLoadingService.getConfiguration().getFileLocation().toString());
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testLocation2 () {

        LinkedHashMap productMap =  new LinkedHashMap<>();
        productMap.put("id", "foobar.owl");
        productMap.put("is_canonical", false);
        List products = new ArrayList();
        products.add(productMap);
        ontology.put("products", products);

        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/",  false);
        try {
            assertEquals("http://foobar.com/foobar.owl", yamlBasedLoadingService.getConfiguration().getFileLocation().toString());
        } catch (ConfigParsingException e) {
            fail();
        }
    }
}

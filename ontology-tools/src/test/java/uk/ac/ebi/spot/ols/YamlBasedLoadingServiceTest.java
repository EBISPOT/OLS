package uk.ac.ebi.spot.ols;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.ebi.spot.ols.config.OboDefaults;
import uk.ac.ebi.spot.ols.config.YamlBasedLoadingService;
import uk.ac.ebi.spot.ols.exception.ConfigParsingException;
import uk.ac.ebi.spot.ols.loader.AbstractOWLOntologyLoader;

import java.net.URI;
import java.util.*;

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
        ontology.put("ontology_purl", "http://www.foo.com/bar");

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
            assertEquals("http://foobar.com/foo", yamlBasedLoadingService.getConfiguration().getId());
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
    public void testDefinition () {

        ontology.put("uri", "http://example1.com/foo.owl");
        Collection defs = new ArrayList<>();

        defs.add("http://example1.com/definition1");
        defs.add("http://example1.com/definition2");
        ontology.put("definition_property", defs);
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertTrue(yamlBasedLoadingService.getConfiguration().getDefinitionProperties().size() == 2);
            assertTrue(yamlBasedLoadingService.getConfiguration().getDefinitionProperties().contains(URI.create("http://example1.com/definition1")));
            assertTrue(yamlBasedLoadingService.getConfiguration().getDefinitionProperties().contains(URI.create("http://example1.com/definition2")));
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testOBODefinition () {

        ontology.put("uri", "http://example1.com/foo.owl");
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", true);
        try {
            assertTrue(yamlBasedLoadingService.getConfiguration().getDefinitionProperties().size() == 1);
            assertTrue(yamlBasedLoadingService.getConfiguration().getDefinitionProperties().contains(URI.create(OboDefaults.DEFINITION)));
        } catch (ConfigParsingException e) {
            fail();
        }
    }


    @Test
    public void testSynonym () {

        ontology.put("uri", "http://example1.com/foo.owl");
        Collection syns = new ArrayList<>();

        syns.add("http://example1.com/synonym1");
        syns.add("http://example1.com/synonym2");
        ontology.put("synonym_property", syns);
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertTrue(yamlBasedLoadingService.getConfiguration().getSynonymProperties().size() == 2);
            assertTrue(yamlBasedLoadingService.getConfiguration().getSynonymProperties().contains(URI.create("http://example1.com/synonym1")));
            assertTrue(yamlBasedLoadingService.getConfiguration().getSynonymProperties().contains(URI.create("http://example1.com/synonym2")));
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testOboSynonym () {

        ontology.put("uri", "http://example1.com/foo.owl");
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", true);
        try {
            assertTrue(yamlBasedLoadingService.getConfiguration().getSynonymProperties().size() == 1);
            assertTrue(yamlBasedLoadingService.getConfiguration().getSynonymProperties().contains(URI.create(OboDefaults.EXACT_SYNONYM)));
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testHiddenProp () {

        ontology.put("uri", "http://example1.com/foo.owl");
        Collection prop = new ArrayList<>();

        prop.add("http://example1.com/hidden");
        ontology.put("hidden_property", prop);
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertTrue(yamlBasedLoadingService.getConfiguration().getHiddenProperties().size() == 1);
            assertTrue(yamlBasedLoadingService.getConfiguration().getHiddenProperties().contains(URI.create("http://example1.com/hidden")));
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public void testHierarchicalProp () {

        ontology.put("uri", "http://example1.com/foo.owl");
        Collection prop = new ArrayList<>();

        prop.add("http://example1.com/hierarchical");
        ontology.put("hierarchical_property", prop);
        yamlBasedLoadingService = new YamlBasedLoadingService(ontology, "http://foobar.com/", false);
        try {
            assertTrue(yamlBasedLoadingService.getConfiguration().getHierarchicalProperties().size() == 1);
            assertTrue(yamlBasedLoadingService.getConfiguration().getHierarchicalProperties().contains(URI.create("http://example1.com/hierarchical")));
        } catch (ConfigParsingException e) {
            fail();
        }
    }

    @Test
    public static void testOboVersionIriDate ( ) {

        String versionsIri = "http://purl.obolibrary.org/obo/obi/2009-11-06/obi.owl";

        String version = AbstractOWLOntologyLoader.parseOboVersion(IRI.create(versionsIri));

        assertEquals("2009-11-06", version);


    }
}

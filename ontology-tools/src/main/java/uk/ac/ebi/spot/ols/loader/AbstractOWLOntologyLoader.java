package uk.ac.ebi.spot.ols.loader;

import com.google.common.collect.Multimap;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import uk.ac.ebi.spot.ols.config.OboDefaults;
import uk.ac.ebi.spot.ols.config.OntologyDefaults;
import uk.ac.ebi.spot.ols.config.OntologyLoadingConfiguration;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.renderer.OWLHTMLVisitor;
import uk.ac.ebi.spot.ols.util.*;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;
import uk.ac.ebi.spot.usage.CpuUtils;
import uk.ac.ebi.spot.usage.MemoryUtils;
import uk.ac.ebi.spot.usage.ResourceUsage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 03/02/2015
 *
 * This Abstract class provies an OWL API based implementation of an ontology loader. Ontologies are loaded
 * and various caches are created for extracting  common slices out of the ontology.
 * This class has being doing the rounds in various guises for a while now and had become a bit unwieldy
 * todo refactor do include individual processors for various aspects of the ontology we want to extract
 *
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public abstract class
AbstractOWLOntologyLoader extends Initializable implements OntologyLoader {

    private static final Pattern oboIdFragmentPattern = Pattern.compile("(^([A-Za-z0-9_]*)_([A-Za-z0-9]*)$)");

    private IRI ontologyIRI;
    private IRI ontologyVersionIRI;
    private String ontologyName;
    private String ontologyTitle;
    private String ontologyDescription;
    private String ontologyHomePage;
    private String ontologyMailingList;
    private String ontologyTracker;
    private String ontologyLogo;
    private String version;
    private Collection<String> ontologyCreators;
    private Map<String, Collection<String>> ontologyAnnotations;
    private Resource ontologyResource;
    private Map<IRI, IRI> ontologyImportMappings;

    private Collection<IRI> classes  = new HashSet<IRI>();
    private Collection<IRI> individuals  = new HashSet<IRI>();
    private Collection<IRI> objectProperties  = new HashSet<IRI>();
    private Collection<IRI> dataProperties  = new HashSet<IRI>();
    private Collection<IRI> annotationProperties  = new HashSet<IRI>();
    private Collection<IRI> owlVocabulary  = new HashSet<IRI>();

    private IRI labelIRI = Namespaces.RDFS.createIRI("label");
    private Collection<IRI> synonymIRIs = new HashSet<IRI>();
    private Collection<IRI> definitionIRIs  = new HashSet<IRI>();

    // Henriette To do: Remove
    private Collection<IRI> hiddenIRIs  = new HashSet<IRI>();

    private Collection<IRI> unsatisfiableIris  = new HashSet<IRI>();

    private IRI exclusionClassIRI;
    private IRI exclusionAnnotationIRI;

    private OWLOntologyManager manager;
    private OWLDataFactory factory;
    private OWLOntology ontology;
    
    private boolean OWLAPIInitialized = false;

    private Collection<String> baseIRIs = new HashSet<>();

    private Map<IRI, String> ontologyAccessions = new HashMap<>();
    private Map<IRI, String> oboIds = new HashMap<>();
    private Map<IRI, String> ontologyLabels = new HashMap<>();
    private Map<IRI, Collection<String>> ontologySynonyms = new HashMap<>();
    private Map<IRI, Collection<String>> ontologyDefinitions = new HashMap<>();
    private Map<IRI, Map<IRI,Collection<String>>> termAnnotations = new HashMap<>();
    private Collection<IRI> obsoleteTerms = new HashSet<>();
    private Map<IRI, Collection<String>> slims = new HashMap<>();
    private Map<IRI, String> termReplacedBy = new HashMap<>();

    /**
     * The set of terms that have the same IRI as the ontology currently being loaded.
     */
    private Collection<IRI> localTerms = new HashSet<>();

    /**
     * Terms that have owl:Thing as direct parent.
     */
    private Collection<IRI> rootTerms = new HashSet<>();

    private Map<IRI, Collection<IRI>> directParentTerms = new HashMap<>();

    /**
     * The direct types of individuals.
     */
    private Map<IRI, Collection<IRI>> directTypes = new HashMap<>();
    /**
     * A map of all the terms that are ancestors of a term.
     */
    private Map<IRI, Collection<IRI>> allParentTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> directChildTerms = new HashMap<>();
    /**
     * A map of all descendants of a term.
     */
    private Map<IRI, Collection<IRI>> allChildTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> equivalentTerms = new HashMap<>();

    /**
     * A map of a map of the property IRIs and the class IRIs the property IRI are related to for
     * each term in the ontology.
     */
    private Map<IRI, Map<IRI,Collection<IRI>>> relatedTerms = new HashMap<>();

    /**
     * A map of the terms that can be considered to be related to a term via a "hierarchical
     * relation". Here "hierarchical relation" refers to a property IRI that is considered to be
     * hierarchical and is stored in {@link #hierarchicalRels}. A term is related to another term
     * via such hierarchical relation if it is a subclass of existential restriction using such a
     * property IRI. This map is generated based on the IRIs stored in {@link #hierarchicalRels} and
     * the {@link #isPartOf(IRI)} method.
     */
    private Map<IRI, Map<IRI,Collection<IRI>>> relatedParentTerms = new HashMap<>();

    /**
     * This is a map of the terms that are subclasses of existential restrictions using via property
     * IRIs that are considered to be hierarchical (i.e. stored in {@link #hierarchicalRels}) or for
     * which the {@link #isPartOf(IRI)} method returns true. Any class that is a subclass of such a
     * hierarchical relation is considered to be a related child of the filler of the given property
     * IRIs.
     */
    private Map<IRI, Collection<IRI>> relatedChildTerms = new HashMap<>();

    // Henriette To do: Is this used?
    private Map<IRI, Map<IRI,Collection<IRI>>> allRelatedTerms = new HashMap<>();
    /**
     * A map of the individuals which have existential restrictions as type where the filler of the
     * existential restriction is a nominal. For each such individual a map is stored of the related
     * property IRI and its (n) filler(s).
     */
    private Map<IRI, Map<IRI,Collection<IRI>>> allRelatedIndividuals = new HashMap<>();
    private Map<IRI, Map<IRI,Collection<IRI>>> allRelatedIndividualsToClasses = new HashMap<>();
    /**
     * A map of the individuals which have existential restrictions as type where the filler of the
     * existential restriction is a class. For each such individual a map is stored of the related
     * property IRI and its (class) filler(s).
     */
    private Map<IRI, Map<IRI,Collection<IRI>>> allRelatedClassesToIndividuals = new HashMap<>();
    /**
     * Terms that have defined by the ontology designer as being preferred root terms for the
     * ontology.
     */
    private Collection<IRI> preferredRootTerms = new HashSet<>();

    private Map<IRI, Collection<OBODefinitionCitation>> oboDefinitionCitations = new HashMap<>();
    private Map<IRI, Collection<OBOXref>> oboXrefs = new HashMap<>();
    private Map<IRI, Collection<OBOSynonym>> oboSynonyms = new HashMap<>();


    private Collection<IRI> hierarchicalRels = new HashSet<>();
    private Collection<String> internalMetadataProperties = new HashSet<>();

    private ShortFormProvider provider;
    private ManchesterOWLSyntaxOWLObjectRendererImpl manSyntaxRenderer;

    private Map<IRI, Collection<String>> equivalentClassExpressionsAsString = new HashMap<>();
    private Map<IRI, Collection<String>> superclassExpressionsAsString = new HashMap<>();
    private String preferredPrefix;
    private Map<OWLAnnotationProperty, List<String>> preferredLanguageMap = new HashMap<>();

    private DatabaseService databaseService;

    private OntologyLoadingConfiguration ontologyLoadingConfiguration;


    public AbstractOWLOntologyLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        this(config, null, null);
        OWLObjectRenderer o;
    }

    public AbstractOWLOntologyLoader(OntologyResourceConfig config, DatabaseService databaseService,
    		OntologyLoadingConfiguration ontologyLoadingConfiguration)
    		throws OntologyLoadingException {
    	
        this.databaseService = databaseService;
        this.ontologyLoadingConfiguration = ontologyLoadingConfiguration;
    	readConfiguration(config);
    	initializeOWLAPIWithoutReasoner();
    	initializeEnglishLanguagePreference();
    	initializeVocabularyToIgnore();
    }
    
    private void initializeEnglishLanguagePreference() throws OntologyLoadingException {
    	if (!OWLAPIInitialized) {
    		initializeOWLAPIWithoutReasoner();
    	} 
    	setPreferredLanguageMap(factory.getOWLAnnotationProperty(getLabelIRI()), 
        		Arrays.asList("en", ""));
    }

    private void readConfiguration(OntologyResourceConfig config) throws OntologyLoadingException {
        setOntologyIRI(IRI.create(config.getId()));
        setOntologyName(config.getNamespace());
        setOntologyTitle(config.getTitle());

        setOntologyDescription(config.getDescription());
        setOntologyHomePage(config.getHomepage());
        setOntologyMailingList(config.getMailingList());
        setOntologyTracker(config.getTracker());
        setOntologyLogo(config.getLogo());
        setOntologyCreators(config.getCreators());


        setPreferredPrefix(config.getPreferredPrefix());
        setSynonymIRIs(config.getSynonymProperties().stream()
                .map(IRI::create).
                        collect(Collectors.toSet()));

        setHiddenIRIs(
                config.getHiddenProperties()
                        .stream()
                        .map(IRI::create)
                        .collect(Collectors.toSet()));

        setLabelIRI(IRI.create(config.getLabelProperty()));


        setDefinitionIRIs(
                config.getDefinitionProperties().stream()
                        .map(IRI::create).
                        collect(Collectors.toSet()));
        setBaseIRI(config.getBaseUris());

        setHierarchicalIRIs(config.getHierarchicalProperties()
                .stream()
                .map(IRI::create)
                .collect(Collectors.toSet()));

        
        try {
			setOntologyResource(new UrlResource(config.getFileLocation()));
		} catch (MalformedURLException e) {
			throw new OntologyLoadingException("Can't load file from " + 
					config.getFileLocation(), e);
		}

        setPreferredRootTerms(config.getPreferredRootTerms()
                .stream()
                .map(IRI::create)
                .collect(Collectors.toSet()));
    }
    
    private void initializeOWLAPIWithoutReasoner() throws OntologyLoadingException {
        this.manager = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.THROW_EXCEPTION);
        this.manager.setOntologyLoaderConfiguration(config);

        if (getOntologyResource() != null) {
        	try {
			getLogger().info("Mapping ontology IRI from " + getOntologyIRI() + " to " +
					getOntologyResource().getURI());
            this.manager.addIRIMapper(new SimpleIRIMapper(getOntologyIRI(),
                    IRI.create(getOntologyResource().getURI())));
        	} catch (IOException e) {
        		throw new OntologyLoadingException("The ontology " + getOntologyIRI() + 
        				" could not be loaded", e);
        	}
        }
        if (getOntologyImportMappings() != null) {
            for (IRI from : getOntologyImportMappings().keySet()) {
                IRI to = getOntologyImportMappings().get(from);
                getLogger().info("Mapping imported ontology IRI from " + from + " to " + to);
                this.manager.addIRIMapper(new SimpleIRIMapper(from, to));
            }
        }
        this.factory = manager.getOWLDataFactory();

        OWLAPIInitialized = true;
    }
    
    private void initializeVocabularyToIgnore() throws OntologyLoadingException {
    	if (!OWLAPIInitialized) {
    		initializeOWLAPIWithoutReasoner();
    	} 
    	
        owlVocabulary.add(factory.getOWLThing().getIRI());
        owlVocabulary.add(factory.getOWLNothing().getIRI());
        owlVocabulary.add(factory.getOWLTopObjectProperty().getIRI());
        owlVocabulary.add(factory.getOWLBottomObjectProperty().getIRI());    	
    }

    @Override
    public String getShortForm(IRI ontologyTermIRI) {

        if (getOntologyTermAccessions().containsKey(ontologyTermIRI)) {
            getOntologyTermAccessions().get(ontologyTermIRI);
        }
        return  extractShortForm(ontologyTermIRI).get();
    }

    @Override
    public String getOboId(IRI ontologyTermIRI) {

        if (getOntologyTermOboId().containsKey(ontologyTermIRI)) {
            return  getOntologyTermOboId().get(ontologyTermIRI);
        }
        return  null;
    }

    
    
    private <G> G lazyGet(Callable<G> callable) {
    	try {
            initOrWait();
            return callable.call();
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(getClass().getSimpleName() + " failed to initialize", e);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to lazily instantiate collection for query", e);
        }
    }

    @Override 
    protected void doInitialization() throws Exception {        
        this.ontology = loadOntology();
    }

    @Override 
    protected void doTermination() throws Exception {
        // nothing to do
    }


    /**
     * Extracts and loads into memory all the class labels and corresponding IRIs.  This class makes the assumption that
     * one primary label per class exists. If any classes contain multiple rdfs:labels, these classes are ignored.
     * <p>
     * Once loaded, this method must set the IRI of the ontology, and should add class labels, class types (however you
     * chose to implement the concept of a "type") and synonyms, where they exist.
     * <p>
     * Implementations do not need to concern themselves with resolving imports or physical/logical mappings as this is
     * done in initialisation at the abstract level.  Subclasses can simply do <code>OWLOntology ontology =
     * getManager().loadOntology(IRI.create(getOntologyURI()));</code> as a basic implementation before populating the
     * various required caches
     */
    protected OWLOntology loadOntology() throws OWLOntologyCreationException {
        try {
            getLogger().debug("Loading ontology...");
            ResourceUsage.logUsage(getLogger(), "#### Monitoring ",
                    getOntologyIRI().getShortForm() + ":Before loading ontology", ":");
            this.ontology = getManager().loadOntology(getOntologyIRI());
            Optional<IRI> actualOntologyIRI = ontology.getOntologyID().getOntologyIRI();

            // set
            if (actualOntologyIRI.isPresent()) {
                if (!actualOntologyIRI.get().equals(IRI.create("http://purl.obolibrary.org/obo/TEMP"))) {
                    setOntologyIRI(actualOntologyIRI.get());
                }
            }

            if (ontology.getOntologyID().getVersionIRI().isPresent()) {
                ontologyVersionIRI = ontology.getOntologyID().getVersionIRI().get();

                getLogger().debug("Version IRI = " + ontologyVersionIRI);
                String oboVersion = parseOboVersion(ontologyVersionIRI);
                if (oboVersion != null) {
                    getLogger().debug("Set obo version");
                    setOntologyVersion(oboVersion);
                }

            }
            if (getOntologyName() == null) {
                Optional<String> name = extractShortForm(getOntologyIRI());
                if (!name.isPresent()) {
                    getLogger().warn("Can't shorten the name for " + getOntologyIRI().toString());
                    name = Optional.of(getOntologyIRI().toString());
                }
                setOntologyName(name.get());
            }
            getLogger().debug("Successfully loaded ontology " + getOntologyIRI());

            this.provider = new AnnotationValueShortFormProvider(
                    Collections.singletonList(factory.getOWLAnnotationProperty(getLabelIRI())),
                    getPreferredLanguageMap(),
                    manager);
            this.manSyntaxRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            manSyntaxRenderer.setShortFormProvider(provider);
            ResourceUsage.logUsage(getLogger(), "#### Monitoring ", getOntologyName() +
                    ":After loading ontology - before running reasoner", ":");

            // this call will initialise the reasoner
            OWLReasoner reasoner = getOWLReasoner(ontology);
            ResourceUsage.logUsage(getLogger(), "#### Monitoring ", getOntologyName() +
                    ":After running reasoner:" + reasoner.getReasonerName(), ":");

            // cache all URIs for classes, properties and individuals
            getLogger().debug("Computing indexes...");


            Collection<OWLEntity> allEntities = new HashSet<>();
            for (OWLOntology ontology1 : manager.getOntologies()) {
                allEntities.addAll(ontology1.getSignature());
            }
            ResourceUsage.logUsage(getLogger(), "#### Monitoring ", getOntologyName() +
                    ":After copying of entities", ":");

            indexTerms(allEntities);
            ResourceUsage.logUsage(getLogger(), "#### Monitoring ", getOntologyName() +
                    ":After index terms", ":");
            indexOntologyAnnotations(ontology.getAnnotations());
            ResourceUsage.logUsage(getLogger(), "#### Monitoring ", getOntologyName() +
                    ":After index annotations", ":");

            return ontology;
        }
        catch (Exception e) {
            setInitializationException(e);
            getLogger().error("Failed to parse " + getOntologyName() + " : " + e.getMessage(), e);
            throw e;
        }
        finally {
            setReady(true);
            discardReasoner(ontology);
        }
    }

    public static String parseOboVersion(IRI ontologyVersionIRI) {
        Pattern pattern = Pattern.compile(".*\\/(\\d{4}-\\d{2}-\\d{2})\\/.*");
        String DATE_FORMAT = "yyyy-MM-dd";
        String iriAsString = ontologyVersionIRI.toString();
        Matcher matcher = pattern.matcher(iriAsString);
        if (matcher.matches()) {
            String versionDate = matcher.group(1);

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            try {
                sdf.parse(versionDate);
                return versionDate;
            } catch (ParseException e) {
            }
        }
        return null;
    }

    private void indexOntologyAnnotations(Set<OWLAnnotation> owlAnnotations) {
    	getLogger().debug("Calling indexOntologyAnnotations");

        Set<String> creators = new HashSet<>();
        Map<String, Collection<String>> annotations = new HashMap<>();
        Collection<IRI> preferredRootTermAnnotations = new HashSet<>();
         for (OWLAnnotation annotation : owlAnnotations) {

            OWLAnnotationProperty annotationProperty = annotation.getProperty();
            OWLAnnotationValue annotationValue = annotation.getValue();
            Optional<String> theValue = getOWLAnnotationValueAsString(annotationValue);

            IRI annotationPropertyIri = annotationProperty.getIRI();

            getLogger().debug("annotationPropertyIri 1 = " + annotationPropertyIri);
            if (annotationPropertyIri.toString().equals(OntologyDefaults.DEFINITION)) {
                if (theValue.isPresent()) {
                    setOntologyDescription(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.DEFINITION);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.TITLE)) {
                if (theValue.isPresent()) {
                    setOntologyTitle(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.TITLE);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.CREATOR)) {
                if (theValue.isPresent()) {
                    creators.add(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.CREATOR);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.MAILINGLIST)) {
                if (annotationValue != null && annotationValue instanceof  IRI) {
                    setOntologyHomePage( ((IRI) annotationValue).toString());
                    internalMetadataProperties.add(OntologyDefaults.MAILINGLIST);
                } else if (theValue.isPresent()) {
                    setOntologyMailingList(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.MAILINGLIST);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.TRACKER)) {
                if (annotationValue != null && annotationValue instanceof  IRI) {
                    setOntologyTracker( ((IRI) annotationValue).toString());
                    internalMetadataProperties.add(OntologyDefaults.TRACKER);
                } else if (theValue.isPresent()) {
                    setOntologyTracker(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.TRACKER);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.LOGO)) {
                if (annotationValue != null && annotationValue instanceof  IRI) {
                    setOntologyLogo( ((IRI) annotationValue).toString());
                    internalMetadataProperties.add(OntologyDefaults.LOGO);
                } else if (theValue.isPresent()) {
                    setOntologyLogo(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.LOGO);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.HOMEPAGE)) {
                if (annotationValue != null && annotationValue instanceof  IRI) {
                    setOntologyHomePage( ((IRI) annotationValue).toString());
                    internalMetadataProperties.add(OntologyDefaults.HOMEPAGE);
                }
                else if (theValue.isPresent()) {
                    setOntologyHomePage(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.HOMEPAGE);
                }
            }
            else if (annotationPropertyIri.toString().equals(OntologyDefaults.VERSION)) {
                if (theValue.isPresent()) {
                    setOntologyVersion(theValue.get());
                    internalMetadataProperties.add(OntologyDefaults.VERSION);
                }
            } else if (annotationPropertyIri.toString().equals(
            		ontologyLoadingConfiguration.getPreferredRootTermAnnotationProperty())) {

                getLogger().debug("Check whether we can add preferredRootTerms for: "
            		+ annotationPropertyIri);

            	if (annotationValue instanceof IRI) {
                    preferredRootTermAnnotations.add((IRI)annotationValue);
            	}
            }
            else  {
                String propertyLabel = ontologyLabels.get(annotationPropertyIri);
                if (!annotations.containsKey(propertyLabel)) {
                    annotations.put(propertyLabel, new HashSet<>());
                }
                annotations.get(propertyLabel).add(theValue.get());
            }
        }
        overrideConfiguredPreferredRootTerms(preferredRootTermAnnotations);

        setOntologyCreators(creators);
        setOntologyAnnotations(annotations);
    }

    private void overrideConfiguredPreferredRootTerms(Collection<IRI> preferredRootTermAnnotations) {
        if (!preferredRootTermAnnotations.isEmpty()) {
            getLogger().debug("Preferred root term annotations are overriding configured annotations: " +
                    preferredRootTerms);
            preferredRootTerms = preferredRootTermAnnotations;
        }

        getLogger().debug("Preferred root terms = " + preferredRootTerms);
    }

    protected void indexTerms(Collection<OWLEntity> entities) {

        getLogger().debug("Starting to index " + entities.size() + " entities");

        for (OWLEntity entity: entities) {
            // get all the annotation properties
            evaluateAllAnnotationsValues(entity);

            // add the class accession for this entity
            Optional<String> shortForm = extractShortForm(entity.getIRI());
            if (shortForm.isPresent()) {
                addClassAccession(entity.getIRI(), shortForm.get());
                // if no label, create one form shortform
                if (ontologyLabels.get(entity.getIRI()) == null) {
                    addClassLabel(entity.getIRI(), shortForm.get() );
                }

                Optional<String> oboForm = getOBOid(shortForm.get());

                if (oboForm.isPresent()) {
                    addOboId(entity.getIRI(), oboForm.get());
                }

            }

            // find out if this term is local to the ontology based on the base URIs
            for (String base : getBaseIRI()) {
                if (entity.getIRI().toString().startsWith(base)) {
                    addLocalTerms(entity.getIRI());
                }
            }
            // index the different types of entity
            entity.accept(new OWLEntityVisitor() {
                @Override
                public void visit(OWLClass cls) {
                    try {
                        if (!cls.getIRI().toString().contains(Namespaces.OWL.toString())) {
                            classes.add(cls.getIRI());
                            indexSubclassRelations(cls);
                            indexEquivalentRelations(cls);
                        }

                    } catch (OWLOntologyCreationException e) {
                        getLogger().error("unable to index classes, unable to create reasoner", e);
                    }

                }

                @Override
                public void visit(OWLObjectProperty property) {
                    objectProperties.add(property.getIRI());
                    indexSubPropertyRelations(property);
                }

                @Override
                public void visit(OWLDataProperty property) {
                    dataProperties.add(property.getIRI());
                }

                @Override
                public void visit(OWLNamedIndividual individual) {

                    individuals.add(individual.getIRI());
                     try {
                        // add types as parents
                        indexIndividualTypes(individual);
                        indexPropertyRelations(individual);

                    } catch(OWLOntologyCreationException e) {
                        getLogger().error("unable to index individuals, unable to create reasoner", e);
                    }

                }

                @Override
                public void visit(OWLDatatype datatype) {
                    //ignore datatypes
                }

                @Override
                public void visit(OWLAnnotationProperty property) {
                    annotationProperties.add(property.getIRI());
                    indexSubAnnotationPropertyRelations(property);
                }

                @Override
                public void doDefault(Object object) {

                }
            });
        }
    }

    private void indexIndividualTypes(OWLNamedIndividual individual) throws OWLOntologyCreationException {
        Set<IRI> instanceTypes = new HashSet<IRI>();
        Map<IRI,Collection<IRI>> instanceClassRelations = new HashMap<IRI,Collection<IRI>>();
        Map<IRI,Collection<IRI>> instanceRelations = new HashMap<IRI,Collection<IRI>>();

        OWLReasoner reasoner = getOWLReasoner(ontology);
        reasoner.getTypes(individual,true).getFlattened().forEach(c->instanceTypes.add(c.getIRI()));

        EntitySearcher.getTypes(individual, ontology).forEach(expression -> {
            if (expression.isAnonymous())  {
                if (expression instanceof OWLObjectSomeValuesFrom) {
                    indexIndividualsToExistentialRestriction(instanceClassRelations, instanceRelations,
                            (OWLObjectSomeValuesFrom) expression);
                } else if(expression instanceof OWLObjectHasValue) {
                    indexIndividualsToExistentialRestriction(instanceClassRelations, instanceRelations,
                            (OWLObjectSomeValuesFrom) ((OWLObjectHasValue) expression).asSomeValuesFrom());
                }
            }
        });


        if (!instanceTypes.isEmpty()) {
            addDirectTypes(individual.getIRI(), instanceTypes);
        }
        if (!instanceClassRelations.isEmpty()) {
            addRelatedClassesToIndividual(individual.getIRI(), instanceClassRelations);
        }
        if (!instanceRelations.isEmpty()) {
            addRelatedIndividuals(individual.getIRI(), instanceRelations);
        }
    }

    private void indexIndividualsToExistentialRestriction(Map<IRI, Collection<IRI>> instanceClassRelations,
        Map<IRI, Collection<IRI>> instanceRelations, OWLObjectSomeValuesFrom objectSomeValuesFrom) {

      //we cover only the specific case of an existential restriction to a named class,
        //no inference, no syntactic equivalence (R some (A and A)
        if (!objectSomeValuesFrom.getProperty().isAnonymous()) {
            OWLClassExpression classExpressionFiller = objectSomeValuesFrom.getFiller();
            OWLObjectProperty objectProperty = objectSomeValuesFrom.getProperty().asOWLObjectProperty();
            if (classExpressionFiller instanceof OWLClass) {

                if (!instanceClassRelations.containsKey(objectProperty.getIRI())) {
                    instanceClassRelations.put(objectProperty.getIRI(), new HashSet<IRI>());
                }
                instanceClassRelations.get(objectProperty.getIRI()).add(
                    objectSomeValuesFrom.getFiller().asOWLClass().getIRI());

            } else if(classExpressionFiller instanceof OWLObjectOneOf) {
                indexRelationsFromExistentialRestrictionsToNominals(instanceRelations,
                    (OWLObjectOneOf) classExpressionFiller, objectProperty);
            }
        }
    }

    private void indexRelationsFromExistentialRestrictionsToNominals(
        Map<IRI, Collection<IRI>> instanceRelations, OWLObjectOneOf objectOneOf,
        OWLObjectProperty objectProperty) {
        if(objectOneOf.getIndividuals().size()==1) { // If there is more than one, we cannot assume a relationship.
            for (OWLIndividual individual : objectOneOf.getIndividuals()) {
                if (individual.isNamed()) {
                    if (!instanceRelations.containsKey(objectProperty.getIRI())) {
                        instanceRelations.put(objectProperty.getIRI(), new HashSet<>());
                    }
                    instanceRelations.get(objectProperty.getIRI()).add(individual.asOWLNamedIndividual().getIRI());
                }
            }
        }
    }

    private void indexSubAnnotationPropertyRelations(OWLAnnotationProperty property) {

        Set<IRI> superProperties = new HashSet<>();
        EntitySearcher.getSuperProperties(property, ontology).forEach(owlProperty -> {
            superProperties.add(owlProperty.asOWLAnnotationProperty().getIRI());
        });

        addDirectParents(property.getIRI(), superProperties);

        Set<IRI> subProperties = new HashSet<>();
        EntitySearcher.getSubProperties(property, ontology).forEach(owlProperty -> {
            subProperties.add(owlProperty.asOWLAnnotationProperty().getIRI());
        });
        addDirectChildren(property.getIRI(), subProperties);
    }

    private Set<IRI> findAllDirectAndIndirectSuperProperties(OWLObjectProperty objectProperty,
    		Set<IRI> indirectSuperProperties, Set<OWLOntology> ontologyImportClosure) {
        try {
            EntitySearcher.getSuperProperties(objectProperty, ontologyImportClosure.stream()).forEach(
                superObjectPropertyExpression -> {
                    if (!superObjectPropertyExpression.isAnonymous()) {
                        IRI superObjectPropertyIRI = superObjectPropertyExpression.asOWLObjectProperty().getIRI();
                        if (!indirectSuperProperties.contains(superObjectPropertyIRI)) {
                            indirectSuperProperties.add(superObjectPropertyIRI);
                            findAllDirectAndIndirectSuperProperties(superObjectPropertyExpression.asOWLObjectProperty(),
                                    indirectSuperProperties, ontologyImportClosure);
                        }
                    }
                });
        } catch (Throwable t) {
           getLogger().error("Problematic object property = " + objectProperty.getIRI(), t);
        }
    	return indirectSuperProperties;
    }

    private Set<IRI> findAllDirectAndIndirectSubProperties(OWLObjectProperty objectProperty,
    		Set<IRI> indirectSubProperties, Set<OWLOntology> ontologyImportClosure) {

        try {
            EntitySearcher.getSuperProperties(objectProperty, ontologyImportClosure.stream()).forEach(
                subObjectPropertyExpression -> {
                    IRI subObjectPropertyIRI = subObjectPropertyExpression.asOWLObjectProperty().getIRI();
                    if (!subObjectPropertyExpression.isAnonymous() &&
                            !indirectSubProperties.contains(subObjectPropertyIRI)) {

                        indirectSubProperties.add(subObjectPropertyIRI);
                        findAllDirectAndIndirectSubProperties(subObjectPropertyExpression.asOWLObjectProperty(),
                                indirectSubProperties, ontologyImportClosure);
                    }
                });
        } catch (Throwable t) {
            getLogger().error("Problematic object property = " + objectProperty.getIRI(), t);
        }
    	return indirectSubProperties;
    }

    private void indexSubPropertyRelations(OWLObjectProperty property) {
        getLogger().debug("indexSubPropertyRelations = " + property);

        Set<IRI> directSuperProperties = new HashSet<>();
        Set<IRI> indirectSuperProperties = new HashSet<>();
        Set<IRI> indirectSubProperties = new HashSet<>();

        Set<OWLOntology> ontologyImportClosure = ontology.getImportsClosure();
        try {
            EntitySearcher.getSuperProperties(property, ontologyImportClosure.stream()).filter(
                owlObjectProperty -> (!owlObjectProperty.isAnonymous() && owlObjectProperty.getInverseProperty() != null)).forEach(
                owlProperty -> {
                    directSuperProperties.add(owlProperty.asOWLObjectProperty().getIRI());
                });
        } catch (Throwable t) {
            getLogger().error("Problematic object property = " + property.getIRI(), t);
        }

        addDirectParents(property.getIRI(), directSuperProperties);
        addAllParents(property.getIRI(), findAllDirectAndIndirectSuperProperties(property,
        		indirectSuperProperties, ontologyImportClosure));
//        getLogger().debug("indexSubPropertyRelations: " + property + " directSuperProperties = " + directSuperProperties);
//        getLogger().debug("indexSubPropertyRelations: " + property + " indirectSuperProperties = " + indirectSuperProperties);

        Set<IRI> directSubProperties = new HashSet<>();
        try {
            EntitySearcher.getSubProperties(property, ontologyImportClosure.stream()).filter(
                owlObjectProperty -> !owlObjectProperty.isAnonymous()).forEach(
                owlProperty -> {
                    directSubProperties.add(owlProperty.asOWLObjectProperty().getIRI());
                });
        } catch (Throwable t) {
            getLogger().error("Problematic object property = " + property.getIRI());
        }

        addDirectChildren(property.getIRI(), directSubProperties);
        addAllParents(property.getIRI(), findAllDirectAndIndirectSubProperties(property,
        		indirectSubProperties, ontologyImportClosure));
        getLogger().debug("indexSubPropertyRelations: " + property + " directSubProperties = " + directSubProperties);
        getLogger().debug("indexSubPropertyRelations: " + property + " indirectSubProperties = " + indirectSubProperties);
    }


    protected  void indexSubclassRelations(OWLClass owlClass) throws OWLOntologyCreationException {
        getLogger().debug("indexSubclassRelations {}", owlClass);
        OWLReasoner reasoner = getOWLReasoner(ontology);

        Set<OWLClass> directSubClasses = reasoner.getSubClasses(owlClass, true).getFlattened();
        Set<OWLClass> allSubClasses = reasoner.getSubClasses(owlClass, false).getFlattened();
        Set<OWLClass> directSuperClasses = reasoner.getSuperClasses(owlClass, true).getFlattened();
        Set<OWLClass> allSuperClasses = reasoner.getSuperClasses(owlClass, false).getFlattened();

        // use reasoner to check if root
        if (directSuperClasses.contains(getFactory().getOWLThing())) {
            getLogger().debug("indexSubclassRelations add root for {}", owlClass);
            addRootsTerms(owlClass.getIRI());
        }

        if (directSuperClasses.contains(getFactory().getOWLClass(Namespaces.OBOINOWL.createIRI("ObsoleteClass")))) {
            addObsoleteTerms(owlClass.getIRI());
        }

        // get direct children

        Set<IRI> directChildTerms = removeExcludedIRI(
                directSubClasses.parallelStream()
                        .map(OWLNamedObject::getIRI)
                        .collect(Collectors.toSet()),
                owlVocabulary);
//        getLogger().debug("indexSubclassRelations directChildTerms for {} count  {}", owlClass, directChildTerms.size());
        if (directChildTerms.size() > 0)
          addDirectChildren(owlClass.getIRI(), directChildTerms) ;

        // get all children
        Set<IRI> allDescendantTerms = removeExcludedIRI(
                allSubClasses.parallelStream()
                        .map(OWLNamedObject::getIRI)
                        .collect(Collectors.toSet()),
                owlVocabulary);
        if (allDescendantTerms.size() >0)
          addAllChildren(owlClass.getIRI(), allDescendantTerms);

        // get parents
        Set<IRI> directParentTerms =
                removeExcludedIRI(
                        directSuperClasses.parallelStream()
                                .map(OWLNamedObject::getIRI)
                                .collect(Collectors.toSet()),
                        owlVocabulary);

        if (directParentTerms.size()>0)
          addDirectParents(owlClass.getIRI(), directParentTerms);

        // get all parents
        Set<IRI> allAncestorTerms =
                removeExcludedIRI(
                        allSuperClasses.parallelStream()
                                .map(OWLNamedObject::getIRI)
                                .collect(Collectors.toSet()),
                        owlVocabulary);

        if (allAncestorTerms.size()>0)
          addAllParents(owlClass.getIRI(), allAncestorTerms);

        // map of related parent terms for hierarchy views
        Map<IRI, Collection<IRI>> relatedParentTerms = new HashMap<>();

        // find direct related terms
        Map<IRI, Collection<IRI>> relatedTerms = new HashMap<>();
        Map<IRI,Collection<IRI>> relatedIndividualsToClasses = new HashMap<>();

        Set<String> relatedDescriptions = new HashSet<>();

        EntitySearcher.getSuperClasses(owlClass, getManager().ontologies()).forEach(expression -> {

                // only want existential with named class as filler
                if (expression.isAnonymous()) {

                    if (expression instanceof OWLObjectSomeValuesFrom) {

                        OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) expression;

                        if (!someValuesFrom.getFiller().isAnonymous() && ! someValuesFrom.getProperty().isAnonymous()) {
                            IRI propertyIRI = someValuesFrom.getProperty().asOWLObjectProperty().getIRI();
                            IRI relatedTerm = someValuesFrom.getFiller().asOWLClass().getIRI();

                            // skip terms that are related to themselves as this can cause nasty cycles

                            if (!relatedTerms.containsKey(propertyIRI)) {
                                relatedTerms.put(propertyIRI, new HashSet<>());
                            }
                            relatedTerms.get(propertyIRI).add(relatedTerm);

                            // check if hierarchical
                            if (hierarchicalRels.contains(propertyIRI) || isPartOf(propertyIRI) ) {
                                if (owlClass.getIRI().equals(relatedTerm)) {
                                    getLogger().warn("Ignoring Iri that is related to itself: " + owlClass.getIRI());
                                } else  {
                                    if (!relatedParentTerms.containsKey(propertyIRI)) {
                                        relatedParentTerms.put(propertyIRI, new HashSet<>());
                                    }
                                    relatedParentTerms.get(propertyIRI).add(relatedTerm);
                                    addRelatedChildTerm(relatedTerm, owlClass.getIRI());
                                }


                            }

                        } else if (someValuesFrom.getFiller().isAnonymous() && !someValuesFrom.getProperty().isAnonymous()) {
                            indexTermToIndividualRelations(someValuesFrom,relatedIndividualsToClasses);
                        }

                    } else if (expression instanceof OWLObjectHasValue) {
                        OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom)((OWLObjectHasValue) expression).asSomeValuesFrom();
                        indexTermToIndividualRelations(someValuesFrom,relatedIndividualsToClasses);
                    }


                    // store stringified form of class description
                    relatedDescriptions.add(renderHtml(expression));
                }
        });

        if (!relatedTerms.isEmpty()) {
            addRelatedTerms(owlClass.getIRI(), relatedTerms );
        }

        if (!relatedIndividualsToClasses.isEmpty()) {
            addRelatedIndividualsToClasses(owlClass.getIRI(), relatedIndividualsToClasses);
        }

        addRelatedParentTerms(owlClass.getIRI(), relatedParentTerms);

        if (!relatedDescriptions.isEmpty()) {

            addSuperClassDescriptions(owlClass.getIRI(), relatedDescriptions);

        }
        // todo find transitive closure of related terms
    }

    private void indexTermToIndividualRelations(OWLObjectSomeValuesFrom someValuesFrom,
        Map<IRI, Collection<IRI>> relatedIndividualsToClasses) {

      OWLClassExpression classExpression = someValuesFrom.getFiller();
        if(someValuesFrom.getProperty().isAnonymous()) {
            //Must be anonymous property
            return;
        }
        OWLObjectProperty objectProperty = someValuesFrom.getProperty().asOWLObjectProperty();
        if(classExpression instanceof OWLObjectOneOf) {
            indexRelationsFromExistentialRestrictionsToNominals(relatedIndividualsToClasses,
                (OWLObjectOneOf) classExpression, objectProperty);
        }
    }

    /**
     * Bit of a hack to try and detect non standard 'part of' predicates
     * @param propertyIri
     * @return
     */
    private boolean isPartOf(IRI propertyIri) {
        Optional<String> shortForm = extractShortForm(propertyIri);
        if (shortForm.isPresent()) {
            return shortForm.get().toLowerCase().replaceAll("_", "").equals("partof");
        }
        return false;
    }

    private String renderHtml (OWLObject owlObject) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        OWLHTMLVisitor owlhtmlVisitor = new OWLHTMLVisitor(provider, printWriter);
        owlhtmlVisitor.setActiveOntology(ontology);
        owlObject.accept(owlhtmlVisitor);
        return stringWriter.toString();
    }

    private void addRelatedChildTerm(IRI parent, IRI child) {
        if (!relatedChildTerms.containsKey(parent)) {
            relatedChildTerms.put(parent, new HashSet<>());
        }
        relatedChildTerms.get(parent).add(child);
    }

    private void indexEquivalentRelations(OWLClass owlClass) throws OWLOntologyCreationException {
        OWLReasoner reasoner = getOWLReasoner(ontology);

        // get direct children
        addEquivalentTerms(owlClass.getIRI(),
                reasoner.getEquivalentClasses(owlClass).getEntities().stream()
                        .map(OWLNamedObject::getIRI)
                        .collect(Collectors.toSet()));

        Set<String> relatedDescriptions = new HashSet<>();

        EntitySearcher.getEquivalentClasses(owlClass, getManager().ontologies())
                .forEach(expression -> {
                    if (expression.isAnonymous()) {
                        relatedDescriptions.add(renderHtml(expression));
                    }
                });

        if (!relatedDescriptions.isEmpty()) {
            addEquivalentClassDescriptions(owlClass.getIRI(), relatedDescriptions);
        }

    }

    private void indexPropertyRelations(OWLNamedIndividual individual) throws OWLOntologyCreationException {

        Map<IRI,Collection<IRI>> instanceInstanceRelations = new HashMap<IRI,Collection<IRI>>();

        //The following works, will most likely be way too computationally expensive though
        //extractInferredRelationsFromIndividualObjectPropertyAssertions(individual, instanceInstanceRelations);
        extractAssertedRelationsFromIndividualObjectPropertyAssertions(individual, instanceInstanceRelations);
        if (!instanceInstanceRelations.isEmpty()) {
            addRelatedIndividuals(individual.getIRI(), instanceInstanceRelations);
        }

    }

    private void extractAssertedRelationsFromIndividualObjectPropertyAssertions(OWLNamedIndividual individual, Map<IRI, Collection<IRI>> instanceInstanceRelations) {
        Multimap<OWLObjectPropertyExpression, OWLIndividual> assertedRelations =
                EntitySearcher.getObjectPropertyValues(individual, ontology);

        for (OWLObjectPropertyExpression rel : assertedRelations.keySet()) {
            if(!rel.isAnonymous()) {
                IRI reliri = rel.asOWLObjectProperty().getIRI();
                for(OWLIndividual i:assertedRelations.get(rel)) {
                    //no inference, no negation or anonymous individuals
                    if(!i.isAnonymous()) {

                        if(!instanceInstanceRelations.containsKey(reliri)) {
                            instanceInstanceRelations.put(reliri,new HashSet<IRI>());
                        }
                        instanceInstanceRelations.get(reliri).add(i.asOWLNamedIndividual().getIRI());
                    }
                }
            }
        }
    }

    private void extractInferredRelationsFromIndividualObjectPropertyAssertions(OWLNamedIndividual individual, Map<IRI, Collection<IRI>> instanceInstanceRelations) throws OWLOntologyCreationException {
        // <i1,i2>:R (ObjectPropertyAssertions
        OWLReasoner reasoner = getOWLReasoner(ontology);


        Set<OWLObjectPropertyExpression> relations = new HashSet<>();

        //Reduced set if it gets too slow:
        // We only consider inferred relations for this individual if there exists at least one asserted relation, as well
        /*for(OWLObjectPropertyExpression rel:individual.getObjectPropertyValues(ontology).keySet()) {
           relations.addAll( reasoner.getSuperObjectProperties(rel,false).getFlattened());
           relations.add(rel);
        } */
        relations.addAll(ontology.getObjectPropertiesInSignature(false));
        relations.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLBottomObjectProperty());
        relations.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLTopObjectProperty());

        for (OWLObjectPropertyExpression rel : relations) {
            if(!rel.isAnonymous()) {
                IRI reliri = rel.asOWLObjectProperty().getIRI();
                for (OWLNamedIndividual i : reasoner.getObjectPropertyValues(individual,rel.asOWLObjectProperty()).getFlattened()) {
                    if (!instanceInstanceRelations.containsKey(reliri)) {
                        instanceInstanceRelations.put(reliri, new HashSet<IRI>());
                    }
                    instanceInstanceRelations.get(reliri).add(i.asOWLNamedIndividual().getIRI());
                }
            }
        }
    }

    /**
     * Tries to extract the "final part" of an IRI as the short form.
     *
     * @param entityIRI
     * @return
     */
    protected Optional<String> extractShortForm(IRI entityIRI) {
        getLogger().trace("Attempting to extract fragment name of IRI '" + entityIRI + "'");

        // special case for URN schemes: https://www.w3.org/Addressing/URL/URI_URN.html
        if(entityIRI.toString().startsWith("urn:")) {
            return Optional.of(entityIRI.toString().substring(4));
        }

        // oput in try block to catch any URL exceptins
        try {
            if (!StringUtils.isEmpty(entityIRI.toURI().getFragment())) {
                // a uri with a non-null fragment, so use this...
                getLogger().trace("Extracting fragment name using ONTOLOGY_URI fragment (" + entityIRI.toURI().getFragment() + ")");
                return Optional.of(entityIRI.toURI().getFragment());
            }
        } catch (Exception e) {
            // carry on and try some other strategies
        }

        try {
	        if (entityIRI.getRemainder().isPresent()) {
	            return Optional.of(entityIRI.getRemainder().get());
	        }
	        else if (entityIRI.toURI().getPath() != null) {
	            // no fragment, but there is a path so try and extract the final part...
	            if (entityIRI.toURI().getPath().contains("/")) {
	                getLogger().trace("Extracting fragment name using final part of the path of the ONTOLOGY_URI");
	                return Optional.of(entityIRI.toURI().getPath().substring(entityIRI.toURI().getPath().lastIndexOf('/') + 1));
	            }
	            else {
	                // no final path part, so just return whole path
	                getLogger().trace("Extracting fragment name using the path of the ONTOLOGY_URI");
	                return Optional.of(entityIRI.toURI().getPath());
	            }
	        }
	        else {
	            // no fragment, path is null, we've run out of rules so don't shorten
	            getLogger().trace("No rules to shorten this ONTOLOGY_URI could be found (" + entityIRI + ")");
	            return Optional.empty();
	        }
        } catch (IllegalArgumentException iae) {
            getLogger().debug("Workaround for IllegalArgumentException for :" + entityIRI);
        	return Optional.empty();
        }
    }


    private Optional<String> getOBOid(String fragment) {
        Matcher matcher = oboIdFragmentPattern.matcher(fragment);
        if (matcher.find()) {
            String newId = matcher.group(2) + ":" + matcher.group(3);
            return Optional.of(newId);
        }
        else if (fragment.split(":").length == 2 && fragment.split(":")[0].toLowerCase().equals(getOntologyName().toLowerCase())) {
            // if the fragment already contains a :
            return Optional.of(fragment);
        }
        return Optional.empty();
    }


    protected Optional<String> evaluateLabelAnnotationValue(OWLEntity entity, OWLAnnotationValue value) {
        // get label annotations
        Optional<String> label = getOWLAnnotationValueAsString(value);
        if (!label.isPresent()) {
            // try and get the ONTOLOGY_URI fragment and use that as label
            Optional<String> fragment = extractShortForm(entity.getIRI());
            if (fragment.isPresent()) {
                return Optional.of(fragment.get());
            }
            else {
                getLogger().warn("OWLEntity " + entity + " contains no label. " +
                        "No labels for this class will be loaded.");
                return  Optional.of(entity.toStringID());
            }
        }
        return label;
    }

    private boolean isEnglishLabel(OWLAnnotationValue value) {
        return value instanceof OWLLiteral && ((OWLLiteral) value).getLang().equalsIgnoreCase("en");
    }

    protected void evaluateAllAnnotationsValues(OWLEntity owlEntity) {

        IRI owlEntityIRI = owlEntity.getIRI();
        Set<String> synonyms = new HashSet<>();
        Set<String> definitions = new HashSet<>();
        Set<String> slims = new HashSet<>();

        Collection<OBODefinitionCitation> definitionCitations = new HashSet<>();
        Collection<OBOSynonym> oboSynonyms = new HashSet<>();
        Collection<OBOXref> oboEntityXrefs = new HashSet<>();

        // loop through other annotations in the imports closure
        for (OWLOntology anOntology : getManager().ontologies().collect(Collectors.toSet())){
                EntitySearcher.getAnnotationAssertionAxioms(owlEntity, anOntology).forEach(annotationAssertionAxiom -> {
                        OWLAnnotationProperty annotationProperty = annotationAssertionAxiom.getProperty();
                        IRI annotationPropertyIRI = annotationProperty.getIRI();

                        if (getLabelIRI().equals(annotationPropertyIRI)) {
                            if (!ontologyLabels.containsKey(owlEntityIRI)) {
                                addClassLabel(owlEntityIRI, evaluateLabelAnnotationValue(
                                        owlEntity, annotationAssertionAxiom.getValue()).get());
                            } else {
                                getLogger().warn("Found multiple labels for class" + owlEntityIRI.toString());
                                // if english, overide previous label
                                if (isEnglishLabel(annotationAssertionAxiom.getValue())) {
                                    addClassLabel(owlEntityIRI, evaluateLabelAnnotationValue(
                                            owlEntity, annotationAssertionAxiom.getValue()).get());
                                }
                            }
                        }
                        else if (getSynonymIRIs().contains(annotationPropertyIRI)) {
                            synonyms.add(getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).get());
                        }
                        else if (getDefinitionIRIs().contains(annotationPropertyIRI)) {
                            definitions.add(getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).get());
                        }
                        else if (annotationPropertyIRI.equals(Namespaces.OBOINOWL.createIRI("inSubset")) && annotationAssertionAxiom.getValue() instanceof IRI) {
                            if (extractShortForm( (IRI) annotationAssertionAxiom.getValue()).isPresent()) {
                                slims.add(extractShortForm( (IRI) annotationAssertionAxiom.getValue()).get());
                            }
                        }
                        else if (annotationPropertyIRI.equals(Namespaces.OWL.createIRI("deprecated"))) {
                            addObsoleteTerms(owlEntityIRI);
                        }
                        else {
                            if (getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).isPresent()) {
                                // initialise maps if first time
                                if (!termAnnotations.containsKey(owlEntityIRI)) {
                                    HashMap<IRI, Collection<String>> newMap = new HashMap<>();
                                    newMap.put(annotationPropertyIRI, new HashSet<>());
                                    termAnnotations.put(owlEntityIRI, newMap);
                                }

                                if (!termAnnotations.get(owlEntityIRI).containsKey(annotationPropertyIRI)) {
                                    termAnnotations.get(owlEntityIRI).put(annotationPropertyIRI, new HashSet<>());
                                }

                                if (annotationAssertionAxiom.getValue() instanceof IRI) {
                                    termAnnotations.get(owlEntityIRI).get(annotationPropertyIRI).add(annotationAssertionAxiom.getValue().toString());
                                }
                                else {
                                    termAnnotations.get(owlEntityIRI).get(annotationPropertyIRI).add(getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).get());
                                }
                            }
                        }

                        // pull out term replaced by
                        if (annotationPropertyIRI.equals(Namespaces.OBO.createIRI("IAO_0100001"))) {
                            addTermReplacedBy(owlEntityIRI, getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).get());
                        }

                        // collect any obo definition xrefs
                        if (annotationAssertionAxiom.getProperty().getIRI().toString().equals(OboDefaults.DEFINITION)) {
                            if (!annotationAssertionAxiom.getAnnotations().isEmpty()) {

                                OBODefinitionCitation definitionCitation = new OBODefinitionCitation();
                                Collection<OBOXref> oboXrefs = new HashSet<>();
                                for (OWLAnnotation defAnnotation : annotationAssertionAxiom.getAnnotations()) {
                                    oboXrefs.add(extractOBOXrefs(defAnnotation));
                                }
                                definitionCitation.setDefinition(getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).get());
                                definitionCitation.setOboXrefs(oboXrefs);
                                definitionCitations.add(definitionCitation);
                            }
                        }

                        // collect any obo synonym xrefs
                        if (
                                annotationAssertionAxiom.getProperty().getIRI().toString().equals(OboDefaults.EXACT_SYNONYM)
                                        || annotationAssertionAxiom.getProperty().getIRI().toString().equals(OboDefaults.RELATED_SYNONYM)
                                        || annotationAssertionAxiom.getProperty().getIRI().toString().equals(OboDefaults.NARROW_SYNONYM)
                                        || annotationAssertionAxiom.getProperty().getIRI().toString().equals(OboDefaults.BROAD_SYNONYM)

                        ) {
                            if (!annotationAssertionAxiom.getAnnotations().isEmpty()) {

                                OBOSynonym synonymCitation = new OBOSynonym();

                                synonymCitation.setName(getOWLAnnotationValueAsString(annotationAssertionAxiom.getValue()).get());
                                synonymCitation.setScope(annotationAssertionAxiom.getProperty().getIRI().getShortForm());
                                Collection<OBOXref> oboXrefs = new HashSet<>();
                                for (OWLAnnotation annotationAxiomAnnotation : annotationAssertionAxiom.getAnnotations()) {
                                    if (annotationAxiomAnnotation.getProperty().getIRI().toString().equals(OboDefaults.SYNONYM_TYPE)) {
                                        OWLAnnotationValue value = annotationAxiomAnnotation.getValue();
                                        if (value instanceof IRI) {
                                            OWLAnnotationProperty owlAnnotationProperty = factory.getOWLAnnotationProperty((IRI) value);
                                            EntitySearcher.getAnnotations(owlAnnotationProperty, ontology).forEach(valueAnnotation -> {
                                                if (valueAnnotation.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {
                                                    String type = getOWLAnnotationValueAsString(valueAnnotation.getValue()).get();
                                                    synonymCitation.setType(type);
                                                }
                                            });
                                        }
                                    }
                                    if (annotationAxiomAnnotation.getProperty().getIRI().toString().equals(OboDefaults.DBXREF)) {
                                        oboXrefs.add(extractOBOXrefs(annotationAxiomAnnotation));
                                    }
                                }
                                synonymCitation.setXrefs(oboXrefs);
                                oboSynonyms.add(synonymCitation);
                            }
                        }

                        // collect any obo  xrefs
                        if (annotationAssertionAxiom.getProperty().getIRI().toString().equals(OboDefaults.DBXREF)) {
                            OBOXref oboXrefs = extractOBOXrefs(annotationAssertionAxiom.getAnnotation());
                            if (!annotationAssertionAxiom.getAnnotations().isEmpty()) {
                                for (OWLAnnotation axiomAnnotation : annotationAssertionAxiom.getAnnotations()) {
                                    String description = getOWLAnnotationValueAsString(axiomAnnotation.getValue()).get();
                                    oboXrefs.setDescription(description);
                                }
                            }
                            oboEntityXrefs.add(oboXrefs);
                        }
                });
        }

        if (definitionCitations.size() > 0) {
            addOboDefinitionCitation(owlEntityIRI, definitionCitations);
        }

        if (oboSynonyms.size() >0 ) {
            addOboSynonym(owlEntityIRI, oboSynonyms);
        }

        if (oboEntityXrefs.size() >0 ) {
            addOboXref(owlEntityIRI, oboEntityXrefs);
        }

        if (synonyms.size() > 0) {
            addSynonyms(owlEntityIRI, synonyms);
        }
        if (definitions.size() >0) {
            addDefinitions(owlEntityIRI, definitions);
        }
        if (slims.size() >0) {
            addSlims(owlEntityIRI, slims);
        }
    }

    private OBOXref extractOBOXrefs (OWLAnnotation annotation) {

        OBOXref xref = new OBOXref();
        String xrefValue;
        if (annotation.getValue() instanceof IRI) {
            xrefValue = annotation.getValue().toString();
        }
        else {
            xrefValue=getOWLAnnotationValueAsString(annotation.getValue()).get();
        }

        String database = null;
        String id = xrefValue;

        if (xrefValue.startsWith("http")) {
            try {
                URL descUrl = new URL(xrefValue);
                xref.setUrl(descUrl.toString());
            } catch (MalformedURLException e) {
                // not a URL so ignore
            }
        } else {
            if (xrefValue.contains(":") & xrefValue.split(":").length <= 2) {
                database = xrefValue.substring(0, xrefValue.indexOf(":"));
                id = xrefValue.substring(xrefValue.indexOf(":") + 1, xrefValue.length() );

                // check for Url
                if (databaseService != null) {
                    if (databaseService.findByName(database).isPresent()) {
                        try {
                            URL url = databaseService.findByName(database).get().getUrlForId(id);
                            xref.setUrl(url.toString());
                        } catch (MalformedURLException e) {
                            // not a URL so ignore
                        }
                    }
                }
            }
        }


        xref.setDatabase(database);
        xref.setId(id);
        if (!annotation.getAnnotations().isEmpty()) {
            for (OWLAnnotation axiomAnnotation : annotation.getAnnotations()) {
                String description = getOWLAnnotationValueAsString(axiomAnnotation.getValue()).get();
                xref.setDescription(description);
            }
        }

        return xref;
    }

    private Optional<String> getOWLAnnotationValueAsString (OWLAnnotationValue value) {

        if (value instanceof IRI) {
            Optional<String> shortForm= extractShortForm((IRI) value);
            if ( shortForm.isPresent() && !shortForm.get().isEmpty()) {
                return Optional.of(shortForm.get());
            } else {
                return Optional.of( ((IRI) value).toString() );
            }
        }
        else if (value instanceof OWLLiteral) {
            return Optional.of(((OWLLiteral) value).getLiteral());
        }
        return Optional.of("");

    }

    protected abstract OWLReasoner getOWLReasoner(OWLOntology ontology) throws OWLOntologyCreationException;
    protected abstract void discardReasoner(OWLOntology ontology) throws OWLOntologyCreationException;

    // bunch of getters and setters

    protected void addDirectParents(IRI termIRI, Set<IRI> parents) {
        this.directParentTerms.put(termIRI, parents);
    }
    protected void addDirectTypes(IRI termIRI, Set<IRI> parents) {
        this.directTypes.put(termIRI, parents);
    }
    protected void addAllParents(IRI termIRI, Set<IRI> allParents) {
        this.allParentTerms.put(termIRI, allParents);
    }
    protected void addDirectChildren(IRI termIRI, Set<IRI> children) {
        this.directChildTerms.put(termIRI, children);
    }
    protected void addAllChildren(IRI termIRI, Set<IRI> allChildren) {
        this.allChildTerms.put(termIRI, allChildren);
    }
    protected void addEquivalentTerms(IRI termIRI, Set<IRI> equivalent) {
        this.equivalentTerms.put(termIRI, equivalent);
    }
    protected void addLocalTerms(IRI termIRI) {
        this.localTerms.add(termIRI);
    }

    protected void addRootsTerms(IRI termIRI) {
        this.rootTerms.add(termIRI);
    }
    protected void addObsoleteTerms(IRI termIRI) {
        this.obsoleteTerms.add(termIRI);
    }
    protected void addRelatedTerms(IRI termIRI, Map<IRI, Collection<IRI>> relatedTerms) {
        this.relatedTerms.put(termIRI, relatedTerms);
    }
    protected void addRelatedParentTerms(IRI termIRI, Map<IRI, Collection<IRI>> relatedTerms) {
        this.relatedParentTerms.put(termIRI, relatedTerms);
    }
    protected void addAllRelatedTerms(IRI termIRI, Map<IRI, Collection<IRI>> relatedTerms) {
        this.allRelatedTerms.put(termIRI, relatedTerms);
    }
    protected void addRelatedIndividuals(IRI termIRI, Map<IRI, Collection<IRI>> relatedTerms) {
        this.allRelatedIndividuals.put(termIRI, relatedTerms);
    }
    protected void addRelatedIndividualsToClasses(IRI termIRI, Map<IRI, Collection<IRI>> relatedTerms) {
        this.allRelatedIndividualsToClasses.put(termIRI, relatedTerms);
    }
    protected void addRelatedClassesToIndividual(IRI termIRI, Map<IRI, Collection<IRI>> relatedTerms) {
        this.allRelatedClassesToIndividuals.put(termIRI, relatedTerms);
    }


    protected void addOboDefinitionCitation (IRI termIri, Collection<OBODefinitionCitation> definitionCitations) {
        this.oboDefinitionCitations.put(termIri, definitionCitations);
    }
    protected void addOboXref (IRI termIri, Collection<OBOXref> xrefs) {
        this.oboXrefs.put(termIri, xrefs);
    }
    protected void addOboSynonym (IRI termIri, Collection<OBOSynonym> synonyms) {
        this.oboSynonyms.put(termIri, synonyms);
    }

    protected void addSuperClassDescriptions(IRI termIRI, Set<String> relatedSuperDescriptions) {
        this.superclassExpressionsAsString.put(termIRI, relatedSuperDescriptions);
    }
    protected void addEquivalentClassDescriptions(IRI termIRI, Set<String> relatedEquivalentDescriptions) {
        this.equivalentClassExpressionsAsString.put(termIRI, relatedEquivalentDescriptions);
    }
    public Collection<String> getBaseIRI() {
        return baseIRIs;
    }

    @Override
    public Map<IRI, Collection<IRI>> getRelatedTerms(IRI entityIRI) {
        if (relatedTerms.containsKey(entityIRI)) {
            return relatedTerms.get(entityIRI);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<IRI, Collection<IRI>> getRelatedClassesToIndividual(IRI entityIRI) {
        if (allRelatedClassesToIndividuals.containsKey(entityIRI)) {
            return allRelatedClassesToIndividuals.get(entityIRI);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<IRI, Collection<IRI>> getRelatedIndividualsToClass(IRI entityIRI) {
        if (allRelatedIndividualsToClasses.containsKey(entityIRI)) {
            return allRelatedIndividualsToClasses.get(entityIRI);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<IRI, Collection<IRI>> getRelatedIndividuals(IRI entityIRI) {
        if (allRelatedIndividuals.containsKey(entityIRI)) {
            return allRelatedIndividuals.get(entityIRI);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<IRI, Collection<IRI>> getRelatedParentTerms(IRI entityIRI) {
        if (relatedParentTerms.containsKey(entityIRI)) {
            return relatedParentTerms.get(entityIRI);
        }
        return Collections.emptyMap();
    }


    public Collection<IRI> getAllRelatedParentTerms(IRI entityIRI) {

        Collection<IRI> allRelatedParents = new HashSet();

        // get any related parents then go up tree
        if (relatedParentTerms.containsKey(entityIRI)) {

            // if term is related to one of its children then ignore as this creates a cycle in the tree
            for (IRI value : relatedParentTerms.get(entityIRI).values().stream().flatMap(Collection::stream).collect(Collectors.toSet())) {
                if (allChildTerms.containsKey(entityIRI)) {
                    if (allChildTerms.get(entityIRI).contains(value)) {
                        getLogger().warn("Cycle detected where is " + entityIRI + " is related to one of its descendants");
                        continue;
                    }
                }
                allRelatedParents.addAll(fillAllRelatedParents(value));
            }
        }
        // get any direct parents, then go up tree
        if (directParentTerms.containsKey(entityIRI)) {
            for (IRI value : directParentTerms.get(entityIRI)) {
                allRelatedParents.addAll(fillAllRelatedParents(value));
            }
        }
        return allRelatedParents;
    }

    private Collection<IRI> fillAllRelatedParents(IRI entityIRI) {

        Set<IRI> newValues = new HashSet<>();

        newValues.add(entityIRI);
        // get any related parents then go up tree
        if (relatedParentTerms.containsKey(entityIRI)) {
            for (IRI value : relatedParentTerms.get(entityIRI).values().stream().flatMap(Collection::stream).collect(Collectors.toSet())) {
                if (allChildTerms.containsKey(entityIRI)) {
                    if (allChildTerms.get(entityIRI).contains(value)) {
                        getLogger().warn("Cycle detected where " + entityIRI + " is related to one of its decendants");
                        continue;
                    }
                }
                if (relatedParentTerms.containsKey(value)) {
                    Set<IRI> parentsOfValue = relatedParentTerms.get(value).values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
                    if (parentsOfValue.contains(entityIRI)) {
                        getLogger().warn("Cycle detected  on relationship " + entityIRI + " is related to one of its decendants");
                        continue;
                    }
                }

                newValues.addAll(fillAllRelatedParents(value));
            }
        }
        // get any direct parents, then go up tree

        if (directParentTerms.containsKey(entityIRI)) {
            for (IRI value : directParentTerms.get(entityIRI)) {
                // make sure no cycles
                if (allParentTerms.containsKey(value)) {
                    if (allParentTerms.get(value).contains(entityIRI)) {
                        getLogger().warn("Cycle detected where " + entityIRI + " is a subclass of itself");
                        continue;
                    }
                }

                newValues.addAll(fillAllRelatedParents(value));
            }
        }

        return newValues;

    }


    public Collection<IRI> getRelatedChildTerms(IRI entityIRI) {
        if (relatedChildTerms.containsKey(entityIRI)) {
            return relatedChildTerms.get(entityIRI);
        }
        return Collections.emptySet();
    }

    @Override
    public Map<IRI, Collection<IRI>> getDirectParentTerms() {
        return directParentTerms;
    }

    @Override
    public Map<IRI, Collection<IRI>> getDirectTypes() {
        return directTypes;
    }

    @Override
    public Collection<IRI> getDirectParentTerms(IRI iri) {
        Collection<IRI> parentTerms = directParentTerms.get(iri);
        if(parentTerms == null){
            return new ArrayList<IRI>();
        }
        return parentTerms;
    }

    @Override
    public Map<IRI, Collection<IRI>> getAllParentTerms() {
        return allParentTerms;
    }

    @Override
    public Map<IRI, Collection<IRI>> getDirectChildTerms() {
        return directChildTerms;
    }

    @Override
    public Collection<IRI> getDirectChildTerms(IRI iri) {
        Collection<IRI> childTerms = directChildTerms.get(iri);
        if(childTerms == null){
            return new ArrayList<IRI>();
        }
        return directChildTerms.get(iri);
    }

    @Override
    public Map<IRI, Collection<IRI>> getAllChildTerms() {
        return allChildTerms;
    }

    @Override
    public Map<IRI, Collection<String>> getLogicalSuperClassDescriptions() {
        return this.superclassExpressionsAsString;
    }

    @Override
    public Map<IRI, Collection<String>> getLogicalEquivalentClassDescriptions() {
        return this.equivalentClassExpressionsAsString;
    }

    @Override
    public Map<IRI, Collection<IRI>> getEquivalentTerms() {
        return equivalentTerms;
    }

    @Override
    public boolean isObsoleteTerm(IRI entityIRI) {
        return this.obsoleteTerms.contains(entityIRI);
    }

    @Override
    public boolean isLocalTerm(IRI entityIRI) {
        return this.localTerms.contains(entityIRI);
    }
    public void setBaseIRI(Collection<String> baseIRIs) {
        this.baseIRIs = baseIRIs;
    }

    @Override
    public Collection<IRI> getAllClasses() {
        return lazyGet(() -> classes);
    }

    @Override
    public Collection<IRI> getAllObjectPropertyIRIs() {
        return  lazyGet(() -> objectProperties);
    }

    @Override
    public Collection<IRI> getAllDataPropertyIRIs() {
        return dataProperties;
    }

    @Override
    public Collection<IRI> getAllIndividualIRIs() {
        return individuals;
    }

    @Override
    public Collection<IRI> getAllAnnotationPropertyIRIs() {
        return annotationProperties;
    }

    public void setOntologyResource(Resource ontologyResource) {
        this.ontologyResource = ontologyResource;
    }

    public void setOntologyImportMappings(Map<IRI, IRI> ontologyImportMappings) {
        this.ontologyImportMappings = ontologyImportMappings;
    }

    public void setSynonymIRIs(Collection<IRI> synonymIRI) {
        this.synonymIRIs = synonymIRI;
    }

    public void setLabelIRI(IRI labelIRI) {
        this.labelIRI = labelIRI;
    }

    public IRI getLabelIRI() {
        return labelIRI;
    }

    public Collection<IRI> getDefinitionIRIs() {
        return definitionIRIs;
    }

    public void setExclusionClassIRI(IRI exclusionClassIRI) {
        this.exclusionClassIRI = exclusionClassIRI;
    }

    public void setExclusionAnnotationIRI(IRI exclusionAnnotationIRI) {
        this.exclusionAnnotationIRI = exclusionAnnotationIRI;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }


    public void setOntologyDescription(String ontologyName) {
        this.ontologyDescription = ontologyName;
    }
    /**
     * Returns the short name of the ontology
     *
     * @return the short name of the ontology
     */
    public String getOntologyName() {
        return ontologyName;
    }

    /**
     * Returns the location from which the ontology (specified by the <code>ontologyIRI</code> property) will be loaded
     * from
     *
     * @return a spring Resource representing this ontology
     */
    public Resource getOntologyResource() {
        return ontologyResource;
    }

    /**
     * Returns a series of mappings between two IRIs to describe where to load any imported ontologies, declared by the
     * ontology being loaded, should be acquired from.  In the returned map, each key is a logical name of an imported
     * ontology, and each value is the physical location the ontology should be loaded from.  In other words, if this
     * <code>OntologyLoader</code> loads an ontology <code>http://www.test.com/ontology_A</code> and ontology A
     * declares
     * <pre><owl:imports rdf:resource="http://www.test.com/ontology_B" /></pre>, if no import mappings are set then
     * ontology B will be loaded from <code>http://www.test.com/ontology_B</code>.  Declaring a mapping
     * {http://www.test.com/ontology_B, file://tmp/ontologyB.owl}, though, will cause ontology B to be loaded from a
     * local copy of the file.
     *
     * @return the ontology import mappings, logical IRI -> physical location IRI
     */
    public Map<IRI, IRI> getOntologyImportMappings() {
        return ontologyImportMappings;
    }

    /**
     * Gets the IRI used to denote synonym annotations in this ontology.  As there is no convention for this (i.e. no
     * rdfs:synonym), ontologies tend to define their own.
     *
     * @return the synonym annotation IRI
     */
    public Collection<IRI> getSynonymIRIs() {
        return synonymIRIs;
    }

    /**
     * Gets the IRI used to denote definition annotations in this ontology.  As there is no convention for this (i.e. no
     * rdfs:definition), ontologies tend to define their own.
     *
     * @return the definition annotation IRI
     */
    public void setDefinitionIRIs(Collection<IRI> definitionIRIs) {
        this.definitionIRIs = definitionIRIs;
    }

    /**
     * Gets the IRI used to denote definition annotations in this ontology.  As there is no convention for this (i.e. no
     * rdfs:definition), ontologies tend to define their own.
     *
     * @return the definition annotation IRI
     */
    public void setHiddenIRIs(Collection<IRI> hiddenIRIs) {
        this.hiddenIRIs = hiddenIRIs;
    }

    public void setHierarchicalIRIs(Collection<IRI> hierarchicalIRIs) {
        this.hierarchicalRels = hierarchicalIRIs;
    }

    /**
     * Gets the IRI used to denote a class which represents the superclass of all classes to exclude in this ontology.
     * When this ontology is loaded, all subclasses of the class with this IRI will be excluded.  This is to support the
     * case where an ontology has declared an "Obsolete" class and favours moving classes under this heirarchy as
     * opposed to deleting classes.
     *
     * @return the IRI representing the class in the hierarchy that denotes classes to exclude during loading
     */
    public IRI getExclusionClassIRI() {
        return exclusionClassIRI;
    }

    /**
     * Gets the IRI of an annotation property that is used to exclude classes during loading.  This is to support the
     * case where an ontology used an annotation property to act as a flag indicating that classes should not be shown
     * or else are deprecated.  Any classes with an annotation with this IRI will be excluded from loading
     *
     * @return the IRI representing the annotation that denotes an exclusion flag
     */
    public IRI getExclusionAnnotationIRI() {
        return exclusionAnnotationIRI;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public OWLDataFactory getFactory() {
        return factory;
    }

    @Override public IRI getOntologyIRI() {
        return ontologyIRI;
    }
    @Override public IRI getOntologyVersionIRI() {
        return ontologyVersionIRI;
    }

    public OWLOntology getOntology() {
        return lazyGet(() -> ontology);
    }

    public Map<IRI, String> getOntologyTermAccessions() {
        return lazyGet(() -> ontologyAccessions);
    }

    public Map<IRI, String> getOntologyTermOboId() {
        return lazyGet(() -> oboIds);
    }

    @Override public Map<IRI, String> getTermLabels() {
        return lazyGet(() -> ontologyLabels);
    }

    @Override public Map<IRI, Collection<String>> getTermSynonyms() {
        return lazyGet(() -> ontologySynonyms);
    }

    @Override public Map<IRI, Collection<String>> getTermDefinitions() {
        return lazyGet(() -> ontologyDefinitions);
    }

    public Collection<IRI> getUnsatisfiableIris() {
        return unsatisfiableIris;
    }

    public void setUnsatisfiableIris(Collection<IRI> unsatisfiableIris) {
        this.unsatisfiableIris = unsatisfiableIris;
    }

    @Override
    public Map<IRI, Collection<String>> getAnnotations(IRI entityIRI) {
        if (termAnnotations.containsKey(entityIRI)) {
            return termAnnotations.get(entityIRI);
        }
        return Collections.emptyMap();
    }

    @Override
    public Collection<OBODefinitionCitation> getOBODefinitionCitations(IRI entityIRI) {
        if (this.oboDefinitionCitations.containsKey(entityIRI)) {
            return this.oboDefinitionCitations.get(entityIRI);
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<OBOXref> getOBOXrefs(IRI entityIRI) {
        if (this.oboXrefs.containsKey(entityIRI)) {
            return this.oboXrefs.get(entityIRI);
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<OBOSynonym> getOBOSynonyms(IRI entityIRI) {
        if (this.oboSynonyms.containsKey(entityIRI)) {
            return this.oboSynonyms.get(entityIRI);
        }
        return Collections.emptySet();
    }

    public void setOntologyIRI(IRI ontologyIRI) {
        this.ontologyIRI = ontologyIRI;
    }

    private void addClassAccession(IRI clsIri, String accession) {
        this.ontologyAccessions.put(clsIri, accession);
    }

    protected void addOboId(IRI clsIri, String oboId) {
        this.oboIds.put(clsIri, oboId);
    }

    protected void addClassLabel(IRI clsIri, String label) {
        this.ontologyLabels.put(clsIri, label);
    }

    protected void addSynonyms(IRI clsIri, Set<String> synonyms) {
        this.ontologySynonyms.put(clsIri, synonyms);
    }

    protected void addDefinitions(IRI clsIri, Set<String> definitions) {
        this.ontologyDefinitions.put(clsIri, definitions);
    }

    protected void addSlims(IRI clsIri, Set<String> slims) {
        this.slims.put(clsIri, slims);
    }

    @Override
    public String getTermReplacedBy(IRI entityIRI) {
        return termReplacedBy.get(entityIRI);
    }

    protected void addTermReplacedBy(IRI clsIri, String replacedBy) {
        this.termReplacedBy.put(clsIri, replacedBy);
    }

    @Override
    public Collection<String> getSubsets(IRI termIri) {
        if (this.slims.containsKey(termIri)) {
            return slims.get(termIri);
        }
        return Collections.emptySet();
    }

    protected Set<IRI> removeExcludedIRI(
            Set<IRI> allIris,
            Collection<IRI> iris) {
        iris.forEach(allIris::remove);
        // and return
        return allIris;
    }


    public void setPreferredPrefix(String preferredPrefix) {
        this.preferredPrefix = preferredPrefix;
    }

    public String getPreferredPrefix() {
        return preferredPrefix;
    }

    public void setOntologyAnnotations(Map<String, Collection<String>> annotations) {
        this.ontologyAnnotations = annotations;
    }

    public void setOntologyHomePage(String ontologyHomePage) {
        this.ontologyHomePage = ontologyHomePage;
    }

    public void setOntologyMailingList(String ontologyMailingList) {
        this.ontologyMailingList = ontologyMailingList;
    }

    public void setOntologyTracker(String ontologyTracker) {
        this.ontologyTracker = ontologyTracker;
    }

    public void setOntologyLogo(String ontologyLogo) {
        this.ontologyLogo = ontologyLogo;
    }


    public void setOntologyVersion(String version) {
        this.version = version;
    }

    public void setOntologyCreators(Collection<String> ontologyCreators) {
        this.ontologyCreators = ontologyCreators;
    }

    public void setOntologyTitle(String ontologyTitle) {
        this.ontologyTitle = ontologyTitle;
    }

    @Override
    public String getVersionNumber() {
        return this.version;
    }

    public String getTitle() {
        return ontologyTitle;
    }

    @Override
    public String getOntologyDescription() {
        return ontologyDescription;
    }

    public String getHomePage() {
        return ontologyHomePage;
    }

    public String getMailingList() {
        return ontologyMailingList;
    }

    public String getTracker() {
        return ontologyTracker;
    }

    public String getLogo() {
        return ontologyLogo;
    }

    public Collection<String> getCreators() {
        return ontologyCreators;
    }

    @Override
    public Map<String, Collection<String>> getOntologyAnnotations() {
        return ontologyAnnotations;
    }

    public Collection<String> getInternalMetadataProperties() {
        return internalMetadataProperties;
    }

    public Map<OWLAnnotationProperty, List<String>> getPreferredLanguageMap() {
        return preferredLanguageMap;
    }

    public void setPreferredLanguageMap(OWLAnnotationProperty property, List<String> languages) {
        this.preferredLanguageMap.put(property, languages);
    }


    public Collection<IRI> getPreferredRootTerms() {
    	return preferredRootTerms;
    }

    public void setPreferredRootTerms(Collection<IRI> preferredRootTerms) {
        this.preferredRootTerms = preferredRootTerms;
    }
}

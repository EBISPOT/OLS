package uk.ac.ebi.spot.ols.loader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import uk.ac.ebi.spot.ols.config.OboDefaults;
import uk.ac.ebi.spot.ols.config.OntologyDefaults;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.renderer.OWLHTMLVisitor;
import uk.ac.ebi.spot.ols.util.*;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

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

    private static final Pattern oboIdFragmentPattern = Pattern.compile("(([A-Za-z1-9_]*)_(\\d+))");

    private IRI ontologyIRI;
    private IRI ontologyVersionIRI;
    private String ontologyName;
    private String ontologyTitle;
    private String ontologyDescription;
    private String ontologyHomePage;
    private String ontologyMailingList;
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
    private Collection<IRI> hiddenIRIs  = new HashSet<IRI>();

    private Collection<IRI> unsatisfiableIris  = new HashSet<IRI>();

    private IRI exclusionClassIRI;
    private IRI exclusionAnnotationIRI;

    private OWLOntologyManager manager;
    private OWLDataFactory factory;
    private OWLOntology ontology;

    private Collection<String> baseIRIs = new HashSet<>();

    private Map<IRI, String> ontologyAccessions = new HashMap<>();
    private Map<IRI, String> oboIds = new HashMap<>();
    private Map<IRI, String> ontologyLabels = new HashMap<>();
    private Map<IRI, Collection<String>> ontologySynonyms = new HashMap<>();
    private Map<IRI, Collection<String>> ontologyDefinitions = new HashMap<>();
    private Map<IRI, Map<IRI,Collection<String>>> termAnnotations = new HashMap<>();
    private Collection<IRI> obsoleteTerms = new HashSet<>();
    private Map<IRI, Collection<String>> slims = new HashMap<>();
    private Collection<IRI> localTerms = new HashSet<>();
    private Collection<IRI> rootTerms = new HashSet<>();

    private Map<IRI, Collection<IRI>> directParentTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> allParentTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> directChildTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> allChildTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> equivalentTerms = new HashMap<>();
    private Map<IRI, Map<IRI,Collection<IRI>>> relatedTerms = new HashMap<>();
    private Map<IRI, Map<IRI,Collection<IRI>>> relatedParentTerms = new HashMap<>();
    private Map<IRI, Collection<IRI>> relatedChildTerms = new HashMap<>();
    private Map<IRI, Map<IRI,Collection<IRI>>> allRelatedTerms = new HashMap<>();

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

    private DatabaseService databaseService;

    public AbstractOWLOntologyLoader(OntologyResourceConfig config) throws OntologyLoadingException {
        this(config,null);
    }

    public AbstractOWLOntologyLoader(OntologyResourceConfig config, DatabaseService service) throws OntologyLoadingException {
        databaseService = service;
        // read from config
        setOntologyIRI(IRI.create(config.getId()));
        setOntologyName(config.getNamespace());
        setOntologyTitle(config.getTitle());

        setOntologyDescription(config.getDescription());
        setOntologyHomePage(config.getHomepage());
        setOntologyMailingList(config.getMailingList());
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
            throw new OntologyLoadingException("Can't load file from " + config.getFileLocation(), e);
        }
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

    @Override protected void doInitialization() throws Exception {
        // init owl fields

        this.manager = OWLManager.createOWLOntologyManager();
        this.manager.setSilentMissingImportsHandling(true);

        if (getOntologyResource() != null) {
            getLog().info("Mapping ontology IRI from " + getOntologyIRI() + " to " + getOntologyResource().getURI());
            this.manager.addIRIMapper(new SimpleIRIMapper(getOntologyIRI(),
                    IRI.create(getOntologyResource().getURI())));
        }
        if (getOntologyImportMappings() != null) {
            for (IRI from : getOntologyImportMappings().keySet()) {
                IRI to = getOntologyImportMappings().get(from);
                getLog().info("Mapping imported ontology IRI from " + from + " to " + to);
                this.manager.addIRIMapper(new SimpleIRIMapper(from, to));
            }
        }
        this.factory = manager.getOWLDataFactory();

        // collect things we want to ignore form OWL vocab
        owlVocabulary.add(factory.getOWLThing().getIRI());
        owlVocabulary.add(factory.getOWLNothing().getIRI());
        owlVocabulary.add(factory.getOWLTopObjectProperty().getIRI());
        owlVocabulary.add(factory.getOWLBottomObjectProperty().getIRI());

        // load the ontology
        this.ontology = loadOntology();
    }

    @Override protected void doTermination() throws Exception {
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
            getLog().debug("Loading ontology...");
            this.ontology = getManager().loadOntology(getOntologyIRI());
            IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();

            if (ontology.getOntologyID().getVersionIRI() != null) {
                ontologyVersionIRI = ontology.getOntologyID().getVersionIRI();

                String oboVersion = parseOboVersion(ontologyVersionIRI);
                if (oboVersion != null) {
                    setOntologyVersion(oboVersion);
                }

            }
            if (getOntologyName() == null) {
                String name = extractShortForm(ontologyIRI).get();
                if (name == null) {
                    getLog().warn("Can't shorten the name for " + ontologyIRI.toString());
                    name = ontologyIRI.toString();
                }
                setOntologyName(name);
            }
            getLog().debug("Successfully loaded ontology " + ontologyIRI);

            this.provider = new AnnotationValueShortFormProvider(
                    Collections.singletonList(factory.getOWLAnnotationProperty(getLabelIRI())),
                    Collections.<OWLAnnotationProperty, List<String>>emptyMap(),
                    manager);
            this.manSyntaxRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
            manSyntaxRenderer.setShortFormProvider(provider);

            // this call will initialise the reasoner
            getOWLReasoner(ontology);

            // cache all URIs for classes, properties and individuals
            getLog().debug("Computing indexes...");


            Collection<OWLEntity> allEntities = new HashSet<>();
            for (OWLOntology ontology1 : manager.getOntologies()) {
                allEntities.addAll(ontology1.getSignature());
            }
            indexTerms(allEntities);
            indexOntologyAnnotations(ontology.getAnnotations());

            return ontology;
        }
        catch (Exception e) {
            setInitializationException(e);
            getLog().error("Failed to parse " + getOntologyName() + " : " + e.getMessage());
            throw e;
        }
        finally {
            setReady(true);
            discardReasoner(ontology);
        }
    }

    public static String parseOboVersion(IRI ontologyVersionIRI) {
        Pattern p = Pattern.compile(".*\\/(\\d{4}-\\d{2}-\\d{2})\\/.*");
        String DATE_FORMAT = "yyyy-MM-dd";
        String iriAsString = ontologyVersionIRI.toString();
        Matcher m = p.matcher(iriAsString);
        if (m.matches()) {
            String versionDate = m.group(1);

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

        Set<String> creators = new HashSet<>();
        Map<String, Collection<String>> annotations = new HashMap<>();
        for (OWLAnnotation annotation : owlAnnotations) {

            OWLAnnotationProperty property = annotation.getProperty();
            OWLAnnotationValue value = annotation.getValue();
            Optional<String> thevalue = getOWLAnnotationValueAsString(value);

            IRI propertyIri = property.getIRI();

            if (propertyIri.toString().equals(OntologyDefaults.DEFINITION)) {
                if (thevalue.isPresent()) {
                    setOntologyDescription(thevalue.get());
                    internalMetadataProperties.add(OntologyDefaults.DEFINITION);
                }
            }
            else if (propertyIri.toString().equals(OntologyDefaults.TITLE)) {
                if (thevalue.isPresent()) {
                    setOntologyTitle(thevalue.get());
                    internalMetadataProperties.add(OntologyDefaults.TITLE);
                }
            }
            else if (propertyIri.toString().equals(OntologyDefaults.CREATOR)) {
                if (thevalue.isPresent()) {
                    creators.add(thevalue.get());
                    internalMetadataProperties.add(OntologyDefaults.CREATOR);
                }
            }
            else if (propertyIri.toString().equals(OntologyDefaults.MAILINGLIST)) {
                if (thevalue.isPresent()) {
                    setOntologyMailingList(thevalue.get());
                    internalMetadataProperties.add(OntologyDefaults.MAILINGLIST);

                }
            }
            else if (propertyIri.toString().equals(OntologyDefaults.HOMEPAGE)) {
                if (thevalue.isPresent()) {
                    setOntologyHomePage(thevalue.get());
                    internalMetadataProperties.add(OntologyDefaults.HOMEPAGE);
                }
            }
            else if (propertyIri.toString().equals(OntologyDefaults.VERSION)) {
                if (thevalue.isPresent()) {
                    setOntologyVersion(thevalue.get());
                    internalMetadataProperties.add(OntologyDefaults.VERSION);
                }
            }
            else  {
                String propertyLabel = ontologyLabels.get(propertyIri);
                if (!annotations.containsKey(propertyLabel)) {
                    annotations.put(propertyLabel, new HashSet<>());
                }
                annotations.get(propertyLabel).add(thevalue.get());
            }
        }
        setOntologyCreators(creators);
        setOntologyAnnotations(annotations);
    }

    protected void indexTerms(Collection<OWLEntity> entities) {

        getLog().debug("Starting to index " + entities.size() + " entities");

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
                        getLog().error("unable to index classes, unable to create reasoner");
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
                    // add types as parents
                    Set<IRI> instanceTypes = new HashSet<IRI>();
                    for (OWLClassExpression expression : individual.getTypes(ontology)) {
                        if (expression instanceof  OWLClass) {
                            instanceTypes.add( ((OWLClass) expression).getIRI());
                        }
                    }
                    if (!instanceTypes.isEmpty()) {
                        addDirectParents(individual.getIRI(), instanceTypes);
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
            });
        }
    }

    private void indexSubAnnotationPropertyRelations(OWLAnnotationProperty property) {

        Set<IRI> superProperties = new HashSet<>();
        for (OWLAnnotationProperty owlProperty : property.getSuperProperties(ontology)) {
            superProperties.add(owlProperty.asOWLAnnotationProperty().getIRI());
        }
        addDirectParents(property.getIRI(), superProperties);

        Set<IRI> subProperties = new HashSet<>();
        for (OWLAnnotationProperty owlProperty : property.getSubProperties(ontology)) {
            subProperties.add(owlProperty.asOWLAnnotationProperty().getIRI());
        }
        addDirectChildren(property.getIRI(), subProperties);
    }

    private void indexSubPropertyRelations(OWLObjectProperty property) {

        Set<IRI> superProperties = new HashSet<>();
        for (OWLObjectPropertyExpression owlProperty : property.getSuperProperties(ontology)) {
            if (!owlProperty.isAnonymous()) {
                superProperties.add(owlProperty.asOWLObjectProperty().getIRI());
            }
        }
        addDirectParents(property.getIRI(), superProperties);

        Set<IRI> subProperties = new HashSet<>();
        for (OWLObjectPropertyExpression owlProperty : property.getSubProperties(ontology)) {
            if (!owlProperty.isAnonymous()) {
                subProperties.add(owlProperty.asOWLObjectProperty().getIRI());
            }
        }
        addDirectChildren(property.getIRI(), subProperties);
    }

    protected  void indexSubclassRelations(OWLClass owlClass) throws OWLOntologyCreationException {

        OWLReasoner reasoner = getOWLReasoner(ontology);

        Set<OWLClass> directSubClasses = reasoner.getSubClasses(owlClass, true).getFlattened();
        Set<OWLClass> allSubClasses = reasoner.getSubClasses(owlClass, false).getFlattened();
        Set<OWLClass> directSuperClasses = reasoner.getSuperClasses(owlClass, true).getFlattened();
        Set<OWLClass> allSuperClasses = reasoner.getSuperClasses(owlClass, false).getFlattened();

        // use reasoner to check if root
        if (directSuperClasses.contains(getFactory().getOWLThing())) {
            addRootsTerms(owlClass.getIRI());
        }

        if (directSuperClasses.contains(getFactory().getOWLClass(Namespaces.OBOINOWL.createIRI("ObsoleteClass")))) {
            addObsoleteTerms(owlClass.getIRI());
        }

        // get direct children

        Set<IRI> ct = removeExcludedIRI(
                directSubClasses.parallelStream()
                        .map(OWLNamedObject::getIRI)
                        .collect(Collectors.toSet()),
                owlVocabulary);
        if (ct.size() >0) addDirectChildren(owlClass.getIRI(), ct) ;

        // get all children
        Set<IRI> act = removeExcludedIRI(
                allSubClasses.parallelStream()
                        .map(OWLNamedObject::getIRI)
                        .collect(Collectors.toSet()),
                owlVocabulary);
        if (act.size() >0) addAllChildren(owlClass.getIRI(), act);

        // get parents
        Set<IRI> dp =
                removeExcludedIRI(
                        directSuperClasses.parallelStream()
                                .map(OWLNamedObject::getIRI)
                                .collect(Collectors.toSet()),
                        owlVocabulary);
        if (dp.size()>0) addDirectParents(owlClass.getIRI(), dp);

        // get all parents
        Set<IRI> ap =
                removeExcludedIRI(
                        allSuperClasses.parallelStream()
                                .map(OWLNamedObject::getIRI)
                                .collect(Collectors.toSet()),
                        owlVocabulary);
        if (ap.size()>0) addAllParents(owlClass.getIRI(), ap);

        // map of related parent terms for hierarchy views
        Map<IRI, Collection<IRI>> relatedParentTerms = new HashMap<>();

        // find direct related terms
        Map<IRI, Collection<IRI>> relatedTerms = new HashMap<>();

        Set<String> relatedDescriptions = new HashSet<>();
        for (OWLClassExpression expression : owlClass.getSuperClasses(getManager().getOntologies())) {
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
                                getLog().warn("Ignoring Iri that is related to itself: " + owlClass.getIRI());
                            } else  {
                                if (!relatedParentTerms.containsKey(propertyIRI)) {
                                    relatedParentTerms.put(propertyIRI, new HashSet<>());
                                }
                                relatedParentTerms.get(propertyIRI).add(relatedTerm);
                                addRelatedChildTerm(relatedTerm, owlClass.getIRI());
                            }


                        }

                    }
                }


                // store stringified form of class description
                relatedDescriptions.add(renderHtml(expression));
            }
        }
        if (!relatedTerms.isEmpty()) {
            addRelatedTerms(owlClass.getIRI(), relatedTerms );
        }

        addRelatedParentTerms(owlClass.getIRI(), relatedParentTerms);

        if (!relatedDescriptions.isEmpty()) {

            addSuperClassDescriptions(owlClass.getIRI(), relatedDescriptions);

        }
        // todo find transitive closure of related terms
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

        for (OWLClassExpression expression : owlClass.getEquivalentClasses(getManager().getOntologies())) {
            if (expression.isAnonymous()) {
                relatedDescriptions.add(renderHtml(expression));
            }
        }

        if (!relatedDescriptions.isEmpty()) {
            addEquivalentClassDescriptions(owlClass.getIRI(), relatedDescriptions);
        }

    }


    protected Optional<String> extractShortForm(IRI entityIRI) {

        getLog().trace("Attempting to extract fragment name of URI '" + entityIRI + "'");
        String termURI = entityIRI.toString();

        // we want the "final part" of the URI...

        // oput in try block to catch any URL exceptins
        try {
            if (!StringUtils.isEmpty(entityIRI.toURI().getFragment())) {
                // a uri with a non-null fragment, so use this...
                getLog().trace("Extracting fragment name using URI fragment (" + entityIRI.toURI().getFragment() + ")");
                return Optional.of(entityIRI.toURI().getFragment());
            }
        } catch (Exception e) {
            // carry on and try some other strategies
        }

        if (entityIRI.getRemainder().isPresent()) {
            return Optional.of(entityIRI.getRemainder().get());
        }
        else if (entityIRI.toURI().getPath() != null) {
            // no fragment, but there is a path so try and extract the final part...
            if (entityIRI.toURI().getPath().contains("/")) {
                getLog().trace("Extracting fragment name using final part of the path of the URI");
                return Optional.of(entityIRI.toURI().getPath().substring(entityIRI.toURI().getPath().lastIndexOf('/') + 1));
            }
            else {
                // no final path part, so just return whole path
                getLog().trace("Extracting fragment name using the path of the URI");
                return Optional.of(entityIRI.toURI().getPath());
            }
        }
        else {
            // no fragment, path is null, we've run out of rules so don't shorten
            getLog().trace("No rules to shorten this URI could be found (" + termURI + ")");
            return Optional.empty();
        }
    }


    private Optional<String> getOBOid(String fragment) {
        Matcher matcher = oboIdFragmentPattern.matcher(fragment);
        if (matcher.find()) {
            String newId = matcher.group(2) + ":" + matcher.group(3);
            return Optional.of(newId);
        }
        return Optional.empty();
    }


    protected Optional<String> evaluateLabelAnnotationValue(OWLEntity entity, OWLAnnotationValue value) {
        // get label annotations
        Optional<String> label = getOWLAnnotationValueAsString(value);
        if (!label.isPresent()) {
            // try and get the URI fragment and use that as label
            Optional<String> fragment = extractShortForm(entity.getIRI());
            if (fragment.isPresent()) {
                return Optional.of(fragment.get());
            }
            else {
                getLog().warn("OWLEntity " + entity + " contains no label. " +
                        "No labels for this class will be loaded.");
                return  Optional.of(entity.toStringID());
            }
        }
        return label;
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
        for (OWLOntology ontology1 : getManager().getOntologies()) {
            for (OWLAnnotationAssertionAxiom annotation : owlEntity.getAnnotationAssertionAxioms(ontology1)) {
                OWLAnnotationProperty property = annotation.getProperty();
                IRI propertyIRI = property.getIRI();

                if (getLabelIRI().equals(propertyIRI)) {
                    addClassLabel(owlEntityIRI, evaluateLabelAnnotationValue(owlEntity, annotation.getValue()).get());
                }
                else if (getSynonymIRIs().contains(propertyIRI)) {
                    synonyms.add(getOWLAnnotationValueAsString(annotation.getValue()).get());
                }
                else if (getDefinitionIRIs().contains(propertyIRI)) {
                    definitions.add(getOWLAnnotationValueAsString(annotation.getValue()).get());
                }
                else if (propertyIRI.equals(Namespaces.OBOINOWL.createIRI("inSubset")) && annotation.getValue() instanceof IRI) {
                    if (extractShortForm( (IRI) annotation.getValue()).isPresent()) {
                        slims.add(extractShortForm( (IRI) annotation.getValue()).get());
                    }
                }
                else if (propertyIRI.equals(Namespaces.OWL.createIRI("deprecated"))) {
                    addObsoleteTerms(owlEntityIRI);
                }
                else {
                    if (getOWLAnnotationValueAsString(annotation.getValue()).isPresent()) {
                        // initialise maps if first time
                        if (!termAnnotations.containsKey(owlEntityIRI)) {
                            HashMap<IRI, Collection<String>> newMap = new HashMap<>();
                            newMap.put(propertyIRI, new HashSet<>());
                            termAnnotations.put(owlEntityIRI, newMap);
                        }

                        if (!termAnnotations.get(owlEntityIRI).containsKey(propertyIRI)) {
                            termAnnotations.get(owlEntityIRI).put(propertyIRI, new HashSet<>());
                        }

                        if (annotation.getValue() instanceof IRI) {
                            termAnnotations.get(owlEntityIRI).get(propertyIRI).add(annotation.getValue().toString());
                        }
                        else {
                            termAnnotations.get(owlEntityIRI).get(propertyIRI).add(getOWLAnnotationValueAsString(annotation.getValue()).get());
                        }
                    }
                }

                // collect any obo definition xrefs
                if (annotation.getProperty().getIRI().toString().equals(OboDefaults.DEFINITION)) {
                    if (!annotation.getAnnotations().isEmpty()) {

                        OBODefinitionCitation definitionCitation = new OBODefinitionCitation();
                        Collection<OBOXref> oboXrefs = new HashSet<>();
                        for (OWLAnnotation defAnnotation : annotation.getAnnotations()) {
                            oboXrefs.add(extractOBOXrefs(defAnnotation));
                        }
                        definitionCitation.setDefinition(getOWLAnnotationValueAsString(annotation.getValue()).get());
                        definitionCitation.setOboXrefs(oboXrefs);
                        definitionCitations.add(definitionCitation);
                    }
                }

                // collect any obo synonym xrefs
                if (
                        annotation.getProperty().getIRI().toString().equals(OboDefaults.EXACT_SYNONYM)
                                || annotation.getProperty().getIRI().toString().equals(OboDefaults.RELATED_SYNONYM)
                                || annotation.getProperty().getIRI().toString().equals(OboDefaults.NARROW_SYNONYM)
                                || annotation.getProperty().getIRI().toString().equals(OboDefaults.BROAD_SYNONYM)

                        ) {
                    if (!annotation.getAnnotations().isEmpty()) {

                        OBOSynonym synonymCitation = new OBOSynonym();

                        synonymCitation.setName(getOWLAnnotationValueAsString(annotation.getValue()).get());
                        synonymCitation.setScope(annotation.getProperty().getIRI().getShortForm());
                        String type;
                        Collection<OBOXref> oboXrefs = new HashSet<>();
                        for (OWLAnnotation annotationAxiomAnnotation : annotation.getAnnotations()) {
                            if (annotationAxiomAnnotation.getProperty().getIRI().toString().equals(OboDefaults.SYNONYM_TYPE)) {
                                OWLAnnotationValue value = annotationAxiomAnnotation.getValue();
                                if (value instanceof IRI) {
                                    for (OWLAnnotation valueAnnotation : factory.getOWLAnnotationProperty((IRI) value).getAnnotations(ontology)) {
                                        if (valueAnnotation.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {
                                            type = getOWLAnnotationValueAsString(valueAnnotation.getValue()).get();
                                            synonymCitation.setType(type);
                                        }
                                    }
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
                if (annotation.getProperty().getIRI().toString().equals(OboDefaults.DBXREF)) {
                    OBOXref oboXrefs = extractOBOXrefs(annotation.getAnnotation());
                    if (!annotation.getAnnotations().isEmpty()) {
                        for (OWLAnnotation axiomAnnotation : annotation.getAnnotations()) {
                            String description = getOWLAnnotationValueAsString(axiomAnnotation.getValue()).get();
                            oboXrefs.setDescription(description);
                        }
                    }
                    oboEntityXrefs.add(oboXrefs);
                }

            }
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
            if ( shortForm.isPresent()) {
                return Optional.of(shortForm.get());
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


    public Map<IRI, Collection<IRI>> getRelatedTerms(IRI entityIRI) {
        if (relatedTerms.containsKey(entityIRI)) {
            return relatedTerms.get(entityIRI);
        }
        return Collections.emptyMap();
    }

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
                        getLog().warn("Cycle detected where is " + entityIRI + " is related to one of its decendants");
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
                        getLog().warn("Cycle detected where " + entityIRI + " is related to one of its decendants");
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
                        getLog().warn("Cycle detected where " + entityIRI + " is a subclass of itself");
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

    protected void addClassAccession(IRI clsIri, String accession) {
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

    @Override public Collection<String> getSubsets(IRI termIri) {
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


}

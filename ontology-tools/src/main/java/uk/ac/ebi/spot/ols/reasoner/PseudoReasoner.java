package uk.ac.ebi.spot.ols.reasoner;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PseudoReasoner implements OWLReasoner {

    private OWLOntology owlOntology;
    private final Logger logger = LoggerFactory.getLogger(PseudoReasoner.class);

    private Map<OWLClass, Set<Node<OWLClass>>> allSuperClasses = new HashMap<>();
    private Map<OWLClass, Set<Node<OWLClass>>> allSubClasses = new HashMap<>();

    private final Marker ALL_SUB_CLASSES = MarkerFactory.getMarker("AllSubClasses");
    private final Marker ALL_SUPER_CLASSES = MarkerFactory.getMarker("AllSuperClasses");
    private final Marker EQUIVALENT_CLASSES = MarkerFactory.getMarker("EquivalentClasses");
    private final Marker SUB_CLASSES = MarkerFactory.getMarker("SubClasses");
    private final Marker SUPER_CLASSES = MarkerFactory.getMarker("SuperClasses");

    protected Logger getLogger() {
        return logger;
    }

    public PseudoReasoner(OWLOntology owlOntology) {
        this.owlOntology = owlOntology;
    }

    @Override
    public String getReasonerName() {
        return "Pseudo";
    }

    @Override
    public Version getReasonerVersion() {
        return null;
    }

    @Override
    public BufferingMode getBufferingMode() {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        return null;
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        return null;
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        return null;
    }

    @Override
    public OWLOntology getRootOntology() {
        return null;
    }

    @Override
    public void interrupt() {

    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {

    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        return false;
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return null;
    }

    @Override
    public boolean isConsistent() {
        return true;
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) {
        return true;
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return new OWLClassNode(new HashSet<>());
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        return false;
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        return false;
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return false;
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        return null;
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return null;
    }

    private Set<Node<OWLClass>> getAllSubClasses(Set<Node<OWLClass>> setOfSubClasses, OWLClass owlClass) {
        getLogger().debug(ALL_SUB_CLASSES, "Input = {}", owlClass.toString());
        if (allSubClasses.containsKey(owlClass))
            return allSubClasses.get(owlClass);

        if (setOfSubClasses.contains(owlClass)) {
            getLogger().debug(ALL_SUB_CLASSES, "Output = {}", setOfSubClasses.toString());
            allSubClasses.put(owlClass, setOfSubClasses);
            return setOfSubClasses;
        } else {
            setOfSubClasses.add(new OWLClassNode(owlClass));
            Set<Node<OWLClass>> owlClassNodeSetToAdd = EntitySearcher
                    .getSubClasses(owlClass, owlOntology.importsClosure())
                    .filter(classExpression -> classExpression.isOWLClass())
                    .map(classExpression -> new OWLClassNode(classExpression.asOWLClass()))
                    .collect(Collectors.toSet());
            owlClassNodeSetToAdd.forEach(owlClassNode -> setOfSubClasses.addAll(
                    getAllSubClasses(setOfSubClasses, owlClassNode.getRepresentativeElement())));
            allSubClasses.put(owlClass, setOfSubClasses);
            getLogger().debug(ALL_SUB_CLASSES, "Output = {}", setOfSubClasses.toString());
            return setOfSubClasses;
        }
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression owlClassExpression, boolean direct) {
        getLogger().debug(SUB_CLASSES, "Input = {}", owlClassExpression.toString());
        if (direct) {
            Set<Node<OWLClass>> owlClassNodeSet = EntitySearcher
                    .getSubClasses(owlClassExpression.asOWLClass(), owlOntology.importsClosure())
                    .filter(ce -> ce.isOWLClass())
                    .map(ce -> new OWLClassNode(ce.asOWLClass()))
                    .collect(Collectors.toSet());
            getLogger().debug(SUB_CLASSES, "Output = {}", owlClassNodeSet.toString());
            OWLClassNodeSet owlClassNodeSetInstance = new OWLClassNodeSet(owlClassNodeSet);
            return owlClassNodeSetInstance;
        } else {
            if (allSubClasses.containsKey(owlClassExpression.asOWLClass()))
                return new OWLClassNodeSet(allSubClasses.get(owlClassExpression.asOWLClass()));
            Set<Node<OWLClass>> owlClassNodeSet = new HashSet<>();
            owlClassNodeSet = getAllSubClasses(owlClassNodeSet, owlClassExpression.asOWLClass());
            getLogger().debug(SUB_CLASSES, "Output = {}", owlClassNodeSet.toString());
            return new OWLClassNodeSet(owlClassNodeSet);
        }
    }

    private Set<Node<OWLClass>> getAllSuperClasses(Set<Node<OWLClass>> setOfSuperClasses, OWLClass owlClass) {
        getLogger().debug(ALL_SUPER_CLASSES, "Input = ", owlClass.toString());
        if (allSuperClasses.containsKey(owlClass))
            return allSuperClasses.get(owlClass);

        if (setOfSuperClasses.contains(owlClass)) {
            getLogger().debug(ALL_SUPER_CLASSES, "Output = {}", setOfSuperClasses.toString());
            allSuperClasses.put(owlClass, setOfSuperClasses);
            return setOfSuperClasses;
        } else {
            setOfSuperClasses.add(new OWLClassNode(owlClass));
            Set<Node<OWLClass>> owlClassNodeSetToAdd = EntitySearcher
                    .getSuperClasses(owlClass, owlOntology.importsClosure())
                    .filter(classExpression -> classExpression.isOWLClass())
                    .map(classExpression -> new OWLClassNode(classExpression.asOWLClass()))
                    .collect(Collectors.toSet());
            owlClassNodeSetToAdd.forEach(owlClassNode -> setOfSuperClasses.addAll(
                    getAllSuperClasses(setOfSuperClasses, owlClassNode.getRepresentativeElement())));
            allSuperClasses.put(owlClass, setOfSuperClasses);
            getLogger().debug(ALL_SUPER_CLASSES, "Output = {}", setOfSuperClasses.toString());
            return setOfSuperClasses;
        }
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression owlClassExpression, boolean direct) {
        getLogger().debug(SUPER_CLASSES, "Input = {}", owlClassExpression.toString());
        if (direct) {
            Set<Node<OWLClass>> owlClassNodeSet = EntitySearcher
                    .getSuperClasses(owlClassExpression.asOWLClass(), owlOntology.importsClosure())
                    .filter(ce -> ce.isOWLClass())
                    .map(ce -> new OWLClassNode(ce.asOWLClass()))
                    .collect(Collectors.toSet());
            OWLClassNodeSet owlClassNodeSetInstance = new OWLClassNodeSet(owlClassNodeSet);
            getLogger().debug(SUPER_CLASSES, "Output = {}", owlClassNodeSet.toString());
            return owlClassNodeSetInstance;
        } else {
            if (allSuperClasses.containsKey(owlClassExpression.asOWLClass()))
                return new OWLClassNodeSet(allSuperClasses.get(owlClassExpression.asOWLClass()));
            Set<Node<OWLClass>> owlClassNodeSet = new HashSet<>();
            owlClassNodeSet = getAllSuperClasses(owlClassNodeSet, owlClassExpression.asOWLClass());
            allSuperClasses.put(owlClassExpression.asOWLClass(), owlClassNodeSet);
            getLogger().debug(SUPER_CLASSES, "Output = {}", owlClassNodeSet.toString());
            return new OWLClassNodeSet(owlClassNodeSet);
        }
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression owlClassExpression) {
        getLogger().debug(EQUIVALENT_CLASSES, "Input = {}", owlClassExpression.toString());
        Set<OWLClass> owlClassSet = EntitySearcher
                .getEquivalentClasses(owlClassExpression.asOWLClass(), owlOntology.importsClosure())
                .filter(ce -> ce.isOWLClass())
                .map(ce -> ce.asOWLClass())
                .collect(Collectors.toSet());
        getLogger().debug(EQUIVALENT_CLASSES, "Output = {}", owlClassSet.toString());
        return new OWLClassNode(owlClassSet);
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return null;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {
        return null;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
        return null;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return null;
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return null;
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
        return null;
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
        return null;
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
        return null;
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
        return null;
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
        return null;
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
        getLogger().debug("Input = {}", ind.toString());
        Set<Node<OWLClass>> owlClassNodeSet = EntitySearcher
                .getTypes(ind, owlOntology.importsClosure())
                .filter(classExpression -> classExpression.isOWLClass())
                .map(classExpression -> new OWLClassNode(classExpression.asOWLClass()))
                .collect(Collectors.toSet());
        getLogger().debug("Output = {}", owlClassNodeSet.toString());
        return new OWLClassNodeSet(owlClassNodeSet);
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        return null;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual owlNamedIndividual,
                                                               OWLObjectPropertyExpression owlObjectPropertyExpression) {
        getLogger().debug("Input = {}" , owlNamedIndividual.toString());
        Collection<OWLIndividual> owlIndividuals = EntitySearcher
                .getObjectPropertyValues(owlNamedIndividual, owlOntology.importsClosure())
                .values();
        Set<Node<OWLNamedIndividual>> owlNamedIndividualNodeSet = new HashSet();
        for (OWLIndividual owlIndividual: owlIndividuals) {
            if (owlIndividual.isOWLNamedIndividual())
                owlNamedIndividualNodeSet.add(new OWLNamedIndividualNode(owlIndividual.asOWLNamedIndividual()));
        }
        getLogger().debug("Output = {}"  + owlNamedIndividualNodeSet.toString());
        return new OWLNamedIndividualNodeSet(owlNamedIndividualNodeSet);
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
        return null;
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
        return null;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
        return null;
    }

    @Override
    public long getTimeOut() {
        return 0;
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return null;
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return null;
    }

    @Override
    public void dispose() {

    }
}

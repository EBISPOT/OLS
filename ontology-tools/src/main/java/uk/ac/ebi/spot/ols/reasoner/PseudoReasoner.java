package uk.ac.ebi.spot.ols.reasoner;

import com.google.common.collect.Multimap;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.Version;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PseudoReasoner implements OWLReasoner {

    private OWLOntology owlOntology;

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

    private Set<Node<OWLClass>> getSubClasses(Set<Node<OWLClass>> owlClassNodeSet, OWLClass owlClass) {
        if (owlClassNodeSet.contains(owlClass)) {
            return owlClassNodeSet;
        } else {
            owlClassNodeSet.add(new OWLClassNode(owlClass));
            Set<Node<OWLClass>> owlClassNodeSetToAdd = EntitySearcher
                    .getSubClasses(owlClass, owlOntology.importsClosure())
                    .map(classExpression -> new OWLClassNode(classExpression.asOWLClass()))
                    .collect(Collectors.toSet());
            owlClassNodeSetToAdd.forEach(owlClassNode -> owlClassNodeSet.addAll(
                    getSubClasses(owlClassNodeSet, owlClassNode.getRepresentativeElement())));
            return owlClassNodeSet;
        }
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression owlClassExpression, boolean direct) {
        if (direct) {
            Set<Node<OWLClass>> owlClassNodeSet = EntitySearcher
                    .getSubClasses(owlClassExpression.asOWLClass(), owlOntology.importsClosure())
                    .map(ce -> new OWLClassNode(ce.asOWLClass()))
                    .collect(Collectors.toSet());
            return new OWLClassNodeSet(owlClassNodeSet);
        } else {
            Set<Node<OWLClass>> owlClassNodeSet = new HashSet<>();
            owlClassNodeSet = getSubClasses(owlClassNodeSet, owlClassExpression.asOWLClass());
            return new OWLClassNodeSet(owlClassNodeSet);
        }
    }

    private Set<Node<OWLClass>> getSuperClasses(Set<Node<OWLClass>> owlClassNodeSet, OWLClass owlClass) {
        if (owlClassNodeSet.contains(owlClass)) {
            return owlClassNodeSet;
        } else {
            owlClassNodeSet.add(new OWLClassNode(owlClass));
            Set<Node<OWLClass>> owlClassNodeSetToAdd = EntitySearcher
                    .getSuperClasses(owlClass, owlOntology.importsClosure())
                    .map(classExpression -> new OWLClassNode(classExpression.asOWLClass()))
                    .collect(Collectors.toSet());
            owlClassNodeSetToAdd.forEach(owlClassNode -> owlClassNodeSet.addAll(
                    getSuperClasses(owlClassNodeSet, owlClassNode.getRepresentativeElement())));
            return owlClassNodeSet;
        }
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression owlClassExpression, boolean direct) {
        if (direct) {
            Set<Node<OWLClass>> owlClassNodeSet = EntitySearcher
                    .getSuperClasses(owlClassExpression.asOWLClass(), owlOntology.importsClosure())
                    .map(ce -> new OWLClassNode(ce.asOWLClass()))
                    .collect(Collectors.toSet());
            return new OWLClassNodeSet(owlClassNodeSet);
        } else {
            Set<Node<OWLClass>> owlClassNodeSet = new HashSet<>();
            owlClassNodeSet = getSuperClasses(owlClassNodeSet, owlClassExpression.asOWLClass());
            return new OWLClassNodeSet(owlClassNodeSet);
        }
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression owlClassExpression) {
        Set<OWLClass> owlClassSet = EntitySearcher
                .getEquivalentClasses(owlClassExpression.asOWLClass(), owlOntology.importsClosure())
                .map(ce -> ce.asOWLClass())
                .collect(Collectors.toSet());
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
        Set<Node<OWLClass>> owlClassNodeSet = EntitySearcher
                .getTypes(ind, owlOntology.importsClosure())
                .map(classExpression -> new OWLClassNode(classExpression.asOWLClass()))
                .collect(Collectors.toSet());
        return new OWLClassNodeSet(owlClassNodeSet);
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        return null;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual owlNamedIndividual,
                                                               OWLObjectPropertyExpression owlObjectPropertyExpression) {
        Collection<OWLIndividual> owlIndividuals = EntitySearcher
                .getObjectPropertyValues(owlNamedIndividual, owlOntology.importsClosure())
                .values();
        Set<Node<OWLNamedIndividual>> owlNamedIndividualNodeSet = new HashSet();
        for (OWLIndividual owlIndividual: owlIndividuals) {
            owlNamedIndividualNodeSet.add(new OWLNamedIndividualNode(owlIndividual.asOWLNamedIndividual()));
        }

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

package uk.ac.ebi.spot.ols.renderer;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 12, 2008<br><br>
 *
 *
 * Modified: Simon Jupp<br>
 * EMBL European Bioinformatics Institute
 * Date: July 14, 2015
 */
public class OWLHTMLVisitor implements OWLObjectVisitor {

    private Logger log = LoggerFactory.getLogger(getClass());

    // These should match the css class names
    private static final String CSS_DEPRECATED = "deprecated";
    private static final String CSS_ACTIVE_ENTITY = "active-entity";
    private static final String CSS_KEYWORD = "keyword";
    private static final String CSS_ONTOLOGY_URI = "ontology-uri";
    private static final String CSS_ACTIVE_ONTOLOGY_URI = "active-ontology-uri";
    private static final String CSS_SOME = "some";
    private static final String CSS_ONLY = "only";
    private static final String CSS_VALUE = "value";
    private static final String CSS_LITERAL = "literal";
    private static final String CSS_ANNOTATION_URI = "annotation-uri";

    // the subset and equivalence symbols can be encoded in HTML
    private static final boolean USE_SYMBOLS = true;

    private PrintWriter out;

    private ShortFormProvider sfProvider;

    private Set<OWLOntology> ontologies = new HashSet<OWLOntology>();

    private OWLOntology activeOntology = null;

    private int indent = 0;

    private boolean writeStats = false;

    public OWLHTMLVisitor(ShortFormProvider provider, PrintWriter out) {
        this.sfProvider = provider;
        this.out = out;
    }


    public void setOntologies(Set<OWLOntology> ontologies){
        this.ontologies = ontologies;
    }

    public void setActiveOntology(OWLOntology activeOnt){
        this.activeOntology = activeOnt;
    }

    private void write(String s) {
        out.write(s);
    }


    ////////// Entities

    public void visit(OWLClass desc) {
        writeOWLEntity(desc, NamedObjectType.classes.getSingularRendering());
    }

    public void visit(OWLDataProperty property) {
        writeOWLEntity(property, NamedObjectType.dataproperties.getSingularRendering());
    }

    public void visit(OWLObjectProperty property) {
        writeOWLEntity(property, NamedObjectType.objectproperties.getSingularRendering());
    }

    public void visit(OWLAnnotationProperty property) {
        writeOWLEntity(property, NamedObjectType.annotationproperties.getSingularRendering());
    }

    public void visit(OWLNamedIndividual individual) {
        writeOWLEntity(individual, NamedObjectType.individuals.getSingularRendering());
    }

    @Override
    public void visit(OWLOntology owlOntology) {

    }

    public void visit(OWLDatatype datatype) {
        writeOWLEntity(datatype, NamedObjectType.datatypes.getSingularRendering());
    }

    public void visit(IRI iri) {
        writeIRIWithBoldFragment(iri, iri.getFragment());
        try {
            URL url = iri.toURI().toURL();
//            URLUtils.renderURLLinks(url, kit, pageURL, out);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void visit(OWLAnonymousIndividual individual) {
        writeAnonymousIndividual(individual);
    }

    ///////// Anonymous classes

    public void visit(OWLObjectSomeValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SOME.toString(), CSS_SOME);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    public void visit(OWLObjectAllValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.ONLY.toString(), CSS_ONLY);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    public void visit(OWLObjectHasValue desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.VALUE.toString(), CSS_VALUE);
        write(" ");
        writeOp(desc.getValue(), true);
    }

    public void visit(OWLObjectMinCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MIN.toString());
    }

    public void visit(OWLObjectExactCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.EXACTLY.toString());
    }

    public void visit(OWLObjectMaxCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MAX.toString());
    }

    public void visit(OWLObjectComplementOf desc) {
        writeKeyword(ManchesterOWLSyntax.NOT.toString());
        write(" ");
        writeOp(desc.getOperand(), false);
    }

    public void visit(OWLObjectHasSelf desc) {
        writeKeyword(ManchesterOWLSyntax.SELF.toString());
    }

    public void visit(OWLObjectIntersectionOf desc) {
        writeKeywordOpList(orderOps(desc.getOperands()), ManchesterOWLSyntax.AND.toString(), true);
    }

    public void visit(OWLObjectUnionOf desc) {
        writeKeywordOpList(orderOps(desc.getOperands()), ManchesterOWLSyntax.OR.toString(), false);
    }

    public void visit(OWLObjectOneOf desc) {
        write("{");
        writeOpList(desc.getIndividuals(), ", ", false);
        write("}");
    }

    public void visit(OWLDataOneOf desc) {
        write("{");
        writeOpList(desc.getValues(), ", ", false);
        write("}");
    }


    public void visit(OWLDataSomeValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SOME.toString(), CSS_SOME);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    public void visit(OWLDataAllValuesFrom desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.ONLY.toString(), CSS_ONLY);
        write(" ");
        writeOp(desc.getFiller(), true);
    }

    public void visit(OWLDataHasValue desc) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.VALUE.toString(), CSS_VALUE);
        write(" ");
        writeOp(desc.getValue(), true);
    }

    public void visit(OWLDataMinCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MIN.toString());
    }

    public void visit(OWLDataExactCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.EXACTLY.toString());
    }

    public void visit(OWLDataMaxCardinality desc) {
        writeCardinality(desc, ManchesterOWLSyntax.MAX.toString());
    }

    public void visit(OWLDatatypeRestriction node) {
        node.getDatatype().accept(this);
        write(" [");
        writeOpList(node.getFacetRestrictions(), ", ", false);
        write("]");
    }


    public void visit(OWLFacetRestriction node) {
        writeKeyword(writeFacet(node.getFacet()));
        node.getFacetValue().accept(this);
    }

    public void visit(OWLDataComplementOf node) {
        writeKeyword(ManchesterOWLSyntax.NOT.toString());
        write(" ");
        writeOp(node.getDataRange(), true);
    }


    public void visit(OWLDataIntersectionOf owlDataIntersectionOf) {
        writeKeywordOpList(owlDataIntersectionOf.getOperands(), ManchesterOWLSyntax.AND.toString(), true);
    }


    public void visit(OWLDataUnionOf owlDataUnionOf) {
        writeKeywordOpList(owlDataUnionOf.getOperands(), ManchesterOWLSyntax.OR.toString(), false);
    }

    ////////// Properties

    public void visit(OWLObjectInverseOf property) {
        writeKeyword(ManchesterOWLSyntax.INVERSE_OF.toString());
        write(" ");
        writeOp(property.getInverse(), true);
    }

    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.FUNCTIONAL.toString());
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.INVERSE_FUNCTIONAL.toString());
    }

    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.SYMMETRIC.toString());
    }

    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.TRANSITIVE.toString());
    }

    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.ASYMMETRIC.toString());
    }

    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.REFLEXIVE.toString());
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.IRREFLEXIVE.toString());
    }

    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.DOMAIN.toString());
        write(" ");
        writeOp(axiom.getDomain(), true);
        writeAnnotations(axiom);
    }

    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getRange(), true);
        writeAnnotations(axiom);
    }

    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        writeOp(axiom.getFirstProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.INVERSE.toString());
        write(" ");
        writeOp(axiom.getSecondProperty(), true);
        writeAnnotations(axiom);
    }


    public void visit(OWLHasKeyAxiom axiom) {
        writeOp(axiom.getClassExpression(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.HAS_KEY.toString());
        write(" ");
        write("(");
        writeOpList(axiom.getPropertyExpressions(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }


    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        axiom.getDatatype().accept(this);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.EQUIVALENT_TO.toString());
        write(" ");
        axiom.getDataRange().accept(this);
        writeAnnotations(axiom);
    }


    public void visit(SWRLRule swrlRule) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLClassAtom swrlClassAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLDataRangeAtom swrlDataRangeAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLObjectPropertyAtom swrlObjectPropertyAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLDataPropertyAtom swrlDataPropertyAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLBuiltInAtom swrlBuiltInAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLVariable swrlVariable) {
        // @@TODO SWRL Support
    }


    public void visit(SWRLIndividualArgument swrlIndividualArgument) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLLiteralArgument swrlLiteralArgument) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLSameIndividualAtom swrlSameIndividualAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(SWRLDifferentIndividualsAtom swrlDifferentIndividualsAtom) {
        // @@TODO SWRL SUpport
    }


    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        writeKeywordOpList(axiom.getPropertyChain(), "o", false);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        writeOp(axiom.getSuperProperty(), true);
        writeAnnotations(axiom);
    }


    public void visit(OWLDataPropertyDomainAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.DOMAIN.toString());
        write(" ");
        writeOp(axiom.getDomain(), true);
        writeAnnotations(axiom);
    }

    public void visit(OWLDataPropertyRangeAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getRange(), true);
        writeAnnotations(axiom);
    }

    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        writeUnaryPropertyAxiom(axiom, ManchesterOWLSyntax.FUNCTIONAL.toString());
        writeAnnotations(axiom);
    }

    ////////// Annotations

    public void visit(OWLAnnotationAssertionAxiom axiom) {
        final OWLAnnotationSubject subject = axiom.getSubject();
        // extract the entities with this IRI
        if (subject instanceof IRI){
            Set<OWLEntity> entities = new HashSet<OWLEntity>();
            for (OWLOntology ont : ontologies){
                entities.addAll(ont.getEntitiesInSignature((IRI)subject));
            }
            if (!entities.isEmpty()){
                boolean started = false;
                for (OWLEntity entity : entities){
                    if (started){
                        write("&nbsp;");
                    }
                    entity.accept(this);
                    started = true;
                }
            }
            else{
                subject.accept(this);
            }
        }
        else{
            subject.accept(this);
        }
        write("&nbsp;");
        axiom.getAnnotation().accept(this);
        write("&nbsp;");
        writeAnnotations(axiom); // in theory, you could annotate the annotation axioms !!
    }


    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        writeOp(axiom.getSubProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        writeOp(axiom.getSuperProperty(), true);
        writeAnnotations(axiom);
    }


    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getDomain(), true);
        writeAnnotations(axiom);
    }


    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        writeOp(axiom.getProperty(), true);
        write(" ");
        writeKeyword(ManchesterOWLSyntax.RANGE.toString());
        write(" ");
        writeOp(axiom.getRange(), true);
        writeAnnotations(axiom);
    }


    public void visit(OWLAnnotation annotation) {
        annotation.getProperty().accept(this);
        write(" ");
        annotation.getValue().accept(this);
    }

    // OWLAPI v3.1
    public void visit(OWLLiteral node) {
        write("<span class='" + CSS_LITERAL + "'>");
        final OWLDatatype dt = node.getDatatype();
        if (dt.isInteger() || dt.isFloat()){
            writeLiteralContents(node.getLiteral());
            write("</span>");
        }
        else{
            write("\"");
            writeLiteralContents(node.getLiteral());
            write("\"");
            write("</span>");
            if (node.isRDFPlainLiteral()){
                if (node.hasLang()){
                    final String lang = node.getLang();
                    if (lang != null){
                        write(" <span style='color: black;'>@" + lang + "</span>");
                    }
                }
            }
            else{
                write("(");
                dt.accept(this);
                write(")");
            }
        }
    }

    /////////// Axioms

    public void visit(OWLEquivalentClassesAxiom axiom) {
        writeEquivalence(orderOps(axiom.getClassExpressions()), axiom);
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        writeEquivalence(axiom.getProperties(), axiom);
    }

    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        writeEquivalence(axiom.getProperties(), axiom);
    }

    public void visit(OWLSameIndividualAxiom axiom) {
        writeKeywordOpList(axiom.getIndividuals(), ManchesterOWLSyntax.SAME_AS.toString(), false);
        writeAnnotations(axiom);
    }

    public void visit(OWLSubClassOfAxiom axiom) {
        axiom.getSubClass().accept(this);
        write(" ");
        writeKeyword( ManchesterOWLSyntax.SUBCLASS_OF.toString());
        write(" ");
        axiom.getSuperClass().accept(this);
        writeAnnotations(axiom);
    }

    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(" ");
        writeKeyword( ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        axiom.getSuperProperty().accept(this);
        writeAnnotations(axiom);
    }

    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(" ");
        writeKeyword( ManchesterOWLSyntax.SUB_PROPERTY_OF.toString());
        write(" ");
        axiom.getSuperProperty().accept(this);
        writeAnnotations(axiom);
    }

    public void visit(OWLDisjointClassesAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_CLASSES.toString());
        write("(");
        writeOpList(axiom.getClassExpressions(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_PROPERTIES.toString());
        write("(");
        writeOpList(axiom.getProperties(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_PROPERTIES.toString());
        write("(");
        writeOpList(axiom.getProperties(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    public void visit(OWLDifferentIndividualsAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DIFFERENT_INDIVIDUALS.toString());
        write("(");
        writeOpList(axiom.getIndividuals(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    public void visit(OWLDisjointUnionAxiom axiom) {
        writeKeyword(ManchesterOWLSyntax.DISJOINT_UNION_OF.toString());
        write("(");
        writeOpList(axiom.getClassExpressions(), ", ", false);
        write(")");
        writeAnnotations(axiom);
    }

    public void visit(OWLDeclarationAxiom axiom) {
        final OWLEntity entity = axiom.getEntity();
        if (entity instanceof OWLClass){
            writeKeyword(ManchesterOWLSyntax.CLASS.toString());
            write(": ");
        }
        else if (entity instanceof OWLObjectProperty){
            writeKeyword(ManchesterOWLSyntax.OBJECT_PROPERTY.toString());
            write(": ");
        }
        else if (entity instanceof OWLDataProperty){
            writeKeyword(ManchesterOWLSyntax.DATA_PROPERTY.toString());
            write(": ");
        }
        else if (entity instanceof OWLAnnotationProperty){
            writeKeyword(ManchesterOWLSyntax.ANNOTATION_PROPERTY.toString());
            write(": ");
        }
        else if (entity instanceof OWLNamedIndividual){
            writeKeyword(ManchesterOWLSyntax.INDIVIDUAL.toString());
            write(": ");
        }
        else if (entity instanceof OWLDatatype){
            writeKeyword("Datatype");
            write(": ");
        }
        entity.accept(this);
        writeAnnotations(axiom);
    }

    /////// OWLIndividual assertions

    public void visit(OWLClassAssertionAxiom axiom) {
        axiom.getIndividual().accept(this);
        write(": ");
        axiom.getClassExpression().accept(this);
        writeAnnotations(axiom);
    }

    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        writeAssertionAxiom(axiom);
    }

    private void writeEquivalence(Collection<? extends OWLObject> objects, OWLAxiom axiom) {
        String equiv =  ManchesterOWLSyntax.EQUIVALENT_TO.toString();
        writeKeywordOpList(objects, equiv, false);
        writeAnnotations(axiom);
    }

    private String getName(OWLEntity entity){
        return sfProvider.getShortForm(entity).replaceAll(" ", "&nbsp;");
    }

    // just make sure a named class is first if there is one
    private List<OWLClassExpression> orderOps(Set<OWLClassExpression> ops) {
        List<OWLClassExpression> orderedOps = new ArrayList<OWLClassExpression>(ops);
        Collections.sort(orderedOps, new Comparator<OWLClassExpression>(){
            public int compare(OWLClassExpression d1, OWLClassExpression d2) {
                if (d1 instanceof OWLClass){
                    return -1;
                }
                else if (d2 instanceof OWLClass){
                    return 1;
                }
                return 0;
            }
        });
        return orderedOps;
    }

    private void writeIRIWithBoldFragment(IRI iri, String shortForm) {
        final String fullURI = iri.toString();
        int index = 0;
        if (shortForm != null) {
            index = fullURI.lastIndexOf(shortForm);
        }
        if (index == 0){
            write(fullURI);
        }
        else{
            write(fullURI.substring(0, index));
            write("<b>");
            write(shortForm);
            write("</b>");
            write(fullURI.substring(index+shortForm.length()));
        }
    }

    // add a span to allow for css highlighting
    private void writeKeyword(String keyword) {
        writeKeyword(keyword, CSS_KEYWORD);
    }

    // add a span to allow for css highlighting
    private void writeKeyword(String keyword, String cssClass) {
        write("<span class='" + cssClass + "'>" + keyword + "</span>");
    }


    // useful to add brackets around the anonymous operators of unions and intersections and the fillers of restrictions
    private void writeOp(OWLObject op, boolean wrap) {
        if (op instanceof OWLEntity ||
                op instanceof OWLObjectOneOf ||
                op instanceof OWLDataOneOf ||
                op instanceof OWLDatatypeRestriction ||
                op instanceof OWLLiteral){
            op.accept(this);
        }
        else{ // provide brackets for clarity
            write("(");
            if (wrap && op instanceof OWLObjectIntersectionOf){
                indent++;
                write("<br>");
                writeIndent();
            }
            op.accept(this);
            if (wrap && op instanceof OWLObjectIntersectionOf){
                indent--;
            }
            write(")");
        }
    }

    private void writeIndent() {
        for (int i=0; i<indent; i++){
            write("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
    }


    private void writeOWLEntity(OWLEntity entity, String cssClass) {
        final URI uri = entity.getIRI().toURI();

        String name = getName(entity);

        Set<String> cssClasses = new HashSet<String>();
        cssClasses.add(cssClass);
        cssClasses.add("mansyntax");
        write("<a href=\"" + uri + "\"");
        writeCSSClasses(cssClasses);
        write(" title=\"" + uri + "\">" + name + "</a>");

    }

    private void writeAnonymousIndividual(OWLAnonymousIndividual individual) {
        write("<span class=\"anon\">");
        final Set<OWLClassExpression> types = individual.getTypes(ontologies);
        if (!types.isEmpty()){
            writeOpList(types, ", ", false);
        }

        // TODO tidy this up - we shouldn't really group by ontology
        for (OWLOntology ont : ontologies){
            Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataValues = individual.getDataPropertyValues(ont);
            if (!dataValues.isEmpty()){
                write("<ul>");
                for (OWLDataPropertyExpression p : dataValues.keySet()){
                    write("<li>");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(dataValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");
            }
            Map<OWLDataPropertyExpression, Set<OWLLiteral>> negDataValues = individual.getNegativeDataPropertyValues(ont);
            if (!negDataValues.isEmpty()){
                write("<ul>");

                for (OWLDataPropertyExpression p : negDataValues.keySet()){
                    write("<li>not ");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(negDataValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");
            }

            Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objValues = individual.getObjectPropertyValues(ont);
            if (!objValues.isEmpty()){
                write("<ul>");

                for (OWLObjectPropertyExpression p : objValues.keySet()){
                    write("<li>");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(objValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");

            }
            Map<OWLObjectPropertyExpression, Set<OWLIndividual>> negbjValues = individual.getNegativeObjectPropertyValues(ont);
            if (!negbjValues.isEmpty()){
                write("<ul>");

                for (OWLObjectPropertyExpression p : negbjValues.keySet()){
                    write("<li>not ");
                    p.accept(this);
                    write("<ul><li>");
                    writeOpList(negbjValues.get(p), "</li><li>", false);
                    write("</ul></li>");
                }
                write("</ul>");
            }
        }
        write("</span>");
    }


    private void writeCardinality(OWLCardinalityRestriction desc, String cardinalityType) {
        desc.getProperty().accept(this);
        write(" ");
        writeKeyword(cardinalityType, cardinalityType);
        write(" ");
        write(Integer.toString(desc.getCardinality()));
        if (desc.getFiller() != null){
            write(" ");
            writeOp(desc.getFiller(), true);
        }
    }

    private void writeUnaryPropertyAxiom(OWLUnaryPropertyAxiom axiom, String keyword) {
        writeKeyword(keyword);
        write(" (");
        writeOp(axiom.getProperty(), true);
        write(")");
        writeAnnotations(axiom);
    }

    private String writeFacet(OWLFacet facet) {
        // need to make ranges HTML safe
        if (facet.equals(OWLFacet.MIN_INCLUSIVE)) return "&gt;=";
        else if (facet.equals(OWLFacet.MIN_EXCLUSIVE)) return "&gt;";
        else if (facet.equals(OWLFacet.MAX_INCLUSIVE)) return "&lt;=";
        else if (facet.equals(OWLFacet.MAX_EXCLUSIVE)) return "&lt;";
        return facet.getSymbolicForm();
    }

    private void writeLiteralContents(String literal) {
        boolean writtenExternalRef = false;
        try {
            URI uri = new URI(literal);
            if (uri.isAbsolute()){
                write("<a href='" + uri + "' target='ext_ref'>" + uri + "</a>");
                writtenExternalRef = true;
            }
        }
        catch (URISyntaxException e) {
            // do nothing
        }
        finally{
            if (!writtenExternalRef){
                literal = literal.replace("<", "&lt;");
                literal = literal.replace(">", "&gt;");
                literal = literal.replace("\n", "<br />");
                write(literal);
            }
        }
    }


    private void writeAnnotations(OWLAxiom axiom) {
        final Set<OWLAnnotation> annotations = axiom.getAnnotations();
        if (!annotations.isEmpty()){
            write("<ul>");
            for (OWLAnnotation annot : annotations){
                write("<li>");
                annot.accept(this);
                write("</li>");
            }
            write("</ul>");
        }
    }

    private void writeAssertionAxiom(OWLPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        writeAnnotations(axiom);
    }


    private <O extends OWLObject> void writeOpList(Iterable<O> args, String separator, boolean wrap) {
        for (Iterator<O> i = args.iterator(); i.hasNext();) {
            i.next().accept(this);
            if (i.hasNext()){
                write(separator);
                if (wrap && indent > 0){
                    write("<br>"); // cannot use <br /> in java browser
                    writeIndent();
                }
            }
        }
    }

    private <O extends OWLObject> void writeKeywordOpList(Iterable<O> args, String keyword, boolean wrap) {
        for (Iterator<O> i = args.iterator(); i.hasNext();) {
            i.next().accept(this);
            if (i.hasNext()){
                write(" ");
                writeKeyword(keyword);
                write(" ");
                if (wrap && indent > 0){
                    write("<br>"); // cannot use <br /> in java browser
                    writeIndent();
                }
            }
        }
    }
    private void writeCSSClasses(Set<String> cssClasses) {
        if (!cssClasses.isEmpty()){
            boolean started = false;
            write(" class='");
            for (String cls : cssClasses){
                if (started){
                    write(" ");
                }
                write(cls);
                started = true;
            }
            write("'");
        }
    }
}
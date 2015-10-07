package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.model.Related;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class DocumentAssembler implements ResourceAssembler<OntologyDocument, Resource<OntologyDocument>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public Resource<OntologyDocument> toResource(OntologyDocument document) {
        Resource<OntologyDocument> resource = new Resource<OntologyDocument>(document);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OntologyController.class).getOntology(document.getOntologyId()));

        resource.add(lb.withSelfRel());

        resource.add(lb.slash("terms").withRel("terms"));
        resource.add(lb.slash("properties").withRel("properties"));
        resource.add(lb.slash("individuals").withRel("individuals"));
        return resource;
    }
}
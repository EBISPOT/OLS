package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.ols.model.OntologyDocument;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class DocumentAssembler implements RepresentationModelAssembler<OntologyDocument, EntityModel<OntologyDocument>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public EntityModel<OntologyDocument> toModel(OntologyDocument document) {
        EntityModel<OntologyDocument> resource = new EntityModel<OntologyDocument>(document);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OntologyController.class).getOntology(document.getOntologyId()));

        resource.add(lb.withSelfRel());

        resource.add(lb.slash("terms").withRel("terms"));
        resource.add(lb.slash("properties").withRel("properties"));
        resource.add(lb.slash("individuals").withRel("individuals"));
        return resource;
    }
}
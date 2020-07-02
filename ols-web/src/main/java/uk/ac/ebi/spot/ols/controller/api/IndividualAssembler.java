package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.OlsIndividual;

import java.io.UnsupportedEncodingException;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class IndividualAssembler implements RepresentationModelAssembler<OlsIndividual, EntityModel<OlsIndividual>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public EntityModel<OlsIndividual> toModel(OlsIndividual term) {
        EntityModel<OlsIndividual> resource = new EntityModel<OlsIndividual>(term);

        String id = UriUtils.encode(term.getIri(), "UTF-8");
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OntologyIndividualController.class).getIndividual(term.getOntologyName(), id));

        resource.add(lb.withSelfRel());

        resource.add(lb.slash("types").withRel("types"));
        resource.add(lb.slash("alltypes").withRel("alltypes"));
        resource.add(lb.slash("jstree").withRel("jstree"));

        return resource;
    }
}
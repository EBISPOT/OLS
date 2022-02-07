package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;

import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedIndividual;

import java.io.UnsupportedEncodingException;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class IndividualAssembler implements ResourceAssembler<LocalizedIndividual, Resource<LocalizedIndividual>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public Resource<LocalizedIndividual> toResource(LocalizedIndividual term) {
        Resource<LocalizedIndividual> resource = new Resource<LocalizedIndividual>(term);
        try {
            String id = UriUtils.encode(term.iri, "UTF-8");
            final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(OntologyIndividualController.class).getIndividual(term.ontologyName, term.lang, id));

            resource.add(lb.withSelfRel());

            resource.add(lb.slash("types").withRel("types"));
            resource.add(lb.slash("alltypes").withRel("alltypes"));
            resource.add(lb.slash("jstree").withRel("jstree"));


            // other links
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resource;
    }
}
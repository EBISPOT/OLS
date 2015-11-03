package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Individual;

import java.io.UnsupportedEncodingException;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class IndividualAssembler implements ResourceAssembler<Individual, Resource<Individual>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public Resource<Individual> toResource(Individual term) {
        Resource<Individual> resource = new Resource<Individual>(term);
        try {
            String id = UriUtils.encode(term.getIri(), "UTF-8");
            final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(OntologyIndividualController.class).getIndividual(term.getOntologyName(), id));

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
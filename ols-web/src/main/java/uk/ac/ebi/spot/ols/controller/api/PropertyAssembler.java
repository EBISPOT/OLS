package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.OlsProperty;

import java.io.UnsupportedEncodingException;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class PropertyAssembler implements RepresentationModelAssembler<OlsProperty, EntityModel<OlsProperty>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public EntityModel<OlsProperty> toModel(OlsProperty term) {
        EntityModel<OlsProperty> resource = new EntityModel<OlsProperty>(term);

            String id = UriUtils.encode(term.getIri(), "UTF-8");
            final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(OntologyPropertyController.class).getProperty(term.getOntologyName(), id));

            resource.add(lb.withSelfRel());

            if (!term.isRoot()) {
                resource.add(lb.slash("parents").withRel("parents"));
                resource.add(lb.slash("ancestors").withRel("ancestors"));
                resource.add(lb.slash("jstree").withRel("jstree"));
            }

            if (term.hasChildren()) {
                resource.add(lb.slash("children").withRel("children"));
                resource.add(lb.slash("descendants").withRel("descendants"));
            }


        return resource;
    }
}
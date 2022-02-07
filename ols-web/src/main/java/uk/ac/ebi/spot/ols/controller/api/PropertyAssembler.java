package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Property;

import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedProperty;

import java.io.UnsupportedEncodingException;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class PropertyAssembler implements ResourceAssembler<LocalizedProperty, Resource<LocalizedProperty>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public Resource<LocalizedProperty> toResource(LocalizedProperty term) {
        Resource<LocalizedProperty> resource = new Resource<LocalizedProperty>(term);
        try {
            String id = UriUtils.encode(term.iri, "UTF-8");
            final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(OntologyPropertyController.class).getProperty(term.ontologyName, term.lang, id));

            resource.add(lb.withSelfRel());

            if (!term.isRoot) {
                resource.add(lb.slash("parents").withRel("parents"));
                resource.add(lb.slash("ancestors").withRel("ancestors"));
                resource.add(lb.slash("jstree").withRel("jstree"));
            }

            if (term.hasChildren) {
                resource.add(lb.slash("children").withRel("children"));
                resource.add(lb.slash("descendants").withRel("descendants"));
            }

            // other links
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resource;
    }
}
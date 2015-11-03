package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.model.TermDocument;

import java.io.UnsupportedEncodingException;

/**
 * @author Simon Jupp
 * @date 02/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class SearchResultAssembler implements ResourceAssembler<TermDocument, Resource<TermDocument>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public Resource<TermDocument> toResource(TermDocument term) {
        Resource<TermDocument> resource = new Resource<TermDocument>(term);
        try {
            String id = UriUtils.encode(term.getUri(), "UTF-8");
            final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(OntologyTermController.class).getTerm(term.getOntologyName(), id));

            resource.add(lb.withSelfRel());

//            if (!term.isRoot()) {
//                resource.add(lb.slash("parents").withRel("parents"));
//                resource.add(lb.slash("ancestors").withRel("ancestors"));
//                resource.add(lb.slash("jstree").withRel("jstree"));
//            }
//
//            if (!term.hasChildren()) {
//                resource.add(lb.slash("children").withRel("children"));
//                resource.add(lb.slash("descendants").withRel("descendants"));
//            }


//        resource.add(lb.slash("related").withRel("related"));
            // other links
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resource;
    }
}
package uk.ac.ebi.spot.ols.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.spot.ols.neo4j.model.Related;
import uk.ac.ebi.spot.ols.neo4j.model.Term;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;

import uk.ac.ebi.spot.ols.controller.api.localization.LocalizedTerm;

/**
 * @author Henriette Harmse
 * @date 2019/06/03
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 * 
 * Henriette To do: 
 * Add parent TermAssembler class using the Template pattern with template methods dealWithParents()
 * and dealWithChildren. Add DefaultTermAssembler and PreferredRootTermAssembler classes that
 * extend the TermAssembler class.
 */
@Component
public class PreferredRootTermAssembler implements ResourceAssembler<LocalizedTerm, Resource<LocalizedTerm>> {

    @Autowired
    EntityLinks entityLinks;

    @Override
    public Resource<LocalizedTerm> toResource(LocalizedTerm term) {
        Resource<LocalizedTerm> resource = new Resource<LocalizedTerm>(term);
        try {
            String id = UriUtils.encode(term.iri, "UTF-8");
            final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                    ControllerLinkBuilder.methodOn(OntologyTermController.class).getTerm(term.ontologyName, term.lang, id));

            resource.add(lb.withSelfRel());

            if (!term.isPreferredRoot) {
                resource.add(lb.slash("parents").withRel("parents"));
                resource.add(lb.slash("ancestors").withRel("ancestors"));
                resource.add(lb.slash("hierarchicalParents").withRel("hierarchicalParents"));
                resource.add(lb.slash("hierarchicalAncestors").withRel("hierarchicalAncestors"));
                resource.add(lb.slash("jstree").withRel("jstree"));
            }

            if (term.hasChildren) {
                resource.add(lb.slash("children").withRel("children"));
                resource.add(lb.slash("descendants").withRel("descendants"));
                resource.add(lb.slash("hierarchicalChildren").withRel("hierarchicalChildren"));
                resource.add(lb.slash("hierarchicalDescendants").withRel("hierarchicalDescendants"));
            }

            resource.add(lb.slash("graph").withRel("graph"));

            Collection<String> relation = new HashSet<>();
            for (Related related : term.related) {
                if (!relation.contains(related.getLabel())) {
                    String relationId = UriUtils.encode(related.getUri(), "UTF-8");

                    resource.add(lb.slash(relationId).withRel(related.getLabel().replaceAll(" ", "_")));
                }
                relation.add(related.getLabel());
            }

//        resource.add(lb.slash("related").withRel("related"));
            // other links
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resource;
    }
}
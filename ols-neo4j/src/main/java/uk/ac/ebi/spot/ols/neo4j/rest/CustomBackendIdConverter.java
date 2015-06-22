package uk.ac.ebi.spot.ols.neo4j.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.spi.BackendIdConverter;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.repository.OntologyTermRepository;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Simon Jupp
 * @date 16/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class CustomBackendIdConverter implements BackendIdConverter {

    @Autowired
    OntologyTermRepository ontologyTermRepository;

    @Override
    public Serializable fromRequestId(String id, java.lang.Class entityType) {
//        if(entityType.equals(Term.class)) {
//            try {
//                Term node = ontologyTermRepository.findBySchemaPropertyValue("olsId", URLDecoder.decode(id, "utf-8"));
//                return node.getId();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }

        return id;
    }

    @Override
    public String toRequestId(Serializable id, java.lang.Class entityType) {
//        if(entityType.equals(Term.class)) {
//            Term c = ontologyTermRepository.findOne(Long.decode(id.toString()));
//
//            try {
//                String uri = c.getOntologyName().toLowerCase() + ":" + c.getIri();
//                return URLEncoder.encode(uri, "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
        return id.toString();

    }

    @Override
    public boolean supports(java.lang.Class delimiter) {
        return true;
    }
}
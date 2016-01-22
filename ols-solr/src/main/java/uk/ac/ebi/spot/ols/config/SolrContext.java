package uk.ac.ebi.spot.ols.config;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;
import org.springframework.data.solr.server.SolrServerFactory;
import org.springframework.data.solr.server.support.MulticoreSolrServerFactory;
import uk.ac.ebi.spot.ols.indexer.OntologySolrRepository;
import uk.ac.ebi.spot.ols.indexer.OntologySuggestRepository;
import uk.ac.ebi.spot.ols.model.SuggestDocument;

import javax.annotation.Resource;

/**
 * @author Simon Jupp
 * @date 21/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class SolrContext {

  @Resource
  private Environment environment;

  // Factory creates SolrServer instances for base url when requesting server
  // for specific core.
  @Bean
  public SolrServerFactory solrServerFactory() {
    return new MulticoreSolrServerFactory(new HttpSolrServer("localhost"));
  }

  // SolrTemplate for /solrServerUrl/ontology
  @Bean
  public SolrTemplate ontologyTemplate() throws Exception {
    SolrTemplate solrTemplate = new SolrTemplate(solrServerFactory());
    solrTemplate.setSolrCore("ontology");
    return solrTemplate;
  }

  // SolrTemplate for /solrServerUrl/autosuggest
  @Bean
  public SolrTemplate autosuggestTemplate() throws Exception {
    SolrTemplate solrTemplate = new SolrTemplate(solrServerFactory());
    solrTemplate.setSolrCore("autosuggest");
    return solrTemplate;
  }

  @Bean
  public OntologySolrRepository ontologySolrRepository() throws Exception {
    return new SolrRepositoryFactory(ontologyTemplate())
      .getRepository(OntologySolrRepository.class);
  }

  @Bean
  public OntologySuggestRepository townRepository() throws Exception {
    return new SolrRepositoryFactory(autosuggestTemplate())
      .getRepository(OntologySuggestRepository.class);
  }
}
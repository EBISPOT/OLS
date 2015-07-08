package uk.ac.ebi.spot.ols.controller.api;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.config.SearchConfiguration;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 02/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
public class SearchController {

    @Autowired
    private SearchConfiguration searchConfiguration;

    @Autowired
    private SolrTemplate solrTemplate;


    @RequestMapping(path = "/api/select", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public void select(
            @RequestParam("q") String query,
            @RequestParam(value = "ontology", required = false) Collection<String> ontologies,
            @RequestParam(value= "slim", required = false) Collection<String> slims,
            @RequestParam(value = "fieldList", required = false) Collection<String> fieldList,
            @RequestParam(value = "obsoletes", defaultValue = "false") boolean queryObsoletes,
            @RequestParam(value = "local", defaultValue = "false") boolean isLocal,
            @RequestParam(value = "childrenOf", required = false) Collection<String> childrenOf,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            HttpServletResponse response
    ) throws IOException {


        final SolrQuery solrQuery = new SolrQuery(); // 1

        solrQuery.setQuery(query);
        solrQuery.set("defType", "edismax");
        solrQuery.set("qf", "label synonym label_autosuggest_ws label_autosuggest_e label_autosuggest synonym_autosuggest_ws synonym_autosuggest_e synonym_autosuggest shortform_autosuggest");
        solrQuery.set("bq", "is_defining_ontology:true label:\"" + query + "\"^2 synonym:\"" + query + "\"");
        solrQuery.set("wt", "json");

        if (fieldList == null) {
            fieldList = new HashSet<>();
        }

        if (fieldList.isEmpty()) {
            fieldList.add("label");
            fieldList.add("uri");
            fieldList.add("short_form");
            fieldList.add("ontology_name");
        }
        solrQuery.setFields( fieldList.toArray(new String[fieldList.size()]));

        if (ontologies != null) {
            for (String ontologyname : ontologies) {
                solrQuery.addFilterQuery("ontology_name:" + ontologyname);
            }
        }

        if (slims != null) {
            for (String slim : slims) {
                solrQuery.addFilterQuery("subset:" + slim);
            }
        }

        if (isLocal) {
            solrQuery.addFilterQuery("is_defining_ontology:true");
        }

        solrQuery.addFilterQuery("is_obsolete:" + queryObsoletes);
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        solrQuery.setHighlight(true);
        solrQuery.set("hl.fl", "synonym_autosuggest");

        StringBuilder solrSearchBuilder = buildBaseSearchRequest(solrQuery.toString());
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());


//        try {
//            final QueryResponse resp = solrTemplate.getSolrServer().query(solrQuery);
//
//
//            SolrDocumentList list =  new SolrDocumentList();
//            list=resp.getResults();
//            JSONArray jArray =new JSONArray( );
//
//            for (int i = 0; i < list.size(); i++) {
//                 JSONObject json = new JSONObject(list.get(i));
//                 jArray.put(json);
//            }
//
//            ‘
//            JSONArray jsonObject = JSONArray.fromObject(resp.getResults());
//
//            JSONObject returnObj = new JSONObject();
//            List<TermDocument> documentList = solrTemplate.convertQueryResponseToBeans(resp, TermDocument.class);
//            Page<TermDocument> documentPage =  new SolrResultPage<TermDocument>(documentList, page, resp.getResults().getNumFound(), 100f);
//            return new ResponseEntity<>( assembler.toResource(documentPage, searchResultAssembler), HttpStatus.OK);
//
//
//        } catch (SolrServerException e) {
//            e.printStackTrace();
//        }
//        throw new ResourceNotFoundException();
    }

    private void dispatchSearch(String searchString, OutputStream out) throws IOException {
        System.out.println(searchString);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(searchString);
        if (System.getProperty("http.proxyHost") != null) {
            HttpHost proxy;
            if (System.getProperty("http.proxyPort") != null) {
                proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty
                        ("http.proxyPort")));
            }
            else {
                proxy = new HttpHost(System.getProperty("http.proxyHost"));
            }
            httpGet.setConfig(RequestConfig.custom().setProxy(proxy).build());
        }

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
//            getLog().debug("Received HTTP response: " + response.getStatusLine().toString());
            org.apache.http.HttpEntity entity = response.getEntity();
            entity.writeTo(out);
            EntityUtils.consume(entity);
        }
    }

    private StringBuilder buildBaseSearchRequest(String queryPath) {
        // build base request
        StringBuilder solrSearchBuilder = new StringBuilder();
        solrSearchBuilder.append(searchConfiguration.getOlsSearchServer().toString())
                .append("/select?")
                .append(queryPath);
        return solrSearchBuilder;
    }

}

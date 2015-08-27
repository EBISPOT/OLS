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
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Simon Jupp
 * @date 02/07/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
public class SearchController {

    private static String COLON = ":";
    private static String QUOTUE = "\"";
    private static String SPACE = " ";
    private static String OR = "OR";

    @Autowired
    private SearchConfiguration searchConfiguration;
//
//    @Autowired
//    private SolrTemplate solrTemplate;

    @RequestMapping(path = "/api/search", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public void search(
            @RequestParam("q") String query,
            @RequestParam(value = "ontology", required = false) Collection<String> ontologies,
            @RequestParam(value = "type", required = false) Collection<String> types,
            @RequestParam(value= "slim", required = false) Collection<String> slims,
            @RequestParam(value = "fieldList", required = false) Collection<String> fieldList,
            @RequestParam(value = "queryFields", required = false) Collection<String> queryFields,
            @RequestParam(value = "exact", required = false) boolean exact,
            @RequestParam(value = "groupField", required = false) String groupField,
            @RequestParam(value = "obsoletes", defaultValue = "false") boolean queryObsoletes,
            @RequestParam(value = "local", defaultValue = "false") boolean isLocal,
            @RequestParam(value = "childrenOf", required = false) Collection<String> childrenOf,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            HttpServletResponse response
    ) throws IOException {

        final SolrQuery solrQuery = new SolrQuery(); // 1

        solrQuery.set("wt", "json");

        if (queryFields == null) {
            // if exact just search the supplied fields for exact matches
            if (exact) {
                solrQuery.setQuery(
                        "((" +
                        createUnionQuery(query, "label_s", "synonym_s", "shortform_s", "obo_id_s", "iri_s", "annotations_s")
                        + ") AND (is_defining_ontology:true^100 OR is_defining_ontology:false^0))"
                );

            }
            else {
                solrQuery.set("defType", "edismax");
                solrQuery.setQuery(query);
                solrQuery.set("qf", "label^5 synonym^3 description short_form^2 obo_id^2 annotations logical_description iri");
                solrQuery.set("bq", "is_defining_ontology:true^100 label_s:\"" + query + "\"^5 synonym_s:\"" + query + "\"^3 annotations_s:\"" + query + "\"");
            }
        }
        else {
            if (exact) {
                solrQuery.setQuery( createUnionQuery(query, queryFields.toArray(new String [queryFields.size()])));
            }
            else {
                solrQuery.set("defType", "edismax");
                solrQuery.setQuery(query);
                solrQuery.set("qf", String.join(" ", queryFields));
            }
        }

        if (fieldList == null) {
            fieldList = new HashSet<>();
            fieldList.add("id");
            fieldList.add("iri");
            fieldList.add("label");
            fieldList.add("short_form");
            fieldList.add("obo_id");
            fieldList.add("ontology_name");
            fieldList.add("ontology_prefix");
            fieldList.add("description");
            fieldList.add("type");
        }
        solrQuery.setFields( fieldList.toArray(new String[fieldList.size()]));

        if (ontologies != null) {
            solrQuery.addFilterQuery("ontology_name: (" + String.join(" OR ", ontologies) + ")");
        }

        if (slims != null) {
            solrQuery.addFilterQuery("subset: (" + String.join(" OR ", slims) + ")");
        }

        if (isLocal) {
            solrQuery.addFilterQuery("is_defining_ontology:true");
        }

        if (types != null) {
            solrQuery.addFilterQuery("type: (" + String.join(" OR ", types) + ")");
        }

        if (groupField != null) {
            solrQuery.addFilterQuery("{!collapse field=iri}");
            solrQuery.add("expand=true", "true");
            solrQuery.add("expand.rows", "100");

        }

        solrQuery.addFilterQuery("is_obsolete:" + queryObsoletes);
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        solrQuery.setHighlight(true);
        solrQuery.add("hl.simple.pre", "<b>");
        solrQuery.add("hl.simple.post", "</b>");
        solrQuery.addHighlightField("label");
        solrQuery.addHighlightField("synonym");
        solrQuery.addHighlightField("definition");

        solrQuery.addFacetField("ontology_name", "ontology_prefix", "type", "subset", "is_defining_ontology", "is_obsolete");

        StringBuilder solrSearchBuilder = buildBaseSearchRequest(solrQuery.toString());
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }


    private String createUnionQuery (String query, String ... fields) {
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x< fields.length; x++) {
            builder.append(fields[x]);
            builder.append(COLON);
            builder.append(QUOTUE);
            builder.append(query);
            builder.append(QUOTUE);
            builder.append(SPACE);

            if (x+1 < fields.length) {
                builder.append(OR);
                builder.append(SPACE);

            }
        }
        return builder.toString();
    }

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
        solrQuery.set("bq", "is_defining_ontology:true^100.0 label_s:\"" + query + "\"^2 synonym_s:\"" + query + "\"");
        solrQuery.set("wt", "json");

        if (fieldList == null) {
            fieldList = new HashSet<>();
        }

        if (fieldList.isEmpty()) {
            fieldList.add("label");
            fieldList.add("iri");
            fieldList.add("id");
            fieldList.add("type");
            fieldList.add("short_form");
            fieldList.add("ontology_name");
            fieldList.add("ontology_prefix");
        }
        solrQuery.setFields( fieldList.toArray(new String[fieldList.size()]));

        if (ontologies != null) {
            solrQuery.addFilterQuery("ontology_name: (" + String.join(" OR ", ontologies) + ")");
        }

        if (slims != null) {
            solrQuery.addFilterQuery("subset: (" + String.join(" OR ", slims) + ")");
        }

        if (isLocal) {
            solrQuery.addFilterQuery("is_defining_ontology:true");
        }


        solrQuery.addFilterQuery("is_obsolete:" + queryObsoletes);
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        solrQuery.setHighlight(true);
        solrQuery.add("hl.simple.pre", "<b>");
        solrQuery.add("hl.simple.post", "</b>");
        solrQuery.addHighlightField("label_autosuggest");
        solrQuery.addHighlightField("label");
        solrQuery.addHighlightField("synonym_autosuggest");
        solrQuery.addHighlightField("synonym");

        StringBuilder solrSearchBuilder = buildBaseSearchRequest(solrQuery.toString());
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());

    }

    private void dispatchSearch(String searchString, OutputStream out) throws IOException {
//        System.out.println(searchString);

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
        try {
            solrSearchBuilder.append(searchConfiguration.getOlsSearchServer().toString())
                    .append("/select?")
                    .append(queryPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can't search solr server, malformed URL", e);
        }
        return solrSearchBuilder;
    }

}

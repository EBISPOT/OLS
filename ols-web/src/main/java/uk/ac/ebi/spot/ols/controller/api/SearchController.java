package uk.ac.ebi.spot.ols.controller.api;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
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
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;

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
    private static String AND = "AND";

    @Autowired
    private SearchConfiguration searchConfiguration;
//
//    @Autowired
//    private SolrTemplate solrTemplate;

    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(SearchController.class).withRel("search"));
        return resource;
    }

    @RequestMapping(path = "/api/multiSearch", method = RequestMethod.GET)
    public void multiSearch(
            @RequestParam("q") Collection<String> query,
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
            @RequestParam(value = "allChildrenOf", required = false) Collection<String> allChildrenOf,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            @RequestParam(value = "format", defaultValue = "json") String format,
            HttpServletResponse response
    ) throws IOException {

    }

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
            @RequestParam(value = "allChildrenOf", required = false) Collection<String> allChildrenOf,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            @RequestParam(value = "format", defaultValue = "json") String format,
            HttpServletResponse response
    ) throws IOException {

        final SolrQuery solrQuery = new SolrQuery(); // 1


        if (queryFields == null) {
            // if exact just search the supplied fields for exact matches
            if (exact) {
                // todo remove shortform_s once indexes have rebuilt - see https://helpdesk.ebi.ac.uk/Ticket/Display.html?id=75961
                solrQuery.setQuery(
                        "((" +
                                createUnionQuery(query.toLowerCase(), "label_s", "synonym_s", "shortform_s", "short_form_s", "obo_id_s", "iri_s", "annotations_trimmed")
                                + ") AND (is_defining_ontology:true^100 OR is_defining_ontology:false^0))"
                );

            }
            else {
                solrQuery.set("defType", "edismax");
                solrQuery.setQuery(query);
                solrQuery.set("qf", "label^5 synonym^3 description short_form^2 obo_id^2 annotations logical_description iri");
                solrQuery.set("bq", "type:ontology^10.0 is_defining_ontology:true^100 label_s:\"" + query.toLowerCase() + "\"^5 synonym_s:\"" + query.toLowerCase() + "\"^3 annotations_trimmed:\"" + query.toLowerCase() + "\"");
            }
        }
        else {
            if (exact) {
                List<String> fieldS = queryFields.stream()
                        .map(addStringField).collect(Collectors.toList());
                solrQuery.setQuery( createUnionQuery(query, fieldS.toArray(new String [fieldS.size()])));
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
            fieldList.add("is_defining_ontology");
            fieldList.add("ontology_name");
            fieldList.add("ontology_prefix");
            fieldList.add("description");
            fieldList.add("type");
        }
        solrQuery.setFields( fieldList.toArray(new String[fieldList.size()]));

        if (ontologies != null && !ontologies.isEmpty()) {
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

        if (childrenOf != null) {
            String result = childrenOf.stream()
                    .map(addQuotes)
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("ancestor_iri: (" + result + ")");
        }

        if (allChildrenOf != null) {
            String result = allChildrenOf.stream()
                    .map(addQuotes)
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("hierarchical_ancestor_iri: (" + result + ")");
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
        solrQuery.add("wt", format);

        StringBuilder solrSearchBuilder = buildBaseSearchRequest(solrQuery.toString());
    //  dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
        dispatchSearch(solrSearchBuilder.toString(), response);
    }

    Function<String,String> addQuotes = new Function<String,String>() {
        @Override public String apply(String s) {
            return new StringBuilder(s.length()+2).append('"').append(s).append('"').toString();
        }
    };

    Function<String,String> addStringField = new Function<String,String>() {
        @Override public String apply(String s) {

            // todo - need to support shortform_s for time being while https://helpdesk.ebi.ac.uk/Ticket/Display.html?id=75961 is updated
            if (s.equals("short_form")) {
                s = "shortform";
            }

            return new StringBuilder(s.length()+2).append(s).append("_").append('s').toString();
        }
    };

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

    private String createIntersectionString (String query) {
        StringBuilder builder = new StringBuilder();
        String [] tokens = query.split(" ");
        for (int x = 0; x< tokens.length; x++) {
            builder.append(tokens[x]);
            if (x+1 < tokens.length) {
                builder.append(SPACE);
                builder.append(AND);
                builder.append(SPACE);
            }
        }
        return builder.toString();
    }

    @RequestMapping(path = "/api/select", produces = {APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public void select(
            @RequestParam("q") String query,
            @RequestParam(value = "ontology", required = false) Collection<String> ontologies,
            @RequestParam(value = "type", required = false) Collection<String> types,
            @RequestParam(value= "slim", required = false) Collection<String> slims,
            @RequestParam(value = "fieldList", required = false) Collection<String> fieldList,
            @RequestParam(value = "obsoletes", defaultValue = "false") boolean queryObsoletes,
            @RequestParam(value = "local", defaultValue = "false") boolean isLocal,
            @RequestParam(value = "childrenOf", required = false) Collection<String> childrenOf,
            @RequestParam(value = "allChildrenOf", required = false) Collection<String> allChildrenOf,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            HttpServletResponse response
    ) throws IOException {


        final SolrQuery solrQuery = new SolrQuery(); // 1

        String queryLc = query.toLowerCase();
        if (query.contains(" ")) {
            query = "(" + createIntersectionString(query) +")";
        }
        solrQuery.setQuery(query);
        solrQuery.set("defType", "edismax");
        solrQuery.set("qf", "label synonym label_autosuggest_e label_autosuggest synonym_autosuggest_e synonym_autosuggest shortform_autosuggest iri");
        solrQuery.set("bq", "type:ontology^10.0 is_defining_ontology:true^100.0 label_s:\"" + queryLc + "\"^1000  label_autosuggest_e:\"" + queryLc + "\"^500 synonym_s:\"" + queryLc + "\" synonym_autosuggest_e:\"" + queryLc + "\"^100" );
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
            fieldList.add("obo_id");
            fieldList.add("ontology_name");
            fieldList.add("ontology_prefix");
        }
        solrQuery.setFields( fieldList.toArray(new String[fieldList.size()]));

        if (ontologies != null && !ontologies.isEmpty()) {
            solrQuery.addFilterQuery("ontology_name: (" + String.join(" OR ", ontologies) + ")");
        }

        if (types != null) {
            solrQuery.addFilterQuery("type: (" + String.join(" OR ", types) + ")");
        }

        if (slims != null) {
            solrQuery.addFilterQuery("subset: (" + String.join(" OR ", slims) + ")");
        }

        if (isLocal) {
            solrQuery.addFilterQuery("is_defining_ontology:true");
        }

        if (childrenOf != null) {
            String result = childrenOf.stream()
                    .map(addQuotes)
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("ancestor_iri: (" + result + ")");
        }

        if (allChildrenOf != null) {
            String result = allChildrenOf.stream()
                    .map(addQuotes)
                    .collect(Collectors.joining(" OR "));
            solrQuery.addFilterQuery("hierarchical_ancestor_iri: (" + result + ")");
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
        //dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
        dispatchSearch(solrSearchBuilder.toString(), response);
    }

    @RequestMapping(path = "/api/suggest", produces = {APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public void suggest(
            @RequestParam("q") String query,
            @RequestParam(value = "ontology", required = false) Collection<String> ontologies,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "start", defaultValue = "0") Integer start,
            HttpServletResponse response
    ) throws IOException {


        final SolrQuery solrQuery = new SolrQuery(); // 1

        String queryLc = query.toLowerCase();
        query = new StringBuilder(queryLc.length()+2).append('"').append(queryLc).append('"').toString();

        solrQuery.setQuery(query);
        solrQuery.set("defType", "edismax");
        solrQuery.set("qf", "autosuggest^3 autosuggest_e^2 autosuggest_wse^1");
        solrQuery.set("wt", "json");
        solrQuery.setFields("autosuggest");

        if (ontologies != null && !ontologies.isEmpty()) {
            solrQuery.addFilterQuery("ontology_name: (" + String.join(" OR ", ontologies) + ")");
        }

        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        solrQuery.setHighlight(true);
        solrQuery.add("hl.simple.pre", "<b>");
        solrQuery.add("hl.simple.post", "</b>");
        solrQuery.add("group", "true");
        solrQuery.add("group.field", "autosuggest");
        solrQuery.add("group.main", "true");
        solrQuery.addHighlightField("autosuggest");

        StringBuilder solrSearchBuilder = buildBaseSuggestRequest(solrQuery.toString());
        //dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());

        dispatchSearch(solrSearchBuilder.toString(), response);
    }

    private void dispatchSearch(String searchString, HttpServletResponse httpresponse) throws IOException {

        //httpresponse.setHeader(); //Do we need to put something else into the header?
        httpresponse.setContentType("application/json");
        OutputStream out=httpresponse.getOutputStream();

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

    private StringBuilder buildBaseSuggestRequest(String queryPath) {
        // build base request
        StringBuilder solrSearchBuilder = new StringBuilder();
        try {
            solrSearchBuilder.append(searchConfiguration.getOlsSuggestServer().toString())
                    .append("/select?")
                    .append(queryPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can't search solr server, malformed URL", e);
        }
        return solrSearchBuilder;
    }

}

package uk.ac.ebi.spot.ols.ui.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ols.ui.SearchConfiguration;
import uk.ac.ebi.spot.ols.ui.exception.IllegalParameterCombinationException;
import uk.ac.ebi.spot.ols.ui.service.JsonProcessingService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Javadocs go here!
 *
 * @author Julie McMurry adapted from Tony Burdett
 * @date 9 April 2015
 */
@Controller
public class SolrSearchController {
    private SearchConfiguration searchConfiguration;

    //http://localhost:8983/solr/ontology/select?q=label%3Aliver&fl=label&wt=json&indent=true

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public SolrSearchController(SearchConfiguration searchConfiguration) {
        this.searchConfiguration = searchConfiguration;
    }

    protected Logger getLog() {
        return log;
    }

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();



    @RequestMapping(value = "demo", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String showDataRuns(Model model) {
        return "platoSuggestDemo";
    }

    @RequestMapping(value = "api/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doSolrSearch(
            @RequestParam("q") String query,
//            @RequestParam(value = "jsonp", required = false, defaultValue = "false") boolean useJsonp,
//            @RequestParam(value = "callback", required = false) String callbackFunction,
//            @RequestParam(value = "max", required = false, defaultValue = "10") int maxResults,
//            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
//            @RequestParam(value = "group", required = false, defaultValue = "false") boolean useGroups,
//            @RequestParam(value = "group.by", required = false) String groupBy,
//            @RequestParam(value = "group.limit", required = false, defaultValue = "10") int groupLimit,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest("ontologySearch");

//        addFacet(solrSearchBuilder, searchConfiguration.getDefaultFacet());
//        if (useJsonp) {
//            addJsonpCallback(solrSearchBuilder, callbackFunction);
//        }
//        if (useGroups) {
//            addGrouping(solrSearchBuilder, groupBy, groupLimit);
//        }
//        else {
//            addRowsAndPage(solrSearchBuilder, maxResults, page);
//        }
        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    @RequestMapping(value = "api/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doSolrSuggest(
            @RequestParam("q") String query,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest("ontologySuggest");

        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    @RequestMapping(value = "api/select", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doSolrSelect(
            @RequestParam("q") String query,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest("ontologySelect");

        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    private StringBuilder buildBaseSearchRequest(String queryPath) {
        // build base request
        StringBuilder solrSearchBuilder = new StringBuilder();
        solrSearchBuilder.append(searchConfiguration.getOlsSearchServer().toString())
                .append("/"+queryPath+"?")
                .append("wt=json");  // even though solr returns results as json, we have to specify that the api will do the same
        return solrSearchBuilder;
    }



    private void addSortQuery(StringBuilder solrSearchBuilder, String sort) {
        solrSearchBuilder.append("&sort=").append(sort);
    }

    private void addFacet(StringBuilder solrSearchBuilder, String facet) {
        // add configuration
        solrSearchBuilder.append("&facet=true&facet.field=").append(facet);
    }

    private void addFacetMincount(StringBuilder solrSearchBuilder, int min){
        solrSearchBuilder.append("&facet.mincount=").append(min);
    }

    private void addFacetLimit(StringBuilder solrSearchBuilder, int limit){
        solrSearchBuilder.append("&facet.limit=").append(limit);
    }

    private void addFacetSort(StringBuilder solrSearchBuilder, String sort){
        solrSearchBuilder.append("&facet.sort=").append(sort);
    }

    private void addJsonpCallback(StringBuilder solrSearchBuilder, String callbackFunction) {
        if (callbackFunction == null) {
            throw new IllegalParameterCombinationException("If jsonp = true, you must specify a callback function " +
                                                                   "name with callback parameter");
        }
        else {
            solrSearchBuilder.append("&json.wrf=").append(callbackFunction);
        }
    }

    private void addGrouping(StringBuilder solrSearchBuilder, String groupBy, int maxResults) {
        solrSearchBuilder.append("&rows=10000")
                .append("&group=true")
                .append("&group.limit=").append(maxResults)
                .append("&group.field=").append(groupBy);
    }

    private void addRowsAndPage(StringBuilder solrSearchBuilder, int maxResults, int page) {
        solrSearchBuilder.append("&rows=").append(maxResults)
                .append("&start=").append((page - 1) * maxResults);
    }

    private void addFilterQuery(StringBuilder solrSearchBuilder, String filterOn, String filterBy) {
        solrSearchBuilder.append("&fq=").append(filterOn).append("%3A").append(filterBy);
    }

    private void addFilterQuery(StringBuilder solrSearchBuilder, String filterOn, String[] filterBy) {
        int counter = 0;
        String filterString = "";
        for(String filter : filterBy) {
            if(counter == 0){
                filterString = filterString.concat(filterOn).concat("%3A%22").concat(filter).concat("%22");
                counter++;
            } else{
                filterString = filterString.concat("+OR+").concat(filterOn).concat("%3A%22").concat(filter).concat("%22");
                counter++;
            }
        }
        System.out.println(filterString);
        solrSearchBuilder.append("&fq=").append(filterString);

    }

    private void addFilterQuery(StringBuilder solrSearchBuilder, String filterOn, String filterOnAlt, String filterBy) {
        solrSearchBuilder.append("&fq=").append(filterOn).append("%3A").append(filterBy).append("+OR+").append(filterOnAlt).append("%3A").append(filterBy);

    }

    private void addQuery(StringBuilder solrSearchBuilder, String query) throws IOException {
        try {
            solrSearchBuilder.append("&q=").append(URLEncoder.encode(query, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new IOException("Invalid query string - " + query, e);
        }
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
            getLog().debug("Received HTTP response: " + response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            entity.writeTo(out);
            EntityUtils.consume(entity);
        }
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    private String handleIllegalParameterCombinationException(IllegalParameterCombinationException e) {
        getLog().error("An illegal parameter combination was received", e);
        return "An illegal parameter combination was received: " + e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    private String handleIOException(IOException e) {
        getLog().error("An IOException occurred during solr search communication", e);
        return "Your search could not be performed - we encountered a problem.  Weve been notified and will attempt " +
                "to rectify the problem as soon as possible.  If problems persist, please email gwas-info@ebi.ac.uk";
    }

//    @RequestMapping(value = "api/search/downloads", produces = MediaType.TEXT_PLAIN_VALUE)
//    public @ResponseBody String getSearchResults(

//    @RequestMapping(value = "api/search/downloads")
//    public void getSearchResults(
//            @RequestParam("q") String query,
//            @RequestParam(value = "pvalfilter", required = false) String pvalRange,
//            @RequestParam(value = "orfilter", required = false) String orRange,
//            @RequestParam(value = "betafilter", required = false) String betaRange,
//            @RequestParam(value = "datefilter", required = false) String dateRange,
//            @RequestParam(value = "traitfilter[]", required = false) String[] traits,
//            @RequestParam(value = "dateaddedfilter", required = false) String addedDateRange,
//            HttpServletResponse response) throws IOException {
//
//        StringBuilder solrSearchBuilder = buildBaseSearchRequest();
//
//        int maxResults = 10000;
//        int page = 1;
//        String facet = "association";
////        addFilterQuery(solrSearchBuilder, "resourcename", facet);
//        addRowsAndPage(solrSearchBuilder, maxResults, page);
//
//
//        if (pvalRange != "") {
//            getLog().debug(pvalRange);
//            addFilterQuery(solrSearchBuilder, "pValue", pvalRange);
//        }
//        /**TO DO - when we split OR and beta, modify this controller to reflect that change!!***/
//        if (orRange != "") {
//            getLog().debug(orRange);
//
//            addFilterQuery(solrSearchBuilder, "orPerCopyNum", orRange);
//            addFilterQuery(solrSearchBuilder, "orType", "true");
//        }
//        if (betaRange != "") {
//            getLog().debug(betaRange);
//
//            addFilterQuery(solrSearchBuilder, "orPerCopyNum", betaRange);
//            addFilterQuery(solrSearchBuilder, "orType", "false");
//        }
//        if (dateRange != "") {
//            getLog().debug(dateRange);
//            addFilterQuery(solrSearchBuilder, "publicationDate", dateRange);
//        }
//        if (traits != null && traits.length != 0) {
//            System.out.println(String.valueOf(traits));
//
//            addFilterQuery(solrSearchBuilder, "traitName_s", traits);
//        }
//        if(addedDateRange != ""){
//            getLog().debug(addedDateRange);
//            addFilterQuery(solrSearchBuilder, "catalogAddedDate", addedDateRange);
//
//        }
//
//        addQuery(solrSearchBuilder, query);
//
//        String searchString = solrSearchBuilder.toString();
//
//        /*this step is necessary as something about calling the URL directly rather than through $.getJSON messes
//        up the URL encoding but explicitly URL encoding causes other interference
//        */
//        searchString = searchString.replace(" ", "+");
//
//        // dispatch search
////        return dispatchSearch(searchString);
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = new Date();
//        String now = dateFormat.format(date);
//        String fileName;
//
//        if(query.contains("*")){
//            if(addedDateRange != "") {
//                fileName = "gwas-downloaded_".concat(now).concat("-recentStudies.tsv");
//            }
//            else{
//                fileName = "gwas-downloaded_".concat(now).concat("-selectedTraits.tsv");
//            }
//        }
//        else{
//            fileName = "gwas-downloaded_".concat(now).concat("-").concat(query.substring(6, query.length() - 1)).concat(".tsv");
//        }
//        response.setContentType("text/tsv");
//        response.setHeader("Content-Disposition", "attachement; filename=" + fileName);
//
//        dispatchDownloadSearch(searchString, response.getOutputStream());
//
//
//    }

//
//    private void dispatchDownloadSearch(String searchString, OutputStream outputStream) throws IOException {
//        System.out.println(searchString);
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpGet httpGet = new HttpGet(searchString);
//        if (System.getProperty("http.proxyHost") != null) {
//            HttpHost proxy;
//            if (System.getProperty("http.proxyPort") != null) {
//                proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty
//                        ("http.proxyPort")));
//            }
//            else {
//                proxy = new HttpHost(System.getProperty("http.proxyHost"));
//            }
//            httpGet.setConfig(RequestConfig.custom().setProxy(proxy).build());
//        }
//
//        String file = null;
//        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
//            getLog().debug("Received HTTP response: " + response.getStatusLine().toString());
//            HttpEntity entity = response.getEntity();
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
//
//            String output;
//            while ((output = br.readLine()) != null) {
//
//                JsonProcessingService jsonProcessor = new JsonProcessingService(output);
//                file = jsonProcessor.processJson();
//
//            }
//
//            EntityUtils.consume(entity);
//        }
//        if(file == null){
//
//            //TO DO throw exception here and add error handler
//            file = "Some error occurred during your request. Please try again or contact the GWAS Catalog team for assistance";
//        }
//
//        InputStream in = new ByteArrayInputStream(file.getBytes("UTF-8"));
//
//        byte[] outputByte = new byte[4096];
////copy binary contect to output stream
//        while(in.read(outputByte, 0, 4096) != -1)
//        {
//            outputStream.write(outputByte, 0, 4096);
//        }
//        in.close();
//        outputStream.flush();
//
//    }

}

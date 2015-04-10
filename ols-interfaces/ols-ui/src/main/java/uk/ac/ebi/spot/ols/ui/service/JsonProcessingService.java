package uk.ac.ebi.spot.ols.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dwelter on 24/02/15.
 */
public class JsonProcessingService {

    private String json;

    public JsonProcessingService(String json){
         this.json = json;
    }


    public String processJson() throws IOException {

        String header = "Date Added to Catalog\tPUBMEDID\tFirst Author\tDate\tJournal\tLink\tStudy\tDisease/Trait\tInitial Sample Size\tReplication Sample Size\tRegion\tChr_id\tChr_pos\tReported Gene(s)\tMapped_gene\tUpstream_gene_id\tDownstream_gene_id\tSnp_gene_ids\tUpstream_gene_distance\tDownstream_gene_distance\tStrongest SNP-Risk Allele\tSNPs\tMerged\tSnp_id_current\tContext\tIntergenic\tRisk Allele Frequency\tp-Value\tPvalue_mlog\tp-Value (text)\tOR or beta\t95% CI (text)\tPlatform [SNPs passing QC]\tCNV\r\n";

        StringBuilder result = new StringBuilder();
        result.append(header);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        JsonNode responseNode = node.get("response");
        JsonNode docs = responseNode.get("docs");

        for(JsonNode doc : docs) {
            StringBuilder line = new StringBuilder();

            line.append(getDate(doc));
            line.append("\t");

            String pubmedid = getPubmedId(doc);
            line.append(pubmedid);
            line.append("\t");
            line.append(getAuthor(doc));
            line.append("\t");
            line.append(getPublicationDate(doc));
            line.append("\t");
            line.append(getJournal(doc));
            line.append("\t");

            String publink = "www.ncbi.nlm.nih.gov/pubmed/".concat(pubmedid);

            line.append(publink);
            line.append("\t");

            line.append(getTitle(doc));
            line.append("\t");

            line.append(getTrait(doc));
            line.append("\t");

            line.append(getInitSample(doc));
            line.append("\t");
            line.append(getRepSample(doc));
            line.append("\t");

            line.append(getRegion(doc));
            line.append("\t");
            line.append(getChromName(doc));
            line.append("\t");
            line.append(getChromPos(doc));
            line.append("\t");

            line.append(getRepGene(doc));
            line.append("\t");
            line.append(getMapGene(doc));
            line.append("\t");

            Map<String, String> geneIds = getGeneIds(doc);

            line.append(geneIds.get("upstream"));
            line.append("\t");
            line.append(geneIds.get("downstream"));
            line.append("\t");
            line.append(geneIds.get("ingene"));
            line.append("\t");

            //            line.append(doc.get("upstreamDistance").asText().trim());
            line.append("");
            line.append("\t");
            //            line.append(doc.get("downstreamDistance").asText().trim());
            line.append("");
            line.append("\t");

            line.append(getStrongestAllele(doc));
            line.append("\t");

            String rsId = getRsId(doc);
            line.append(rsId);
            line.append("\t");
            //            line.append(doc.get("merged").asText().trim());
            line.append("");
            line.append("\t");

            if(rsId.indexOf("rs") == 0 && rsId.indexOf("rs", 2) == -1) {
                line.append(rsId.substring(2));
            }
            else{
                line.append("");
            }

            line.append("\t");

            String context = getContext(doc);
            line.append(context);
            line.append("\t");

            if(context == ""){
                line.append("1");
            }
            else{
                line.append("0");
            }
            line.append("\t");

            line.append(getRiskFreq(doc));
            line.append("\t");

            double pvalue = getPvalue(doc);

            if(pvalue == -10){
                line.append("");
                line.append("\t");
                line.append("");
            }
            else {
                line.append(pvalue);
                line.append("\t");
                double mlog = Math.log10(pvalue);
                line.append(-mlog);
            }
            line.append("\t");
            
            line.append(getQualifier(doc));
            line.append("\t");
            line.append(getOR(doc));
            line.append("\t");
            line.append(getCI(doc));
            line.append("\t");
            //            line.append(doc.get("platform").asText().trim());
            line.append("");
            line.append("\t");
            //            line.append(doc.get("cnv").asText().trim());
            line.append("N");
            line.append("\r\n");

            result.append(line.toString());

        }
        //        return "Hello world. Your search results are coming soon to a browser near you.";
        return result.toString();
    }

    private String getCI(JsonNode doc) {
        String ci;
        if(doc.get("orPerCopyRange") != null) {
            ci = doc.get("orPerCopyRange").asText().trim();
        }
        else{
           ci = "";
        }
        return ci;
    }

    private String getOR(JsonNode doc) {
        String or;
        if(doc.get("orPerCopyNum") != null) {
            or = doc.get("orPerCopyNum").asText().trim();
        }
        else{
          or  = "";
        }
        return or;
    }

    private String getQualifier(JsonNode doc) {
        String qualifier;
        if(doc.get("qualifier") != null) {
            qualifier = doc.get("qualifier").get(0).asText().trim();
        }
        else{
           qualifier = "";
        }  
        return qualifier;
    }

    private double getPvalue(JsonNode doc) {
        double pvalue;
        if(doc.get("pValue") != null){
            pvalue = doc.get("pValue").asDouble();
        }
        else{
           pvalue = -10;
        }
        return pvalue;
    }

    private String getRiskFreq(JsonNode doc) {
        String riskFreq;
        if(doc.get("riskFrequency") != null) {
            riskFreq = doc.get("riskFrequency").asText().trim();
        }
        else{
           riskFreq = "";
        }
        return riskFreq;
    }

    private String getContext(JsonNode doc) {
        String context;
        if(doc.get("context") != null) {
            context = doc.get("context").get(0).asText().trim();
           
        }
        else{
            context = "";
        }
        return context;
    }

    private String getRsId(JsonNode doc) {
        String rsId;
        if(doc.get("rsId") != null) {
           rsId = doc.get("rsId").get(0).asText().trim();
        }
        else{
           rsId = "";
        }
        return rsId;
    }

    private String getStrongestAllele(JsonNode doc) {
        String strongestAllele;
        if(doc.get("strongestAllele") != null) {
            strongestAllele = doc.get("strongestAllele").get(0).asText().trim();
        }
        else{
           strongestAllele = "";
        }
        return strongestAllele;
    }

    private String getMapGene(JsonNode doc) {
        String gene;
        if(doc.get("mappedGene") != null) {
            gene = doc.get("mappedGene").get(0).asText().trim();
        }
        else{
           gene = "";
        }
        return gene;
    }

    private String getRepGene(JsonNode doc) {
        String genes = "";
        if(doc.get("reportedGene") != null){
            int it = 0;
            for(JsonNode gene : doc.get("reportedGene")){
                if(it > 0){
                    genes = genes.concat(", ");
                }
                genes = genes.concat(gene.asText().trim());
                it++;
            }
        }
        return genes;
    }

    private String getChromPos(JsonNode doc) {
        String chromPos;
        if(doc.get("chromosomePosition") != null) {
           chromPos = doc.get("chromosomePosition").get(0).asText().trim();
        }
        else{
           chromPos = "";
        }
        return chromPos;
    }

    private String getChromName(JsonNode doc) {
        String chromName;
        if(doc.get("chromosomeName") != null) {
            chromName = doc.get("chromosomeName").get(0).asText().trim();
        }
        else{
            chromName = "";
        }
        return chromName;
    }

    private String getRegion(JsonNode doc) {
        String region;
        if(doc.get("region") != null) {
            region = doc.get("region").get(0).asText().trim();
        }
        else{
            region = "";
        }
        return region;
    }

    private String getRepSample(JsonNode doc) {
        String sampleDesc;
        if(doc.get("replicateSampleDescription") != null){
            sampleDesc = doc.get("replicateSampleDescription").asText().trim();
        }
        else{
           sampleDesc = "";
        }
        return sampleDesc;
    }

    private String getInitSample(JsonNode doc) {
        String sampleDesc;
        if(doc.get("initialSampleDescription") != null){
            sampleDesc = doc.get("initialSampleDescription").asText().trim();
        }
        else{
            sampleDesc = "";
        }
        return sampleDesc;
    }

    private String getTrait(JsonNode doc) {
        String traitName;
        if(doc.get("traitName_s") != null){
            traitName = doc.get("traitName_s").asText().trim();
        }
        else{
            traitName = "";
        }
        return traitName;
    }

    private String getTitle(JsonNode doc) {
        String title;
        if(doc.get("title") != null){
            title = doc.get("title").asText().trim();
        }
        else{
            title = "";
        }
        return title;
    }


    private String getJournal(JsonNode doc) {
        String journal;
        if(doc.get("publication") != null) {
            journal = doc.get("publication").asText().trim();
        }
        else {
            journal = "";
        }
        return journal;

    }

    private String getAuthor(JsonNode doc) {
        String author;
        if(doc.get("author_s") != null){
            author = doc.get("author_s").asText().trim();
        }
        else{
            author = "";
        }
        return author;
    }

    private String getPubmedId(JsonNode doc) {
        String pmid;
        if(doc.get("pubmedId") != null){
            pmid = doc.get("pubmedId").asText().trim();
        }
        else{
            pmid = "";
        }
        return pmid;
    }

    private String getPublicationDate(JsonNode doc) {
        String date;
        if(doc.get("publicationDate") != null){
            date= doc.get("publicationDate").asText().trim().substring(0, 10);

        }
        else{
            date= "";
        }
        return date;
    }

    private String getDate(JsonNode doc){
        String date;
        if(doc.get("catalogAddedDate") != null){
            date = doc.get("catalogAddedDate").asText().trim().substring(0,10);
        }
        else{
            date = "";
        }
        return date;
    }

    private Map<String, String> getGeneIds(JsonNode doc){
        Map<String, String> geneIds = new HashMap<String, String>();
        String upstream = "";
        String downstream ="";
        String ingene ="";

        if(doc.get("mappedGeneLinks") != null) {
            if(doc.get("mappedGeneLinks").size() == 1){
                String geneLink = doc.get("mappedGeneLinks").get(0).asText().trim();
                int index = geneLink.indexOf("|");
                ingene = geneLink.substring(index+1);
            }
            else {
                if (doc.get("mappedGene") != null) {
                    String mapped = doc.get("mappedGene").get(0).asText().trim();

                    for (JsonNode geneLink : doc.get("mappedGeneLinks")) {
                        int index = geneLink.asText().trim().indexOf("|");
                        String gene = geneLink.asText().trim().substring(0, index - 1);
                        String geneId = geneLink.asText().trim().substring(index + 1);

                        if (mapped.indexOf(gene) == 0) {
                            upstream = geneId;
                        }
                        else {
                            downstream = geneId;
                        }

                    }
                }
            }

        }
        geneIds.put("upstream", upstream);
        geneIds.put("downstream", downstream);
        geneIds.put("ingene", ingene);


            return geneIds;
    }
}




package uk.ac.ebi.ols.apitester;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OlsApiTester {


	Gson gson;
	String url1, url2, ontologyId;

	static final int SAMPLE_SIZE = 3;

	public OlsApiTester(String url1, String url2, String ontologyId) {

		gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

		if(url1.endsWith("/")) {
			url1 = url1.substring(0, url1.length() - 1);
		}

		if(url2.endsWith("/")) {
			url2 = url2.substring(0, url2.length() - 1);
		}

		this.url1 = url1;
		this.url2 = url2;
		this.ontologyId = ontologyId;
	}

	public boolean test() {

		System.out.println("url1: " + url1);
		System.out.println("url2: " + url2);

		try {

			check(this.ontologyId, "/api/ontologies/" + this.ontologyId);

			Pair<JsonElement, JsonElement> termsResult =
				check(this.ontologyId + "_terms", "/api/ontologies/" + this.ontologyId + "/terms?size=" + SAMPLE_SIZE);

			check(this.ontologyId + "_roots", "/api/ontologies/" + this.ontologyId + "/terms/roots?size=" + SAMPLE_SIZE);
			check(this.ontologyId + "_properties", "/api/ontologies/" + this.ontologyId + "/properties?size=" + SAMPLE_SIZE);
			check(this.ontologyId + "_propertyroots", "/api/ontologies/" + this.ontologyId + "/properties/roots?size=" + SAMPLE_SIZE);

			// Use instance1's list to iterate through the terms
			// The two instances should not differ anyway. If they do we will have already reported it.
			//
			JsonElement instance1Result = termsResult.getLeft();

			JsonArray terms = instance1Result.getAsJsonObject()
				.get("_embedded").getAsJsonObject()
				.get("terms").getAsJsonArray();
			
			for(int i = 0; i < terms.size(); ++ i) {

				JsonObject term = terms.get(i).getAsJsonObject();
				String iri = term.get("iri").getAsString();
				String shortform = term.get("short_form").getAsString();

				check(this.ontologyId + "_" + shortform,
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri));

				check(this.ontologyId + "_" + shortform + "_parents",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/parents");

				check(this.ontologyId + "_" + shortform + "_ancestors",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/ancestors");

				check(this.ontologyId + "_" + shortform + "_hierarchicalParents",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/hierarchicalParents");

				check(this.ontologyId + "_" + shortform + "_hierarchicalAncestors",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/hierarchicalAncestors");

				check(this.ontologyId + "_" + shortform + "_jstree",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/jstree");

				check(this.ontologyId + "_" + shortform + "_children",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/children");

				check(this.ontologyId + "_" + shortform + "_descendants",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/descendants");

				check(this.ontologyId + "_" + shortform + "_hierarchicalChildren",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/hierarchicalChildren");

				check(this.ontologyId + "_" + shortform + "_hierarchicalDescendants",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/hierarchicalDescendants");

				check(this.ontologyId + "_" + shortform + "_graph",
					"/api/ontologies/" + this.ontologyId + "/terms/" + doubleEncode(iri) + "/graph");
			}


		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;

	}


	public Pair<JsonElement, JsonElement> check(String filenameToSave, String path) throws MalformedURLException, IOException {

		System.out.println("Testing URL: " + path);

		Pair<JsonElement, JsonElement> results = getBoth(path);

		JsonElement result1 = results.getLeft();
		JsonElement result2 = results.getLeft();

		String result1Json = gson.toJson(result1);
		String result2Json = gson.toJson(result2);

		save(filenameToSave + "_1.json", result1Json);
		save(filenameToSave + "_2.json", result2Json);

		if(!result1Json.equals(result2Json)) {
			System.out.println("⚠️ Response did not match");
		} else {
			System.out.println("✅ Response matched");
		}

		return results;
	}


	public Pair<JsonElement, JsonElement> getBoth(String path) throws IOException, MalformedURLException {

		return new ImmutablePair<JsonElement, JsonElement>(
			get(url1, path),
			get(url2, path)
		);

	}

	public JsonElement get(String baseUrl, String path) throws IOException, MalformedURLException {

		URLConnection conn = new URL(baseUrl + path).openConnection();
		InputStream is = conn.getInputStream();
		Reader reader = new InputStreamReader(is, "UTF-8");
		JsonElement result = JsonParser.parseReader(reader);

		return deepSort(removeDates(normalizeURLs(result, baseUrl)));
	}


	public JsonElement normalizeURLs(JsonElement element, String instanceBaseUrl) {

		if(element.isJsonArray()) {

			JsonArray arr = element.getAsJsonArray();
			JsonArray res = new JsonArray();
			
			for(int i = 0; i < arr.size(); ++ i) {
				res.add(normalizeURLs(arr.get(i), instanceBaseUrl));
			}

			return res;

		} else if(element.isJsonObject()) {

			JsonObject obj = element.getAsJsonObject();
			JsonObject res = new JsonObject();

			for(Entry<String, JsonElement> entry : obj.entrySet()) {
				res.add(entry.getKey(), normalizeURLs(entry.getValue(), instanceBaseUrl));
			}

			return res;

		} else if(element.isJsonPrimitive()) {

			JsonPrimitive p = element.getAsJsonPrimitive();

			if(p.isString()) {

				String replaced = p.getAsString().replace(instanceBaseUrl, "<base>");
				return new JsonPrimitive(replaced);
			}
		} 

		return element.deepCopy();
	}
	
	public JsonElement deepSort(JsonElement element) {

		if(element.isJsonArray()) {

			JsonArray arr = element.getAsJsonArray();

			JsonElement[] elems = new JsonElement[arr.size()];

			for(int i = 0; i < arr.size(); ++ i) {
				elems[i] = deepSort(arr.get(i));
			}
			
			Arrays.sort(elems, new Comparator<JsonElement>() {

				public int compare(JsonElement a, JsonElement b) {
					return gson.toJson(a).compareTo(gson.toJson(b));
				}
			});

			JsonArray res = new JsonArray();

			for(int i = 0; i < arr.size(); ++ i) {
				res.add(elems[i]);
			}

			return res;

		} else if(element.isJsonObject()) {

			JsonObject obj = element.getAsJsonObject();

			TreeSet<String> sortedKeys = new TreeSet<String>();

			for(String key : obj.keySet()) {
				sortedKeys.add(key);
			}

			JsonObject res = new JsonObject();

			for(String key : sortedKeys) {
				res.add(key, deepSort(obj.get(key)));
			}

			return res;

		}

		return element.deepCopy();
	}

	public JsonElement removeDates(JsonElement element) {

		if(element.isJsonArray()) {

			JsonArray arr = element.getAsJsonArray();
			JsonArray res = new JsonArray();
			
			for(int i = 0; i < arr.size(); ++ i) {
				res.add(removeDates(arr.get(i)));
			}

			return res;

		} else if(element.isJsonObject()) {

			JsonObject obj = element.getAsJsonObject();
			JsonObject res = new JsonObject();

			for(Entry<String, JsonElement> entry : obj.entrySet()) {

				if(entry.getKey().equals("loaded")) {
					res.add(entry.getKey(), new JsonPrimitive("<loaded>"));
					continue;
				}

				if(entry.getKey().equals("updated")) {
					res.add(entry.getKey(), new JsonPrimitive("<updated>"));
					continue;
				}

				res.add(entry.getKey(), removeDates(entry.getValue()));
			}

			return res;

		}

		return element.deepCopy();
	}

	public void save(String filename, String content) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(filename);
		out.append(content);
		out.close();
	}

	public String removeBaseUrl(String url, String baseUrl) {

		if(!url.startsWith(baseUrl)) {
			throw new RuntimeException("url does not start with base url");
		}

		return url.substring(url.length());
	}

	public String doubleEncode(String iri) throws UnsupportedEncodingException {

		return URLEncoder.encode(URLEncoder.encode(iri, "utf-8"), "utf-8");
	}

}

package uk.ac.ebi.spot.ols.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalizedStrings {

    private Map<String, List<String>> localizations = new HashMap<>();

    public LocalizedStrings() {
    }

    public LocalizedStrings(Map<String,String> strings) {
        for(String lang : strings.keySet()) {
            addString(lang, strings.get(lang.toLowerCase()));
        }
    }

    public Set<String> getLanguages() {
        return localizations.keySet();
    }

    public Set<String> getNonEnLanguages() {
	Set<String> langs = new HashSet<>();
	for(String lang : localizations.keySet()) {
		if(lang.compareTo("en") != 0 && (!lang.startsWith("en-")) && lang.compareTo("") != 0) {
			langs.add(lang);
		}
	}
	return langs;
    }

    public List<String> getStrings(String... languages) {

	for(String lang : languages) {

		List<String> strings = localizations.get(lang.toLowerCase());

		if(strings != null) {
			return strings;
		}
	}

	List<String> defaults = localizations.get("");

	if(defaults != null) {
		return defaults;
	}

	return new ArrayList<String>();
    }

    public List<String> getDefaultStrings() {
	    return getStrings("");
    }

    public Map<String, List<String>> getStrings() {
        return localizations;
    }

    public String getFirstString(String... languages) {

	for(String lang : languages) {

		List<String> strings = localizations.get(lang.toLowerCase());

		if(strings != null && strings.size() > 0) {
			return strings.get(0);
		}
	}

	List<String> defaults = localizations.get("");

	if(defaults != null && defaults.size() > 0) {
		return defaults.get(0);
	}

        return null;
    }

    public void addString(String language, String value) {

        List<String> strings = localizations.get(language.toLowerCase());

        if(strings == null) {
            strings = new ArrayList<>();
            localizations.put(language.toLowerCase(), strings);
        }

        if(!strings.contains(value)) {
            strings.add(value);
        }
    }

    public void addDefaultString(String value) {

	addString("", value);

    }

    public int size() {
        int n = 0;
        for(List<String> values : this.localizations.values()) {
            n += values.size();
        }
        return n;
    }

    public void addAll(LocalizedStrings other) {

        for(String lang : other.getLanguages()) {
            for(String string : other.getStrings(lang)) {
                addString(lang, string);
            }
        }

    }
    
}

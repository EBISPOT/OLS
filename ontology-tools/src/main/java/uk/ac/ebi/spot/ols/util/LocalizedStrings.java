package uk.ac.ebi.spot.ols.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalizedStrings {

    private Map<String, List<String>> localizations = new HashMap<>();

    public LocalizedStrings() {
    }

    public Set<String> getLanguages() {
        return localizations.keySet();
    }

    public List<String> getStrings(String language) {
        return localizations.get(language);
    }

    public Map<String, List<String>> getStrings() {
        return localizations;
    }

    public List<String> getEnglishStrings() {
        return getStrings("en");
    }

    public String getFirstEnglishString() {
        List<String> strings = getEnglishStrings();

        if(strings.size() == 0) {
            return "";
        }

        return strings.get(0);
    }

    public void addString(String language, String value) {

        List<String> strings = localizations.get(language);

        if(strings == null) {
            strings = new ArrayList<>();
            localizations.put(language, strings);
        }

        if(!strings.contains(value)) {
            strings.add(value);
        }
    }

    public int size() {
        int n = 0;
        for(List<String> values : this.localizations.values()) {
            n += values.size();
        }
        return n;
    }

    
}

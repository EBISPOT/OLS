package uk.ac.ebi.spot.ols.ui.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.ols.model.SearchResult;

/**
 * @author Julie McMurry adapted from Dani Welter
 */

@Controller
public class SearchController {
    @RequestMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
    String search(Model model, @RequestParam(required = false) String query, @RequestParam(required = false) String filter) {
        SearchResult result = new SearchResult();
        result.setQuery(query);
        result.setFilter(filter);
        model.addAttribute("result", result);
        return "search";
    }

}

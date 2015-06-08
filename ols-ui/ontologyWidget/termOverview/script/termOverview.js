
jQuery(document).ready(function () {
    jQuery.noConflict();
    jQuery('.term-overview').injectTermOverview();


});

var element_to_inject;

//make it a jquery plugin that can be attached to any element.
(function ($) {


    //function getDataAndInject(){
    $.fn.injectTermOverview = function () {

        element_to_inject = this;

        //Get the <term-overview> tag label attribute
        //var label = $(".term-overview").attr("data-label");
        var label = this.attr("data-label");
        console.log("LABEL = " + label);
        //Get the <term-overview> tag ontology-name attribute
        //var ontology_name= $(".term-overview").attr("data-ontology-name");
        var ontology_name = this.attr("data-ontology-name");

        //Get the <term-overview> tag json-path attribute
        // The json_path the url pointing to the solr server, or the server which given a label parameter returns the expected json.
        // The label part of the url should contain the following string "LABEL_TO_REPLACE" so that the widget can easily replace
        // this with the given label.

        // If the json_path was not given use the ebi default url.
        //var json_path = $(".term-overview").attr("data-json-path")
        var json_path = this.attr("data-json-path");
        if (typeof json_path == 'undefined' || json_path.indexOf("LABEL_TO_REPLACE") == -1) {
            json_path = "http://localhost:8983/solr/ontology/select?q=label%3ALABEL_TO_REPLACE&rows=1&wt=json&indent=true";
        }
        //If the json_path does not contain a LABEL_TO_REPLACE string then send an error message.
        else if (json_path.indexOf("LABEL_TO_REPLACE") == -1) {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-overview&#62; tag, you should provide a json_path parameter which is a url' +
            'pointing to your solr server with a LABEL_TO_REPLACE string where the widget need to replace with the given label<br>' +
            'ex : &#60;term-overview  label="lung" ontology-name="EFO" json-path="http://localhost:8983/solr/ontology/select?q=label%3ALABEL_TO_REPLACE+AND+ontology_name%3AONTOLOGY_NAME_TO_REPLACE&rows=1&wt=json&indent=true"&#62;&#60;/term-overview&#62;</div>';

            //$('.term-overview').append(html_to_inject);
            this.append(html_to_inject);
        }
        //If the json_path does not contain a LABEL_TO_REPLACE string then send an error message.
        else if (json_path.indexOf("ONTOLOGY_NAME_TO_REPLACE") == -1) {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-overview&#62; tag, you should provide a json_path parameter which is a url' +
            'pointing to your solr server with a ONTOLOGY_NAME_TO_REPLACE string where the widget need to replace with the given ontology-name<br>' +
            'ex : &#60;term-overview  label="lung" ontology-name="EFO" json-path="http://localhost:8983/solr/ontology/select?q=label%3ALABEL_TO_REPLACE+AND+ontology_name%3AONTOLOGY_NAME_TO_REPLACE&rows=1&wt=json&indent=true"&#62;&#60;/term-overview&#62;</div>';

            //$('.term-overview').append(html_to_inject);
            this.append(html_to_inject);
        }
        //If the user didn't provide the label parameter then send an error message.
        else if (typeof label == 'undefined') {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-overview&#62; tag, you should at least provide a label parameter<br>' +
            'ex : &#60;term-overview label="lung"&#62;&#60;/term-overview&#62;</div>';
            //$('.term-overview').append(html_to_inject);
            this.append(html_to_inject);
        }
        else if (typeof ontology_name == 'undefined') {
            html_to_inject = '<div class="msgError">BAD USE OF THE &#60;term-overview&#62; tag, you should at least provide an ontology-name parameter<br>' +
            'ex : &#60;term-overview label="lung" ontology-name="EFO"&#62;&#60;/term-overview&#62;</div>';
            //$('.term-overview').append(html_to_inject);
            this.append(html_to_inject);
        }
        //Finally if the user has provided everything then parse the json, build the term overview and inject it in the <term-overview> tag.
        else {
            var full_path = json_path.replace("LABEL_TO_REPLACE", label);
            full_path = full_path.replace("ONTOLOGY_NAME_TO_REPLACE", ontology_name);
            //because the url contains the string json.wrf=on_data, then javascript knows that when
            // $.getJSON(full_path);
            // is called it should build the json object from the given full_path parameter and send it to the on_data function.
            full_path = full_path + "&wt=json&json.wrf=on_data&callback=?"

            $.getJSON(full_path);//,function(json){
        }
    }

}(jQuery));



//In order to avoid having the png icons in a separate file which would make the user life a bit more complicated, I include them directly below.
//png code for the minus icon
var minus_png = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAACYElEQVR4nO2ZTWtTQRSGn8k1KErSqi1Uqi3WFq3/oRW60u70PyiiCwVx5V43rsSNgkK7VRA3Ii4Kbf+CtfiBaFSkJEhJ" +
    "wa/kznFjwVXOuXduciO9D2R3zsz75pyZTGagoGBn46yB06tbETABTJUcR4GBjLX8AmpeeAesrc9UfluSdllHF+EMcBM44YVyOo2GeaAGLB5f2br1erbyXYs3VWBquTkOrDnYFyrQSEvg/NtT1QUt0FSBWHhI78QDlIErgGpArcD4UnPA" +
    "wWYWqhISCxz6OFetdwpSK+BFjmSnKRERcBJY7hSkGoh9Vnq6g16B/91AW6QXOlKjt1B/698BLRQXLdRdsthGbzvHaprJRRgFbgCjafLBZKBzCeKzB66nnRwgevLtNN00oOgPJnR81YB0cxt63IjE+yhkCP00qu1Cj+qXgZepZhcZAiZT5" +
    "f5FN6DX+G6IgFAMBvr7lyy8hXImixbKlaKF8qZoobwxtBAbkFcV3Ac1wjTO/fevgOlANUn5zIUJ9UbEdrXo5SrIU2BPqCojMbh7lsCSabiLx14gXEOkhQg9+DxH5I5FmvlyF5EHwAYi54ARYLc510YM1HFuBVjg0mTTkmS+Xv+XsWf1Ib" +
    "JvpzbQqM0Pt5MkJXkfKAHDDg4nVZYEgQbwZX2mYjJivV4vOedGHBy05gTyQ+DTm9nKTy3QtAZEGBSRKtAKlmYjAvYDX7VA6/tAL8Vvs9cSZDXggZ6fKcaWmuXaXLXjF2cy4L2YHtzywFYBT14G1EOYyUBbet7/22RjIMcWUteddRHnUoHN" +
    "+cH+/jdVAPwBrDT9OIYx++8AAAAASUVORK5CYII=";
//png code for the plus icon
var plus_png = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAuZJREFUeNrsms9PU0EQx7" +
    "elFRLTlkYM4URVkBAO5Q8QWrlwM1z8A7yYGP8Iyx9hTDyhVz30KBdpwT9ADoQg/mjVRAlEHu2pltdx9tkDeVh5u7zpTtv9JptNXvt2Zz6dmZ2XPiGsrKysBliRMBaZe1efxCnTZdsrO3cSVWMAZrc8pws4VnCRUVO/IAixJu3YXdCDoQVgZ" +
    "rOexalk0nEfBAen/N5iYpscwHS5Jn/591yc90GY388llSIhprpRC7ywZ+V8W6PtlHxAGgE3NmrAuKg7X+4m02QAJt964V9hfrJlqkvB0yCmFv6QEfwlbSQC0Oq/RkgxAiyAsFQ4WE6tnr0wvn7ypF3Fu6qoypddgFCG33kpeS2s9Xs2BUyk" +
    "mAVgAaj1AcQAwEYAawAusYEu+xQI3gkWmivpVVVjGvfSHZ9N4sVjkj4hqpqjQYaO8xdJrhl0/75thSn2VwIAYJYAxf4xRQvMef/6KCV6BsCrQ1mwSh0/v3+93OG+3H/2zhs/BRSS8KJqHemwfon1MUieAtw7QQtg4AH04cOAjQA2Br78nho" +
    "cAC++ne8TiM55rjWgIJiod1phC4AFAPnHKHcIkYrSt5XXf/75WPB8P0DKEQ9vKv09HlXeAqDopQLPUVR1RwMAVnAAh6HzjgD100XvLbFnH7O4Y4lRKjjoSl48mtqmjwApuRGIeRxr2Bs4Xn9gZjieDdIWDef1I8Cvpx+MvCgpHt++9IuSsTA" +
    "smctOHOL0q8sAGjthHJq6N85u1a/gNIELjJlMfkyEI5x+7C4kfncNwMxmfQSnKbx5iEMFRAguTl/3FhM1cgDT5Vocp1tcnPdB+LSfSzZJawAW32t/9xOnDDtBadtPagAjTJ2Xukp+CiAAYAxAvs0ary4FTwMNAMDWeZ26pg6gJbgDAOoUaA4" +
    "0ABcuHwEYo42D5VTj7LXx9ZNhtHy4ByIglBpwLookkLE3zlAvpMAplZGUa1sAVlZW/9IfAQYAcEOjn2AeGLwAAAAASUVORK5CYII=";



function on_data(json) {


    //Build the first row of the table holding the term label
    var local_label = json.response.docs[0].label;

    html_to_inject = "<div class=\"tbl_container\">"
    + "<div role=\"main\">"
    + "<table class=\"mCustomScrollbar\" data-mcs-theme=\"dark\">"
    + "<thead>"
    + "<tr>"
    + "<th scope=\"row\">"
    + local_label
    + "<a class=\"hideAll\"><img width=\"20\" height=\"20\" title=\"\" alt=\"\" src=\"" + minus_png + "\"></a>"
    + "<a class=\"showAll\"><img width=\"20\" height=\"20\" title=\"\" alt=\"\" src=\"" + plus_png + "\"></a>"
    + "</th>"
    + "<td>"
    + "<tr>"
    + "</thead>"
    + "<tbody>";

    //Build the second row holding the term description
    if (json.response.docs[0].description) {
        html_to_inject = html_to_inject + build_html("description", json.response.docs[0].description);
    }
    //Build the third row holding the term external_definition_annotation
    if (json.response.docs[0].external_definition_annotation) {
        html_to_inject = html_to_inject + build_html("external_definition_annotation", json.response.docs[0].external_definition_annotation);
    }
    //Build all the other row of the table filling with the field from the json file in the order they come (do not re-add label, description or external_definition_annotation)
    jQuery.each(json.response.docs[0], function (key, val) {
        if (key != "label" && key != "description" && key != "external_definition_annotation") {
            html_to_inject = html_to_inject + build_html(key, val);
        }
    });

    //Close the table
    html_to_inject = html_to_inject
    + "</tbody>"
    + "</table>"
    + "</div>"

    + "</div>";

    //Inject the html in the <term-overview> tag
    //this.append(html_to_inject);
    element_to_inject.append(html_to_inject);
    //jQuery(".term-overview").append(html_to_inject);

    //attach the on click hide behaviour to all the tag of class .hide.
    //For example, the tag <a target="description" class="hide"> will be listening for any click event because it is
    //of class "hide". If the click event occurs then all the tag with id equal to "description" will be hidden.
    jQuery(".hide").click(function () {
        $('#' + $(this).attr('target')).hide();
    });
    //attach the on click show behaviour to all the tag of class .show.
    //For example, the tag <a target="description" class="show"> will be listening for any click event because it is
    //of class "show". If the click event occurs then all the tag with id equal to "description" will be shown.
    jQuery(".show").click(function () {
        $('#' + $(this).attr('target')).show();
    });

    //attach an on click behaviour to all the tag of class .hideAll.
    //For example, the tag <a class="showAll"> will be listening for any click event. If a click event occurs it will
    //hide all the element of class .hideOrShow (ex : <description id="description" class="hideOrShow>hello</description>)
    jQuery(".hideAll").click(function () {
        $('.hideOrShow').hide();
    });

    //attach an on click behaviour to all the tag of class .showAll.
    //For example, the tag <a class="showAll"> will be listening for any click event. If a click event occurs it will
    //show all the elements of class .hideOrShow (ex : <description id="description" class="hideOrShow>hello</description>)
    jQuery(".showAll").click(function () {
        $('.hideOrShow').show();
    });

    //By default when first loading the page we want to make the table as small as possible so all the tag of class hideOrShow
    // are hidden.
    jQuery('.hideOrShow').hide();

}




//Given a key (ex : synonym) and a value (ex : pulmo), it builds the html for an additional row for the term overview table and returns it.
function build_html(key, val) {
    //By default we only display the x first letters of any of the field in the table, if the user wants to see the entire field he can click on the plus png image
    //If the user wants to hide it again he can click on the minus png image. The length of the string displayed by default is hard-coded in the show_length parameter
    //below.
    var show_length = 150;
    val = val.toString();
    val = val.toString();
    val = val.replace(/,/g, ', ');

    //In the first cell of the row, add the key as the row title
    var local_html_to_inject = "<tr>"
        + "<th scope=\"row\">" + key + "</th>";

    //In the second cell of the row add the value.
    //If the value length is shorter then show_length then just display it all.
    if (val.length < show_length) {
        local_html_to_inject = local_html_to_inject
        + "<td>"
        + val
        + "</td>"
        + "</tr>";
        //If the value length is longer then show_length then put the first part of the string normally inside the cell but
        // but add the end of the string in a tag  of class "hideOrShow" that can be hidden or shown later.
    } else {
        local_html_to_inject = local_html_to_inject
        + "<td>"
        + val.substring(0, show_length)
        + "<" + key + " id=\"" + key + "\" class=\"hideOrShow\">"
        + val.substring(show_length, val.length)
        + "</" + key + ">"
        + "<a target=\"" + key + "\" class=\"hide\"><img width=\"20\" height=\"20\" title=\"\" alt=\"\" src=\"" + minus_png + "\"></a>"
        + "<a target=\"" + key + "\" class=\"show\"><img width=\"20\" height=\"20\" title=\"\" alt=\"\" src=\"" + plus_png + "\"></a>"
        + "</td>"
        + "</tr>";
    }

    return local_html_to_inject;
}


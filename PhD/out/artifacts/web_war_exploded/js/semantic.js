/**
 * Created by samikanza on 23/04/2017.
 */

var currDoc = "document-0";
var currSideTab = "doc_tab";
var showDocList = true;
var showGate = true;
var showOsc4r = true;
var showOntology = true;
var showChemTagger = true;
var document_array;

$(document).ready(function(){

    document_array = $("li.doc_item");

    $('.doc_item').click(function(){
        $(this).addClass("active");
        $(this).siblings().removeClass("active");
        currDoc = $(this).attr("id");
        switchTab();
    });

    /*** ADD COLOUR CLASSES FOR TOOLTIPS ***/
    $('.chemTagger-tooltip').each(function(){
        $(this).addClass('chemTagger-tooltip-color');
    })

    $('.gate-tooltip').each(function(){
        $(this).addClass('gate-tooltip-color');
    })

    $('.ontology-tooltip').each(function(){
        $(this).addClass('ontology-tooltip-color');
    })

    $('.osc4r-tooltip').each(function(){
        $(this).addClass('osc4r-tooltip-color');
    })

    $('.tooltip-text').each(function(){
        $(this).addClass('osc4r-tooltip-color');
    })



    /*** TOOLTIP HOVERS ***/
    $('.chemTagger-tooltip').mouseleave(function(event){
        hideHover(this);
    });

    $('.chemTagger-tooltip').hover(function(){
        showHover(showChemTagger,this);
    });

    $('.chemTagger-tooltip').mouseleave(function(event){
        hideHover(this);
    });

    $('.osc4r-tooltip').hover(function(){
        showHover(showOsc4r,this);
    });

    $('.osc4r-tooltip').mouseleave(function(event){
        hideHover(this);
    });

    $('.gate-tooltip').hover(function(){
        showHover(showGate,this);
    });

    $('.gate-tooltip').mouseleave(function(event){
        hideHover(this);
    });

    $('.ontology-tooltip').hover(function(){
        showHover(showOntology,this);
    });

    $('.ontology-tooltip').mouseleave(function(event){
        hideHover(this);
    });

    //if same class and doesnt have more than one child
    function showHover(tooltip,hover){
        if (tooltip == true) {
            $(hover).children('.tooltiptext').css('visibility','visible');
            $(hover).siblings('span.tooltiptext').addClass('superHidden');
        }
    }

    function hideHover(hover){
        $(hover).children('.tooltiptext').css('visibility','hidden');
        $(hover).siblings('span.tooltiptext').removeClass('superHidden');
    }

    /*** CHECKBOXES FOR MARKUP ***/
    $('.cbx-markup-osc4r').change(function() {
        var tooltip_class = '.osc4r-tooltip';
        var tooltip_color = 'osc4r-tooltip-color';
        if(this.checked) {
            turnOnTooltip(tooltip_class,tooltip_color);
            showOsc4r = true;
        }else{
            turnOffTooltip(tooltip_class,tooltip_color);
            showOsc4r = false;
        }
    });

    $('.cbx-markup-gate').change(function() {
        var tooltip_class = '.gate-tooltip';
        var tooltip_color = 'gate-tooltip-color';
        if(this.checked) {
            turnOnTooltip(tooltip_class, tooltip_color);
            showGate = true;
        }else{
            turnOffTooltip(tooltip_class,tooltip_color);
            showGate = false;
        }
    });

    $('.cbx-markup-ontology').change(function() {
        var tooltip_class = '.ontology-tooltip';
        var tooltip_color = 'ontology-tooltip-color';
        if(this.checked) {
            turnOnTooltip(tooltip_class, tooltip_color);
            showOntology = true;
        }else{
            turnOffTooltip(tooltip_class,tooltip_color);
            showOntology = false;
        }
    });

    $('.cbx-markup-chemTagger').change(function() {
        var tooltip_class = '.chemTagger-tooltip';
        var tooltip_color = 'chemTagger-tooltip-color';
        if(this.checked) {
            turnOnTooltip(tooltip_class, tooltip_color);
            showChemTagger = true;
        }else{
            turnOffTooltip(tooltip_class,tooltip_color);
            showChemTagger = false;
        }
    });

    $('.left_tab_links').click(function(){
        $(this).addClass("active");
        $(this).siblings().removeClass("active");

        currSideTab = $(this).attr('id');
        switchTab();
    });

    $("#search-btn").click(function(){
        $(".search-info").slideDown();
    });

    $("#up-arrow").click(function(){
        $(".search-info").slideUp();
    })

    $("#search-text").keyup(function(event){
        if(event.keyCode == 13){
            search();
        }
    });
});

function search(){
    $(".no-results").hide();
    $("#search-text").removeClass("no-results-box");
    var searchText = $("#search-text").val();

    if(searchText != ""&& searchText != null){
        $.get("searchservlet", {searchParam: searchText}, function(responseText){
            var json = JSON.stringify(responseText);
            var json_text = responseText.searchParam;
            console.log(responseText);
            console.log(json);

            $(".doc_item").each(function(){
                $(this).hide();
            });

            if(responseText.searchParam.length > 0){
                for(var i = 0; i < json_text.length; i++) {
                    var doc = json_text[i];
                    var doc_item = "li#" + doc;
                    $(doc_item).show();

                    if (i == 0) {
                        if (!$(doc_item).hasClass("active")) {
                            $(doc_item).addClass("active");
                            currDoc = doc;
                            switchTab();
                        }
                    } else {
                        $(doc_item).removeClass("active");
                    }
                }

                for(var i = (json_text.length-1); i > 0; i--){
                    if(i-1 >= 0){
                        var doc_item = "li#" + json_text[i];
                        var nextDoc = 'li#' + json_text[i-1];
                        $(doc_item).before($(nextDoc));
                    }
                }
            }else{
                $(".no-results").show();
                $("#search-text").addClass("no-results-box");
            }
        });
    }else{
        showAllDocs();

        var list = $("ul.doc_list");

        for(var i = 0; i < document_array.length; i++){
            var doc_child = "li#document-" + i;
            $(doc_child).remove();
        }

        for(var i = 0; i < document_array.length; i++){
            list.append(document_array[i]);
        }
    }
}

function showAllDocs(){
    $(".doc_item").each(function(){
        $(this).show();
        $(this).removeClass("active");
    });

    $("li#document-0").addClass("active");
    currDoc = "document-0";
    switchTab();
}


function switchTab(){
    if(currSideTab == "ont_tab"){
        var showTab = "." + currSideTab;
        showDocList = false;
        $('.right_document_tab').hide();
    }else{
        showDocList = true;
        var showTab = "#" + currSideTab + "_" + currDoc;
        $('.tab_content').hide();
        $('.right_document_tab').show();

    }
    $(showTab).show();
}

function turnOffTooltip(tooltip_class,tooltip_color){
    $(tooltip_class).removeClass(tooltip_color);
}

function turnOnTooltip(tooltip_class, tooltip_color){
    $(tooltip_class).addClass(tooltip_color);
}
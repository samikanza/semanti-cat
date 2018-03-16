package com.semantic_markup;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.OntClass;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by samikanza on 03/08/2017.
 */
@WebServlet("/searchservlet")
public class SearchServlet extends HttpServlet {

//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchResult = "no result";
        String searchTerm = request.getParameter("searchParam").toLowerCase();

        String filePath =  getServletContext().getRealPath("/");
        ELNDocumentCollection documentCollection = new ELNDocumentCollection();
        documentCollection.addDocumentsForWebTest(filePath);
        ArrayList<ELNDocument> documents = documentCollection.getDocumentCatalog();

        for(int i = 0; i < documents.size(); i++) {
            ELNDocument currDoc = documents.get(i);
            currDoc.setFilePath(filePath);
            currDoc.readSavedFile();
            currDoc.setSearchScore(0.0);
            currDoc.setDocPosition(i);
        }

        //specific search
        if(searchTerm.contains("title:") || searchTerm.contains("text:")){
            String[] searchTerms = searchTerm.split(":");
            String searchType = searchTerms[0].trim();
            String[] searchString = searchTerms[1].trim().split(" ");

            switch(searchType){
                case "name" : searchResult = searchFiles(searchString,documents, 1); break;
                case "text" : searchResult = searchFiles(searchString,documents, 2); break;
            }
        //generic search
        }else{
            String[] searchString = searchTerm.split(" ");
            searchResult = searchFiles(searchString, documents, 0);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String responseMsg = "{\"searchParam\":[" + searchResult + "]}";
        response.getWriter().write(responseMsg);
    }

    public Double getTextScore(String search, ELNDocument document){
        Double matches = new Double(StringUtils.countMatches(document.getFileContent().toLowerCase(), search.toLowerCase()));
        Double matchesScore =  matches / document.getWordCount();
        return matchesScore;
    }

    public Double getTitleScore(String search, ELNDocument document){
        Double matches = new Double(StringUtils.countMatches(document.getTextTitle().toLowerCase(), search.toLowerCase()));
        Double matchesScore =  matches / document.getTitleCount();
        return matchesScore;
    }

    /***
     *
     * @param search
     * @param documents
     * @param searchType - 0 = general, 1 = title, 2 = text
     * @return searchScores
     *
     *
     * search function, will add term frequency weighting of title and or text depending on searchtype
     */
    public String searchFiles(String[] search, ArrayList<ELNDocument> documents, int searchType){
        ArrayList<ELNDocument> textDocuments = new ArrayList<>();

        //loop through documents, sets the
        for(int i = 0; i < documents.size(); i++){
            ELNDocument currDoc = documents.get(i);
            currDoc.setSearchScore(0.0);
            Double score = 0.0;

            for(int j = 0; j < search.length; j++){
                if(currDoc.getFileContent().toLowerCase().contains(search[j].toLowerCase()) || currDoc.getTextTitle().toLowerCase().contains(search[j].toLowerCase())) {
                    if (!textDocuments.contains(currDoc)) {
                        textDocuments.add(currDoc);
                    }
                    //add title scores
                    if (searchType == 0 || searchType == 1) {
                        score += getTextScore(search[j], currDoc);
                    }
                    //add text scores
                    if (searchType == 0 || searchType == 2){
                        score += getTitleScore(search[j], currDoc);
                    }
                }
            }
            currDoc.setSearchScore(score);
        }

        return sortDocuments(textDocuments);
    }


//    public String searchFilesByTag(String search, ArrayList<ELNDocument> documents, boolean includeAll){
//        ArrayList<ELNDocument> tagDocuments = new ArrayList<>();
//
//        for(int i = 0; i < documents.size(); i++){
//            ELNDocument currDoc = documents.get(i);
//            TreeMap<String,Integer> gateTags = currDoc.getGateTagsAsHash();
//            TreeMap<String,Integer> chemTaggerTags = currDoc.getChemTaggerTagsAsHash();
//            TreeMap<String,Integer> ontologyTags = currDoc.getOntologyTagsAsHash();
//            TreeMap<String,Integer> openCalaisTags = currDoc.getOpenCalaisTagsAsHash();
//
//            if(openCalaisTags.get(search) != null){
//                tagDocuments.add(currDoc);
//                currDoc.setSearchScore(new Double(openCalaisTags.get(search)));
//            } else if(gateTags.get(search) != null){
//                tagDocuments.add(currDoc);
//                currDoc.setSearchScore(new Double(gateTags.get(search)));
//            }else if(chemTaggerTags.get(search) != null){
//                tagDocuments.add(currDoc);
//                currDoc.setSearchScore(new Double(chemTaggerTags.get(search)));
//            }else if(ontologyTags.get(search) != null){
//                tagDocuments.add(currDoc);
//                currDoc.setSearchScore(new Double(ontologyTags.get(search)));
//            //if this is a full search we wil now look at names and text appearances.
//            }else if(includeAll){
//                //tag scores are 1-3, they factor in if the tag is also in the title
//                if(currDoc.getTitle().toLowerCase().contains(search.toLowerCase())){
//                    tagDocuments.add(currDoc);
//                    currDoc.setSearchScore(0.5);
//                }else if(currDoc.getFileContent().toLowerCase().contains(search.toLowerCase())){
//                    tagDocuments.add(currDoc);
//                    Double matches = new Double(StringUtils.countMatches(currDoc.getFileContent().toLowerCase(), search.toLowerCase()));
//                    Double percentage = new Double((matches * 100D) / new Double(currDoc.getWordCount()));
//                    currDoc.setSearchScore(100.0-percentage);
//                }
//            }
//        }
//
//        return reverseSortDocuments(tagDocuments);
//    }


//    public String searchFilesByTermFrequencyTag(String search, ArrayList<ELNDocument> documents, String tagType){
//        ArrayList<ELNDocument> tagDocuments = new ArrayList<>();
//
//        //loop through documents, sets the
//        for(int i = 0; i < documents.size(); i++){
//            ELNDocument currDoc = documents.get(i);
//            currDoc.setSearchScore(0.0);
//            int wordCount = currDoc.getWordCount();
//            ArrayList<String> tags = new ArrayList<>();
//
//            switch(tagType){
//                case "chemtagger": tags = new ArrayList<>(currDoc.getChemTaggerTagsAsArray()); break;
//                case "gate" : tags = new ArrayList<>(currDoc.getGateTagsAsArray()); break;
//                case "ontology" : tags = new ArrayList<>(currDoc.getOntologyKeys()); break;
//            }
//
//            if(tags.contains(search)){
//                tagDocuments.add(currDoc);
//                Double matches = new Double(StringUtils.countMatches(currDoc.getFileContent().toLowerCase(), search));
//                currDoc.setSearchScore(new Double((matches * 100D) / new Double(wordCount)));
//            }
//        }
//
//        return sortDocuments(tagDocuments);
//    }

//    public String searchFilesByOpenCalais(String search, ArrayList<ELNDocument> documents){
//        String searchLetter =  search.substring(0,1).toUpperCase();
//        search = searchLetter + search.substring(1,search.length());
//        ArrayList<ELNDocument> openCalaisDocuments = new ArrayList<>();
//
//        //loop through documents, sets the
//        for(int i = 0; i < documents.size(); i++){
//            ELNDocument currDoc = documents.get(i);
//            currDoc.setSearchScore(0.0);
//            TreeMap<String,Integer> openCalaisTags = currDoc.getOpenCalaisTagsAsHash();
//
//            if(openCalaisTags.get(search) != null){
//                openCalaisDocuments.add(currDoc);
//                currDoc.setSearchScore(new Double(openCalaisTags.get(search)));
//            }
//        }
//
//        return reverseSortDocuments(openCalaisDocuments);
//    }

//    //simple name search to see if the string is contained in the title
//    public String searchFilesByName(String search, ArrayList<ELNDocument> documents){
//        String searchArray = "";
//
//        for(int i = 0; i < documents.size(); i++){
//            if(documents.get(i).getTitle().toLowerCase().contains(search.toLowerCase())){
//                searchArray += "\"document-"+ Integer.toString(i) +"\",";
//            }
//        }
//
//        searchArray = searchArray.substring(0, searchArray.length()-1);
//
//        return searchArray;
//    }



    //sorts documents in order of highest term frequency, used for gate/chemTagger/ontology
    public String sortDocuments(ArrayList<ELNDocument> tagDocuments){
        String searchArray = "";
        if(tagDocuments.size() > 0){
            if(tagDocuments.size() > 1){
                Collections.sort(tagDocuments);
            }

            for(int i = 0; i < tagDocuments.size(); i++){
                ELNDocument currDoc = tagDocuments.get(i);
                searchArray += "\"document-"+ Integer.toString(currDoc.getDocPosition()) +"\",";
            }
            searchArray = searchArray.substring(0, searchArray.length()-1);
        }
        return searchArray;
    }

    //sorts documents in reverse order to use priorities, used for open calais and general searches
    public String reverseSortDocuments(ArrayList<ELNDocument> tagDocuments){
        String searchArray = "";
        if(tagDocuments.size() > 0){
            if(tagDocuments.size() > 1){
                Collections.sort(tagDocuments);
                Collections.reverse(tagDocuments);
            }

            for(int i = 0; i < tagDocuments.size(); i++){
                ELNDocument currDoc = tagDocuments.get(i);
                searchArray += "\"document-"+ Integer.toString(currDoc.getDocPosition()) +"\",";
            }
            searchArray = searchArray.substring(0, searchArray.length()-1);
        }
        return searchArray;
    }

}

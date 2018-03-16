package com.semantic_markup;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistryPOSTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistrySentenceParser;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by samikanza on 19/05/2017.
 */
public class ChemTagger {

    ChemistryPOSTagger chemPos;
    ArrayList<ELNDocument> documents;
    SAXBuilder saxBuilder = new SAXBuilder();


    public ChemTagger(ArrayList<ELNDocument> documents){
        chemPos = ChemistryPOSTagger.getDefaultInstance();
        this.documents = documents;

    }

    public void tagDocuments(){
        for(int i = 0; i < documents.size(); i++){
            POSContainer posContainer = chemPos.runTaggers(documents.get(i).getFileContent(), true);
            ChemistrySentenceParser chemistrySentenceParser = new ChemistrySentenceParser(posContainer);
            chemistrySentenceParser.parseTags();
            documents.get(i).setChemTaggerDocument(chemistrySentenceParser.makeXMLDocument());
            System.out.println(chemistrySentenceParser.makeXMLDocument().toXML());
            generateTagsAndActions(documents.get(i).getChemTaggerDocument().toXML(), i);
        }
    }

    public void generateTagsAndActions(String chemDoc, int docNo){
        ArrayList<String> actionList = new ArrayList<>();
        HashMap<String,Double> tagHash = new HashMap<>();
        Document document = null;
        try {

            StringReader reader = new StringReader(chemDoc);
            document = saxBuilder.build(reader);
            ELNDocument currDoc = documents.get(docNo);

            Iterator<Element> actionDescendants = document.getDescendants(new ElementFilter("ActionPhrase"));

            while(actionDescendants.hasNext()) {
                Attribute actionAttr = actionDescendants.next().getAttribute("type");
                String action = actionAttr.getValue();
                if (!actionList.contains(action)){
                    actionList.add(action);
                }
            }

            Iterator<Element> moleculeDescendants = document.getDescendants(new ElementFilter("OSCAR-CM"));

            while(moleculeDescendants.hasNext()) {
                String molecule = moleculeDescendants.next().getText().toLowerCase();

                if(!tagHash.containsKey(molecule)) {
                    tagHash.put(molecule, currDoc.getTagWeight(molecule, false, 0));
                }

            }

            String cleanedDoc = chemDoc;
            cleanedDoc = cleanedDoc.replaceAll("((?!<ActionPhrase.*?>|</ActionPhrase>|<OSCAR-CM>|</OSCAR-CM>)(<.*?>))", " ");
            cleanedDoc = cleanedDoc.trim().replaceAll(" +", " ");
            cleanedDoc = cleanedDoc.replaceAll("\\s+(?=\\p{Punct})", "");

            for(String action: actionList){
                String match = "<ActionPhrase type=\"" + action + "\">(.*?)</ActionPhrase>";
                String replace = " <div class=' tooltip chemTagger-tooltip' data-markup='chemTagger' data-category='action'>$1<span class='tooltiptext'>" + action + "</span></div> ";
                cleanedDoc = cleanedDoc.replaceAll(match, replace);
            }

            cleanedDoc = cleanedDoc.replaceAll("<OSCAR-CM>", " <div class=' tooltip osc4r-tooltip' data-markup='osc4r' data-category='osc4rChemicalElement'>");
            cleanedDoc = cleanedDoc.replaceAll("</OSCAR-CM>", "<span class='tooltiptext'>Chemical Element</span></div> ");
            cleanedDoc = cleanedDoc.replaceAll("  ", " ");

            documents.get(docNo).setChemTaggerMarkup(cleanedDoc);
            documents.get(docNo).setChemTaggerActions(actionList);
            documents.get(docNo).setChemTaggerTags(tagHash);

        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

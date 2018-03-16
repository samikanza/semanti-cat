package com.semantic_markup;

import java.util.ArrayList;
import gate.util.*;
import org.w3c.dom.Document;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistryPOSTagger;
import uk.ac.cam.ch.wwmm.chemicaltagger.ChemistrySentenceParser;
import uk.ac.cam.ch.wwmm.chemicaltagger.POSContainer;
import uk.ac.cam.ch.wwmm.chemicaltagger.Utils;

import java.io.*;

/**
 * Created by samikanza on 31/03/2017.
 */
public class Main {

    public static void main(String[] args) {

        ELNDocumentCollection documentCollection = new ELNDocumentCollection();
        documentCollection.addDocsForTest();
        ArrayList<ELNDocument> documents = documentCollection.getDocumentCatalog();
        ArrayList<ELNDocument> docsToProcess = new ArrayList<>();
//
        Ontology rxnoOntology = new Ontology("ontologies/rxno.owl", "http://purl.obolibrary.org/obo/rxno.owl", true);
        Ontology mopOntology = new Ontology("ontologies/mop.owl", "http://purl.obolibrary.org/obo/chmo.owl", true);
        Ontology chmoOntology = new Ontology("ontologies/chmo.owl", "http://purl.obolibrary.org/obo/mop.owl", true);
        Ontology physicsOntology = new Ontology("ontologies/physics.owl", "http://www.astro.umd.edu/~eshaya/astro-onto/owl/physics.owl", false);
        Ontology cellOntology = new Ontology("ontologies/cl.owl", "http://purl.obolibrary.org/obo/cl.owl", true);
        Ontology plantOntology = new Ontology("ontologies/po.owl", "http://purl.obolibrary.org/obo/po.owl", true);

        ArrayList<Ontology> ontologies = new ArrayList<Ontology>();
        ontologies.add(rxnoOntology);
        ontologies.add(mopOntology);
        ontologies.add(chmoOntology);
        ontologies.add(physicsOntology);
        ontologies.add(cellOntology);
        ontologies.add(plantOntology);

        for(ELNDocument doc : documents) {
            //if (doc.needsProcessing()) {
                docsToProcess.add(doc);
            //}
        }

        if(docsToProcess.size() > 0){
 //           OPEN CALAIS

            for(ELNDocument doc: docsToProcess) {
                HttpClientCalaisPost myPost = new HttpClientCalaisPost(doc);
            }

            //CHEM TAGGER
            ChemTagger chemTagger = new ChemTagger(docsToProcess);
            chemTagger.tagDocuments();
            //GATE
            try {
                GateRun gr = new GateRun("", "CorpusPipeline/application.xgapp", docsToProcess, "ChemicalCompound,ChemicalIon,ChemicalElement");
            } catch(GateException g){
                System.out.println(g.getMessage());
                System.out.println(g.getStackTrace());
            } catch(IOException i){
                System.out.println("IO Exception");
            }

            OntologyTagger ontologyTagger = new OntologyTagger(ontologies, docsToProcess);
            ontologyTagger.createMarkup();

            for(int i = 0; i < docsToProcess.size(); i++){
                docsToProcess.get(i).writeTextFile();
            }
       }else{
//            for(ELNDocument doc : documents){
//                doc.readSavedFile();
//            }
        }

        int matrix = new File("matricies").listFiles().length;
        if(matrix != ((documents.size()*6) + 6)) {
            for (int i = 0; i < documents.size(); i++) {
                documents.get(i).createRxnoMatrix(i, rxnoOntology.getOntClassStrings());
                documents.get(i).createChmoMatrix(i, chmoOntology.getOntClassStrings());
                documents.get(i).createMopMatrix(i, mopOntology.getOntClassStrings());
                documents.get(i).createPhysicsMatrix(i, physicsOntology.getOntClassStrings());
                documents.get(i).createCellMatrix(i, cellOntology.getOntClassStrings());
                documents.get(i).createPlantMatrix(i, plantOntology.getOntClassStrings());
            }
                documentCollection.getDocumentOntologyTags();
                documentCollection.createRxnoMatrix(rxnoOntology.getOntClassStrings());
                documentCollection.createChmoMatrix(chmoOntology.getOntClassStrings());
                documentCollection.createMopMatrix(mopOntology.getOntClassStrings());
                documentCollection.createPhysicsMatrix(physicsOntology.getOntClassStrings());
                documentCollection.createCellMatrix(cellOntology.getOntClassStrings());
                documentCollection.createPlantMatrix(plantOntology.getOntClassStrings());
        }


        for(ELNDocument doc : documents){
            System.out.println("chemTagger = " + documents.get(0).getChemTaggerMarkup()) ;
            System.out.println("gate = " + documents.get(0).getGateMarkup());

            System.out.println("unmark = " + doc.getUnmarkedContent());

            System.out.println("open Calais Tags = " + doc.getOpenCalaisTags());
            System.out.println(doc.getWordCount());
            System.out.println("<splitter>");
            System.out.println(doc.getTitleCount());
            System.out.println("<splitter>");
            System.out.println(doc.getTextTitle());
            System.out.println("<splitter>");
            System.out.println(doc.getGateTags());
            System.out.println("<splitter>");
            //System.out.println("chemTagger Actions = " + doc.getChemTaggerActions()) ;

            System.out.println(doc.getOntologyTags());
            System.out.println("<splitter>");
            System.out.println(doc.getChemTaggerTags()) ;
            System.out.println(doc.getChemTaggerMarkup()) ;
            System.out.println("ontologies = " + doc.getOntologyMarkup());

        }
    }

}


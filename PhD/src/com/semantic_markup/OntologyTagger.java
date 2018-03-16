package com.semantic_markup;

import org.apache.jena.ontology.OntClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by samikanza on 19/05/2017.
 */
public class OntologyTagger {

    ArrayList<Ontology> ontologies;
    ArrayList<ELNDocument> documents;
    ArrayList<OntClass> ontologyClasses;

    public OntologyTagger(ArrayList<Ontology> ontologies, ArrayList<ELNDocument> documents){
        this.ontologies = ontologies;
        this.documents = documents;
    }

    public void createMarkup(){
        for(ELNDocument doc : documents){
            for(Ontology ont : ontologies){
                doc.copyHash(ont.returnHash());
                doc.addSemanticTags(ont.getModel(), ont.getOntClasses());
            }
            doc.stripSemanticTags();
            doc.createOntologyMarkup();
        }
    }

}

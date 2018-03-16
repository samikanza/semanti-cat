/**
 * Created by samikanza on 06/04/2017.
 */

package com.semantic_markup;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.ArrayList;

public class Ontology {

    private OntModel myModel = null;
    private AnnotationProperty deffProp = null;
    private HashMap ontoKey = new HashMap<String,String>();
    private ArrayList<OntClass> ontClasses = new ArrayList<OntClass>();
    private ArrayList<String> ontClassStrings = new ArrayList<>();
    private HashMap<String,OntClass> ontClassHash = new HashMap<>();

    public Ontology(String fileName, String base, boolean purlOnt){
        FileReader ontoReader = null;

        try{
            ontoReader = new FileReader(fileName);
        }catch(FileNotFoundException fe){
            System.out.println(fe.getStackTrace());
        }

        myModel = ModelFactory.createOntologyModel();
        myModel.read(ontoReader,base);
        System.out.println("making model");

        if(purlOnt){
            deffProp = myModel.getAnnotationProperty("http://purl.obolibrary.org/obo/IAO_0000115");
            fillHashMapAndOntClasses();
        }else{
            fillHashMapAndOntClassesLabels();
        }
    }

    public ArrayList<OntClass> getOntClasses(){
        return ontClasses;
    }

    //fills hashmap with label -> label and adds the classes to an ArrayList
    private void fillHashMapAndOntClassesLabels() {
        ExtendedIterator classes = myModel.listClasses();
        while (classes.hasNext()) {
            OntClass thisClass = (OntClass) classes.next();
            String label = thisClass.getLabel("");

            if(label != null && !label.equals("")){
                label = label.toLowerCase().trim();
                ontClasses.add(thisClass);
                if(!ontoKey.containsKey(label)){
                    ontoKey.put(label, label);
                }
                ontClassStrings.add(label);
            }
        }
    }

    //fills hashmap with label -> definition and adds the classes to an ArrayList
    private void fillHashMapAndOntClasses() {
        ExtendedIterator classes = myModel.listClasses();
        while (classes.hasNext()) {
            OntClass thisClass = (OntClass) classes.next();
            String label = thisClass.getLabel("");
            RDFNode definitionNode = null;
            String definitionString = "";

            if(label != null && !label.equals("") && !label.equals("group")){
                label = label.toLowerCase().trim();
                ontClasses.add(thisClass);
                definitionNode = thisClass.getPropertyValue(deffProp);
                if (definitionNode != null) {
                    definitionString = definitionNode.toString();
                }else{
                    definitionString = label;
                }
                if(ontoKey.containsKey(label) && definitionString != label){
                    definitionString += (", " + ontoKey.get(label));
                    ontoKey.replace(ontoKey.get(label), definitionString);
                }
                ontoKey.put(label, definitionString);
                ontClassStrings.add(label);
            }
        }
    }

    public OntModel getModel() {
        return myModel;
    }

    public String getDescriptionFromClass(String className) {
        return ontoKey.get(className).toString();
    }

    public Object[] returnKeys(){
        return ontoKey.keySet().toArray();
    }

    public HashMap<String,String> returnHash(){
        return ontoKey;
    }

    public ArrayList<String> getOntClassStrings(){
        return ontClassStrings;
    }

    public HashMap<String,OntClass> returnClassHash(){
        return ontClassHash;
    }
}

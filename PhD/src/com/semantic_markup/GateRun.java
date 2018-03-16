package com.semantic_markup;

/**
 * Created by don crookshanks, given to Sami Kanza on 11/05/2017.
 */
// GateRun.java

import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.util.persistence.PersistenceManager;
import mark.chemistry.Tagger;
import org.apache.commons.lang.StringUtils;

public class GateRun {

    public static String Sentinel = "QxNs5w3dHv.";

    private CorpusController controller;

    public GateRun(String filePath, String gappName, ArrayList<ELNDocument> elnDocuments, String annotations) throws GateException, IOException {

        // initialise the GATE library

        Gate.setGateHome(new File(filePath +"gate-8.4-build5748-SRC/"));
        System.out.println("Initialising GATE");
        Gate.init();

        FeatureMap params = Factory.newFeatureMap(); //empty map:default params

        // initialise GATE application (this may take several minutes)

        initialise(gappName);

        // create a GATE corpus and add a document for each command-line argument

        Corpus corpus = Factory.newCorpus("GateRun corpus");

        for (int i = 0; i < elnDocuments.size(); i++){
            addDocument(corpus, elnDocuments.get(i).getFileContent(), Integer.toString(i));
        }

        // tell the pipeline about the corpus and run it

        setCorpus(corpus);
        execute();


        Iterator iter = corpus.iterator();

        while (iter.hasNext()) {

            Document doc = (Document) iter.next();

            int docNameRef = Integer.parseInt(doc.getName());

            AnnotationSet defaultAnnotSet = doc.getAnnotations();

            Set annotTypesRequired = new HashSet();

            // The annotations to use are defined by the third command line argument
            // as a comma separated list of annotation names (no spaces.)

            for (String annotation : annotations.split(",")) {
                annotTypesRequired.add(annotation);
            }

            Set<Annotation> annotationSet = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

            String xmlDocument = doc.toXml(annotationSet, true);

            ELNDocument currDoc = elnDocuments.get(docNameRef);
            createDocumentTags(annotationSet, currDoc.getFileContent(), currDoc);
        }

    }

    public void initialise(String gappName) throws GateException, IOException {

        System.out.println("Initialising GATE application");

        File gappFile = new File(gappName);

        controller = (CorpusController) PersistenceManager.loadObjectFromFile(gappFile);
    }

    public void setCorpus(Corpus corpus) {
        controller.setCorpus(corpus);
    }

    public void execute() throws GateException {

        System.out.println("Running GATE application");
        controller.execute();
    }

    public static void addDocument(Corpus corpus, String text, String docName) throws ResourceInstantiationException {

        FeatureMap params = Factory.newFeatureMap();

        // If the entry is blank, place the sentinel so that we can test for it
        // later.

        if (text.equals("")) {
            text = Sentinel;
        }

        params.put("stringContent", text);
        params.put("preserveOriginalContent", new Boolean(true));
        params.put("collectRepositioningInfo", new Boolean(true));

        Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
        doc.setName(docName);

        corpus.add(doc);
    }

    public void createDocumentTags(Set annotationSet, String docContent, ELNDocument currDoc){

        HashMap<String,Double> docTags = new HashMap<String,Double>();
        ArrayList<String> chemElements = new ArrayList<String>();
        ArrayList<String> chemCompounds = new ArrayList<String>();

        for (Object annotation : annotationSet) {
            if(annotation.toString().contains("offset=")){
                String beginChar = annotation.toString().split("offset=")[1].split(";")[0];
                String endChar = annotation.toString().split("offset=")[2].split(";")[0];
                String annot = docContent.toString().substring(Integer.parseInt(beginChar.trim()), Integer.parseInt(endChar.trim()));

                if(isContain(currDoc.getFileContent().toLowerCase(),annot)){
                    if(!docTags.containsKey(annot)) {
                        docTags.put(annot,  currDoc.getTagWeight(annot, false, 0));

                        if (annot.matches("type=ChemicalCompound")) {
                            chemCompounds.add(annot);
                        } else {
                            chemElements.add(annot);
                        }
                    }
                }
            }
        }

        currDoc.setGateTags(docTags);
        currDoc.setGateCompounds(chemCompounds);
        currDoc.setGateElements(chemElements);
        currDoc.addGateMarkup();
    }

    private static boolean isContain(String source, String searchTerm){
        String pattern = "\\b"+searchTerm+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(source);
        return m.find();
    }

}

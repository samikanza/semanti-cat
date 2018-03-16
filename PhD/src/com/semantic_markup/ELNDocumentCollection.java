package com.semantic_markup;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by samikanza on 19/05/2017.
 */
public class ELNDocumentCollection {

    ArrayList<ELNDocument> documents;
    ArrayList<String> docOntTags;
    ArrayList<Set<String>> docArray;

    public ELNDocumentCollection(){
        documents = new ArrayList<>();
        docOntTags = new ArrayList<>();
        docArray = new ArrayList<>();
    }

    public void addDocsForTest(){
        ArrayList<String> docAuthors = new ArrayList<String>();
        docAuthors.add("Sami Kanza");
        String tempDate = "2017-08-04";

        File folder = new File("elndocs/");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().contains(".txt")) {
               ELNDocument elnDoc =  new ELNDocument(file.getAbsolutePath(), file.getName(), docAuthors, tempDate);
               documents.add(elnDoc);
            }
        }
    }

    public void addDocumentsForWebTest(String webFilePath){
        ArrayList<String> docAuthors = new ArrayList<String>();
        docAuthors.add("Sami Kanza");
        String tempDate = "2017-08-04";

        File folder = new File(webFilePath + "elndocs/");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().contains(".txt")) {
                ELNDocument elnDoc =  new ELNDocument(file.getAbsolutePath(), file.getName(), docAuthors, tempDate);
                documents.add(elnDoc);
            }
        }
    }

    public ArrayList<ELNDocument> getDocumentCatalog(){
        return documents;
    }

    public void getDocumentOntologyTags(){

        Set<String> docOntSet = new HashSet<>();

        for(ELNDocument doc : documents){
            docOntSet.addAll(doc.getOntologyKeys());
            docArray.add(doc.getOntologyKeys());
        }

        docOntTags.addAll(docOntSet);
    }

    public void createMainMatrixFile(String fileTitle, ArrayList<String> ontTags){
        PrintWriter writer = null;
        int termNo = 0;
        String fileLine = "term1,term2,count\n";
        try {

            //for each string in all documents
            for(String tag1 : docOntTags){
                for(String tag2: docOntTags){
                    //if specific ontology contains both tags
                    if(ontTags.contains(tag1) && ontTags.contains(tag2) && !tag1.equals(tag2)){
                        int count = 0;
                        for(Set<String> docSet : docArray){
                            if(docSet.contains(tag1) && docSet.contains(tag2)){
                                count++;
                            }
                        }
                        fileLine += tag1 + "," + tag2 + "," + Integer.toString(count) + "\n";
                        if(count > 0){
                            termNo ++;
                        }
                    }
                }
            }

            if (termNo > 0) {
                writer = new PrintWriter(fileTitle, "UTF-8");
                writer.print(fileLine);
                writer.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void createRxnoMatrix(ArrayList<String> ontTags){
        String fileTitle = "matricies/rxno-matrix.csv";
        createMainMatrixFile(fileTitle, ontTags);
    }

    public void createChmoMatrix(ArrayList<String> ontTags){
        String fileTitle = "matricies/chmo-matrix.csv";
        createMainMatrixFile(fileTitle, ontTags);
    }

    public void createMopMatrix(ArrayList<String> ontTags){
        String fileTitle = "matricies/mop-matrix.csv";
        createMainMatrixFile(fileTitle, ontTags);
    }

    public void createPhysicsMatrix(ArrayList<String> ontTags){
        String fileTitle = "matricies/physics-matrix.csv";
        createMainMatrixFile(fileTitle, ontTags);
    }

    public void createCellMatrix(ArrayList<String> ontTags){
        String fileTitle = "matricies/cl-matrix.csv";
        createMainMatrixFile(fileTitle, ontTags);
    }

    public void createPlantMatrix(ArrayList<String> ontTags){
        String fileTitle = "matricies/po-matrix.csv";
        createMainMatrixFile(fileTitle, ontTags);
    }

    public boolean doesMatrixExist(String filePath, String matrix) {
        boolean matrixExists = false;
        String matrixFilePath = filePath + "matricies/" + matrix + ".csv";
        File matrixFile = new File(matrixFilePath);
        if(matrixFile.exists()){
            matrixExists = true;
        }
        return matrixExists;
    }

}

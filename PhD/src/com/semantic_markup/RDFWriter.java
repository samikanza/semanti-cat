package com.semantic_markup;

import org.apache.jena.rdf.model.*;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.File;

/**
 * Created by samikanza on 24/04/2017.
 */
public class RDFWriter {

    private String dcURI = "http://purl.org/dc/elements/1.1/";
    private Model model = ModelFactory.createDefaultModel();
    private ArrayList<ELNDocument> documents;

    public RDFWriter(ArrayList<ELNDocument> documents) {
        this.documents = documents;
        String sTitle = "title";
        String sCreator = "creator";
        String sDate = "date";
        String sDesc = "description";

        for (int i = 0; i < documents.size(); i++) {
            ELNDocument currentDoc = documents.get(i);
            Resource docResource = model.createResource(currentDoc.getTitle());
            Property pTitle = model.createProperty(dcURI, sTitle);
            Property pCreator = model.createProperty(dcURI, sCreator);
            Property pDate = model.createProperty(dcURI, sDate);
            Property pDesc = model.createProperty(dcURI, sDesc);

            docResource.addProperty(pTitle, currentDoc.getTitle());

            for(int j = 0; j < currentDoc.getAuthors().size(); j++){
                docResource.addProperty(pCreator, currentDoc.getAuthors().get(j));
            }
            docResource.addProperty(pDate, currentDoc.getDate().toString());
            docResource.addProperty(pDesc, currentDoc.getFileContent());
        }

        try {
            model.write(new PrintWriter(new File("newRDF.rdf")));

        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
    }

}
package com.semantic_markup;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


/************************************************************
 - Simple Calais client to process file or files in a folder
 - Takes 3 arguments
 1. File or folder name to process
 2. Output folder name to store response from Calais
 3. Token
 - Please specify the correct web service location url for CALAIS_URL variable
 - Please adjust the values of different request parameters in the createPostMethod

 **************************************************************/

public class HttpClientCalaisPost {

    private static final String CALAIS_URL = "https://api.thomsonreuters.com/permid/calais";
    public static String uniqueAccessKey = "3vxFPXS3RGTwwJeSBBHSJ6ym6DmAG6eX";

    private File input;
    private File output;
    private HttpClient client;
    private ELNDocument document;
    private Model model = ModelFactory.createDefaultModel();

    public HttpClientCalaisPost(ELNDocument document){
        this.document = document;
        client = new HttpClient();

        run();
    }

    private void run(){
        try {
            postFile(createPostMethod(), document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private PostMethod createPostMethod() {

        PostMethod method = new PostMethod(CALAIS_URL);

        method.setRequestHeader("X-AG-Access-Token", uniqueAccessKey);
        method.setRequestHeader("Content-Type", "text/raw");
        method.setRequestHeader("omitOutputtingOtignialText", "true");
        method.setRequestHeader("x:calais-contentClass", "research");
        method.setRequestHeader("x-calais-language", "English");
        method.setRequestHeader("x-calais-selectiveTags", "socialtags,topic");
        method.setRequestHeader("outputformat", "xml/rdf");

        return method;
    }

    private void doRequest(PostMethod method, ELNDocument document) {
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                System.err.println("The Post method is not implemented by this URI");
                // still consume the response body
                method.getResponseBodyAsString();
            } else if (returnCode == HttpStatus.SC_OK) {
                System.out.println("File post succeeded: " + document.getTitle());
                saveResponse(method, document);
            } else {
                System.err.println("File post failed: " + document.getTitle());
                System.err.println("Got code: " + returnCode);
                System.err.println("response: "+method.getResponseBodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
    }

    private void saveResponse(PostMethod method, ELNDocument document) throws IOException {
        String responseMarkup = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    method.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                responseMarkup += line;
            }
            setOpenCalaisTags(document, responseMarkup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postFile(PostMethod method, ELNDocument document) throws IOException {
        method.setRequestEntity(new ByteArrayRequestEntity(document.getFileContent().getBytes("UTF-8")));
        doRequest(method, document);
    }

    private void setOpenCalaisTags(ELNDocument document, String responseMarkup) {
        HashMap<String, Integer> openCalaisTags = new HashMap<>();
        try {
            // Create memory model, read in RDF/XML document

            model.read(new ByteArrayInputStream(responseMarkup.getBytes()), null);

            // Print out objects in model using toString

            String socialTag = "http://d.opencalais.com/dochash-1/" + responseMarkup.split("http://d.opencalais.com/dochash-1/")[1].split("\">")[0] + "/SocialTag/";

            int i = 1;

            //iterates until there are no more social tags
            while(true){
                if(responseMarkup.contains("SocialTag/" + i)){
                    Resource resource = model.getResource( socialTag + Integer.toString(i));
                    StmtIterator iter = resource.listProperties();

                    int importancePred = 0;
                    String namePred = "";

                    //iterates over the properties to find the importance and name
                    while (iter.hasNext(  )) {
                        Statement currStatement = iter.next();
                        String predicate = currStatement.getPredicate().toString();

                        if(predicate.equals("http://s.opencalais.com/1/pred/importance")){
                            importancePred = Integer.parseInt(currStatement.getObject().toString());
                        }else if(predicate.equals("http://s.opencalais.com/1/pred/name")){
                            namePred = currStatement.getObject().toString();
                        }
                    }
                    openCalaisTags.put(namePred, importancePred);
                }else{
                    break;
                }

                i++;
            }

        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
        document.setOpenCalaisMarkup(responseMarkup);
        document.setOpenCalaisTags(openCalaisTags);
    }


}

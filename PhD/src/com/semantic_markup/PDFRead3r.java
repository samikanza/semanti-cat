package com.semantic_markup;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;

/**
 * Created by samikanza on 26/04/2017.
 */
public class PDFRead3r {

    public PDFRead3r(){
        try {

            PdfReader reader = new PdfReader("testPDF.pdf");
            System.out.println("This PDF has "+reader.getNumberOfPages()+" pages.");
            String page = PdfTextExtractor.getTextFromPage(reader, 2);
            System.out.println("Page Content:\n\n"+page+"\n\n");
            System.out.println("Is this document tampered: "+reader.isTampered());
            System.out.println("Is this document encrypted: "+reader.isEncrypted());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



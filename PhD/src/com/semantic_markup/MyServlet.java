package com.semantic_markup;

import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by samikanza on 03/08/2017.
 */
@WebServlet("/myservlet")
public class MyServlet extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String button = request.getParameter("button");

        if ("btn-download".equals(button)){

            String[] fileURLs = request.getParameterValues("file-checkbox");

            for(String fileURL: fileURLs){
                String [] fileDeets = fileURL.split("&url=");
                URL newFileURL = new URL(fileDeets[1]);

                String fileID = getServletContext().getRealPath("/") + "elndocs/" + fileDeets[0] + ".txt";
                downloadFile(newFileURL, fileID);
            }
        } else if (request.getParameter("btn-process") != null) {
           //process files
        }

        response.sendRedirect(request.getContextPath() + "/eln.jsp");
    }

//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    }

    public void downloadFile(URL fileURL, String filePath) {
        File saveFile = new File(filePath);
        try {
            FileUtils.copyURLToFile(fileURL, saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

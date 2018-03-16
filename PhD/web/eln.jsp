<%--
  Created by IntelliJ IDEA.
  User: samikanza
  Date: 16/04/2017
  Time: 15:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="java.io.File" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.semantic_markup.*" %>
<%@ page import="gate.util.GateException" %>
<%@ page import="java.io.IOException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%

    String jspPath = session.getServletContext().getRealPath("/");
    String filePath = jspPath + File.separator;
    ELNDocumentCollection documentCollection = new ELNDocumentCollection();
    documentCollection.addDocumentsForWebTest(filePath);
    ArrayList<ELNDocument> documents = documentCollection.getDocumentCatalog();
    ArrayList<ELNDocument> docsToProcess = new ArrayList();
    String hidden = "hidden";
    String active = "";
;
    for(ELNDocument doc : documents){
        if(doc.needsProcessing()){
          docsToProcess.add(doc);
        }
    }

  if(docsToProcess.size() > 0) {
    //OPEN CALAIS
    for(ELNDocument doc: docsToProcess) {
      HttpClientCalaisPost myPost = new HttpClientCalaisPost(doc);
    }

    //CHEM TAGGER
    ChemTagger chemTagger = new ChemTagger(docsToProcess);
    chemTagger.tagDocuments();

    //GATE
    try {
      GateRun gr = new GateRun(filePath, filePath + "CorpusPipeline/application.xgapp", docsToProcess, "ChemicalCompound,ChemicalIon,ChemicalElement");
    } catch (GateException g) {
      System.out.println(g.getMessage());
      System.out.println(g.getStackTrace());
    } catch (IOException i) {
      System.out.println("IO Exception");
    }

      Ontology rxnoOntology = new Ontology(filePath + "ontologies/rxno.owl", "http://purl.obolibrary.org/obo/rxno.owl", true);
      Ontology mopOntology = new Ontology(filePath + "ontologies/mop.owl", "http://purl.obolibrary.org/obo/chmo.owl", true);
      Ontology chmoOntology = new Ontology(filePath + "ontologies/chmo.owl", "http://purl.obolibrary.org/obo/mop.owl", true);
      Ontology physicsOntology = new Ontology(filePath + "ontologies/physics.owl", "http://www.astro.umd.edu/~eshaya/astro-onto/owl/physics.owl", false);
      Ontology cellOntology = new Ontology(filePath + "ontologies/cl.owl", "http://purl.obolibrary.org/obo/cl.owl", true);
      Ontology plantOntology = new Ontology(filePath + "ontologies/po.owl", "http://purl.obolibrary.org/obo/po.owl", true);

      ArrayList<Ontology> ontologies = new ArrayList<Ontology>();
    ontologies.add(rxnoOntology);
    ontologies.add(mopOntology);
    ontologies.add(chmoOntology);
    ontologies.add(physicsOntology);
    ontologies.add(cellOntology);
    ontologies.add(plantOntology);
    OntologyTagger ontologyTagger = new OntologyTagger(ontologies, docsToProcess);
    ontologyTagger.createMarkup();

    for(int i = 0; i < docsToProcess.size(); i++){
      docsToProcess.get(i).setFilePath(filePath);
      docsToProcess.get(i).writeTextFile();
    }
  }else {
    for (ELNDocument doc : documents) {
      doc.setFilePath(filePath);
      doc.readSavedFile();
    }
  }

//    int matrix = new File("matricies").listFiles().length;
//    if(matrix != (documents.size() + 6)) {
//        for (int i = 0; i < documents.size(); i++) {
//            documents.get(i).createRxnoMatrix(i, rxnoOntology.getOntClassStrings());
//            documents.get(i).createChmoMatrix(i, chmoOntology.getOntClassStrings());
//            documents.get(i).createMopMatrix(i, mopOntology.getOntClassStrings());
//            documents.get(i).createPhysicsMatrix(i, rxnoOntology.getOntClassStrings());
//            documents.get(i).createCellMatrix(i, chmoOntology.getOntClassStrings());
//            documents.get(i).createPlantMatrix(i, mopOntology.getOntClassStrings());
//        }
//        documentCollection.getDocumentOntologyTags();
//        documentCollection.createRxnoMatrix(rxnoOntology.getOntClassStrings());
//        documentCollection.createChmoMatrix(chmoOntology.getOntClassStrings());
//        documentCollection.createMopMatrix(mopOntology.getOntClassStrings());
//        documentCollection.createPhysicsMatrix(physicsOntology.getOntClassStrings());
//        documentCollection.createCellMatrix(cellOntology.getOntClassStrings());
//        documentCollection.createPlantMatrix(plantOntology.getOntClassStrings());
//    }
%>
<html>
  <head>
    <meta charset='UTF-8'/>
    <link rel="stylesheet" type="text/css" href="css/style.css">
    <link rel="stylesheet" type="text/css" href="css/chord.css">
    <link rel="icon" href="images/favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />
    <script src="js/jquery.js"></script>
    <script src="js/jquery-ui.js"></script>
    <script src="js/d3.js"></script>
    <script src="js/underscore_old.js"></script>
    <script src="js/mapper.js"></script>
    <script src="js/chord.js"></script>
    <script src="js/d3-tip.js"></script>
    <script src="js/semantic.js"></script>
    <script src="js/charts.js"></script>
  </head>
  <body>
        <div class="main">
            <div class="top_bar"><span>SEM<span id="header_cat">A</span>NTI-C</span><span id="header_cat">A</span><span>T</span></div>
            <div class="left_tab">
                <div id="doc_tab" class="left_tab_links odd active">Docs</div>
                <div id="full_tab" class="left_tab_links even">Full</div>
                <div id="markup_tab" class="left_tab_links odd">Markup</div>
                <div id="tag_cat_tab" class="left_tab_links even">Tags 1</div>
                <div id="tag_imp_tab" class="left_tab_links odd">Tags 2</div>
                <div id="terms_tab" class="left_tab_links even">Terms</div>
                <div id="ont_tab" class="left_tab_links odd">Ontology</div>
            </div>
            <div class="right_tab">
                <div class="right_document_tab">
                    <div class="document_list">
                        <div class="tab_type">Notebook</div>
                        <div class="search-div">
                            <input type="text" id="search-text" name="search-box"/>
                            <button type="submit" name="button" value="btn-search" id="search-btn">Advanced Search</button>
                        </div>
                        <div class="search-info">
                            For advanced searches use these: </br>
                            <strong>title: searchTerm</strong> (search by the title)</br>
                            <span style="float:left"><strong>text: searchTerm</strong> (search for text matches)</span>
                            <img id="up-arrow" src="images/upArrow.png"/>
                        </div>
                        <div class="no-results">
                            No results
                        </div>
                        <ul class="doc_list">
                            <%
                                for(int i = 0; i < documents.size(); i++){
                                    if(i == 0){
                                        active = "active";
                                    }else{
                                        active = "";
                                    }
                            %>
                                    <li class="doc_item <%=active%>" id="document-<%=Integer.toString(i)%>"><%=documents.get(i).getTitle()%></li>
                            <%
                                }
                            %>
                        </ul>
                    </div>
                    <div class="document_display">
                        <%
                            for(int i = 0; i < documents.size(); i++){
                                if(i == 0){
                                    hidden = "";
                                }else{
                                    hidden = "hidden";
                                }
                        %>
                            <div id="doc_tab_document-<%=Integer.toString(i)%>" class="tab_content <%=hidden%>">
                                <div class="document_sections full_width">
                                    <div class ="document_section">
                                        <%=documents.get(i).getUnmarkedContent()%>
                                    </div>
                                </div>
                            </div>

                            <div id="full_tab_document-<%=Integer.toString(i)%>" class="tab_content hidden">
                                <div class="document_sections partial_width">
                                    <div class ="document_section">
                                        <strong>Open Calais Tags:</strong> <%=documents.get(i).getOpenCalaisTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Chem Tagger Tags:</strong> <%=documents.get(i).getChemTaggerTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Gate Tags:</strong> <%=documents.get(i).getGateTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Ontology Tags:</strong> <%=documents.get(i).getOntologyTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Ontology Markup:</strong> <%=documents.get(i).getOntologyMarkup()%>
                                    </div>
                                </div>
                                <div class="document_markup">
                                    <div><input type="checkbox" class="cbx-markup-osc4r" id="cbx-osc4r-full-<%=Integer.toString(i)%>" name="osc4r-checkbox" value="osc4r-tags" checked/><span class="osc4r-checkbox">OSC4R Chemicals</span></div>
                                    <div><input type="checkbox" class="cbx-markup-gate" id="cbx-gate-full-<%=Integer.toString(i)%>" name="gate-checkbox" value="gate-tags" checked/><span class="gate-checkbox">Gate Chemicals</span></div>
                                    <div><input type="checkbox" class="cbx-markup-ontology" id="cbx-ontology-full-<%=Integer.toString(i)%>" name="ontology-checkbox" value="ontology-tags" checked/><span class="ontology-checkbox">Ontology Entities</span></div>
                                    <div><input type="checkbox" class="cbx-markup-chemTagger" id="cbx-chemTagger-full-<%=Integer.toString(i)%>" name="chemTagger-checkbox" value="chemTagger-tags" checked/><span class="chemTagger-checkbox">Actions</span></div>
                                </div>
                            </div>

                            <div id="markup_tab_document-<%=Integer.toString(i)%>" class="tab_content hidden">
                                <div class="document_sections partial_width">
                                    <div class ="document_section">
                                        <%=documents.get(i).getOntologyMarkup()%>
                                    </div>
                                </div>
                                <div class="document_markup">
                                    <div><input type="checkbox" class="cbx-markup-osc4r" id="cbx-osc4r-markup-<%=Integer.toString(i)%>" name="osc4r-checkbox" value="osc4r-tags" checked/><span class="osc4r-checkbox">OSC4R Chemicals</span></div>
                                    <div><input type="checkbox" class="cbx-markup-gate" id="cbx-gate-markup-<%=Integer.toString(i)%>" name="gate-checkbox" value="gate-tags" checked/><span class="gate-checkbox">Gate Chemicals</span></div>
                                    <div><input type="checkbox" class="cbx-markup-ontology" id="cbx-ontology-markup-<%=Integer.toString(i)%>" name="ontology-checkbox" value="ontology-tags" checked/><span class="ontology-checkbox">Ontology Entities</span></div>
                                    <div><input type="checkbox" class="cbx-markup-chemTagger" id="cbx-chemTagger-markup-<%=Integer.toString(i)%>" name="chemTagger-checkbox" value="chemTagger-tags" checked/><span class="chemTagger-checkbox">Actions</span></div>
                                </div>
                            </div>

                            <div id="tag_cat_tab_document-<%=Integer.toString(i)%>" class="tab_content hidden">
                                <div class="document_sections full_width">
                                    <div class ="document_section">
                                        <strong>Open Calais Tags:</strong> <%=documents.get(i).getOpenCalaisTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Chem Tagger Tags:</strong> <%=documents.get(i).getChemTaggerTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Gate Tags:</strong> <%=documents.get(i).getGateTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Ontology Tags:</strong> <%=documents.get(i).getOntologyTagsForWeb()%>
                                    </div>
                                </div>
                            </div>

                            <div id="tag_imp_tab_document-<%=Integer.toString(i)%>" class="tab_content hidden">
                                <div class="document_sections full_width">
                                    <div class ="document_section">
                                        <strong>Open Calais Tags:</strong> <%=documents.get(i).getOpenCalaisTagsForWeb()%>
                                    </div>
                                    <div class ="document_section">
                                        <strong>Gate/ChemTagger/Ontology Tags:</strong> <%=documents.get(i).getAllTagsForWeb()%>
                                    </div>
                                </div>
                            </div>

                            <div id="terms_tab_document-<%=Integer.toString(i)%>" class="tab_content hidden">
                                <div class="document_sections full_width">
                                    <%if(documents.get(i).getShowMatrix(i, filePath, "rxno-matrix")){
                                        String rxnoMatrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-rxno-matrix.csv";
                                        String rxnoMatrixValue = documents.get(i).getMatrix(rxnoMatrixFilePath);
                                        if(rxnoMatrixValue != ""){%>
                                        <div class ="document_section">
                                            <strong>Reactions Ontology</strong>
                                            <div id="bar-chart-<%=Integer.toString(i)%>-rxno" class="bar-chart" data-counts="<%=rxnoMatrixValue%>"></div>
                                        </div>
                                        <%}
                                    }if(documents.get(i).getShowMatrix(i, filePath, "chmo-matrix")){
                                        String chmoMatrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-chmo-matrix.csv";
                                        String chmoMatrixValue = documents.get(i).getMatrix(chmoMatrixFilePath);
                                        if(chmoMatrixValue != ""){%>
                                        <div class ="document_section">
                                            <strong>Chemical Methods Ontology</strong>
                                            <div id="bar-chart-<%=Integer.toString(i)%>-chmo" class="bar-chart" data-counts="<%=chmoMatrixValue%>"></div>
                                        </div>
                                        <%}
                                    }if(documents.get(i).getShowMatrix(i, filePath, "mop-matrix")){
                                        String mopMatrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-mop-matrix.csv";
                                        String mopMatrixValue = documents.get(i).getMatrix(mopMatrixFilePath);
                                        if(mopMatrixValue != ""){%>
                                        <div class ="document_section">
                                            <strong>Molecular Processes Ontology</strong>
                                            <div id="bar-chart-<%=Integer.toString(i)%>-mop" class="bar-chart" data-counts="<%=mopMatrixValue%>"></div>
                                        </div>
                                        <%}
                                    }if(documents.get(i).getShowMatrix(i, filePath, "physics-matrix")){
                                        String physicsMatrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-physics-matrix.csv";
                                        String physicsMatrixValue = documents.get(i).getMatrix(physicsMatrixFilePath);
                                        if(physicsMatrixValue != ""){%>
                                        <div class ="document_section">
                                            <strong>Physics Ontology</strong>
                                            <div id="bar-chart-<%=Integer.toString(i)%>-physics" class="bar-chart" data-counts="<%=physicsMatrixValue%>"></div>
                                        </div>
                                        <%}
                                    }if(documents.get(i).getShowMatrix(i, filePath, "cell-matrix")){
                                        String cellMatrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-cl-matrix.csv";
                                        String cellMatrixValue = documents.get(i).getMatrix(cellMatrixFilePath);
                                        if(cellMatrixValue != ""){%>
                                        <div class ="document_section">
                                            <strong>Cell Ontology</strong>
                                            <div id="bar-chart-<%=Integer.toString(i)%>-cell" class="bar-chart" data-counts="<%=cellMatrixValue%>"></div>
                                        </div>
                                        <%}
                                    }if(documents.get(i).getShowMatrix(i, filePath, "plant-matrix")){
                                        String plantMatrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-po-matrix.csv";
                                        String plantMatrixValue = documents.get(i).getMatrix(plantMatrixFilePath);
                                        if(plantMatrixValue != ""){%>
                                        <div class ="document_section">
                                            <strong>Plant Ontology</strong>
                                            <div id="bar-chart-<%=Integer.toString(i)%>-plant" class="bar-chart" data-counts="<%=plantMatrixValue%>"></div>
                                        </div>
                                        <%}
                                    }%>
                                </div>
                            </div>
                        <%
                            }
                        %>
                    </div>
                </div>
                <div class="ont_tab tab_content hidden">
                    <div id="tooltip"></div>
                    <%if(documentCollection.doesMatrixExist(filePath, "rxno-matrix")){%>
                        <strong>Reactions Ontology</strong>
                        <div id="rxno-matrix"></div>
                    <%}if(documentCollection.doesMatrixExist(filePath, "chmo-matrix")){%>
                        <strong>Chemical Methods Ontology</strong>
                        <div id="chmo-matrix"></div>
                    <%}if(documentCollection.doesMatrixExist(filePath, "mop-matrix")){%>
                        <strong>Molecular Processes Ontology</strong>
                        <div id="mop-matrix"></div>
                    <%}if(documentCollection.doesMatrixExist(filePath, "physics-matrix")){%>
                        <strong>Physics Ontology</strong>
                        <div id="physics-matrix"></div>
                    <%}if(documentCollection.doesMatrixExist(filePath, "cl-matrix")){%>
                        <strong>Cell Ontology</strong>
                        <div id="cl-matrix"></div>
                    <%}if(documentCollection.doesMatrixExist(filePath, "po-matrix")){%>
                        <strong>Plant Ontology</strong>
                        <div id="po-matrix"></div>
                    <%}%>
                </div>
            </div>
        </div>
  </body>
</html>




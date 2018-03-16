/**
 * Created by samikanza on 18/04/2017.
 */

package com.semantic_markup;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.*;

/**
 * Created by samikanza on 31/03/2017.
 */
public class ELNDocument implements Comparable<ELNDocument>{

    //File Reader Variables
    private BufferedReader br = null;
    private FileReader fr = null;

    private String sFileContent = "";
    private String sUnmarkedContent = "";
    private String title = "";
    private String textTitle = "";
    private String writeFileTitle = "";
    private String rxno_matrixFileTitle = "-rxno-matrix.csv";
    private String chmo_matrixFileTitle = "-chmo-matrix.csv";
    private String mop_matrixFileTitle = "-mop-matrix.csv";
    private String physics_matrixFileTitle = "-physics-matrix.csv";
    private String cl_matrixFileTitle = "-cl-matrix.csv";
    private String po_matrixFileTitle = "-po-matrix.csv";
    private ArrayList<String> authors = new ArrayList<String>();
    private String date;
    private int wordCount;
    private int titleCount;
    private int docPosition;

    //html markup with tooltips
    private String sFileMarkup = "";

    //Semantic Web Variables
    private ArrayList<OntClass> semanticTags = new ArrayList<>();
    private ArrayList<String> semanticLabels = new ArrayList<>();
    private HashMap<String, Double> ontologyTags = new HashMap<>();
    private HashMap<String, String> semanticHash = new HashMap<>();
    private HashMap ontoKey = new HashMap<String, String>();

    //Gate Document
    private String gateDocument = "";
    private String gateMarkup = "";
    private HashMap<String, Double> gateTags = new HashMap<>();
    private ArrayList<String> gateCompounds = new ArrayList<>();
    private ArrayList<String> gateElements = new ArrayList<>();

    //Open Calais
    private HashMap<String, Integer> openCalaisTags = new HashMap<>();
    private String openCalaisMarkup = "";

    //Chem Tagger
    private nu.xom.Document chemTaggerDocument;
    private String chemTaggerMarkup = "";
    private HashMap<String, Double> chemTaggerTags = new HashMap<>();
    private ArrayList<String> chemTaggerActions;

    //Tag Weightings
    private ArrayList<String> importance1Tags = new ArrayList<>();
    private ArrayList<String> importance2Tags = new ArrayList<>();
    private ArrayList<String> importance3Tags = new ArrayList<>();

    private Double searchScore;

    public ELNDocument(String fileName, String title, ArrayList<String> authors, String date) {
        this.title = title;
        this.writeFileTitle = "markup/" + title + "-markup.txt";
        this.authors = authors;
        this.date = date;

        if (fileName.contains(".txt")) {
            try {
                fr = new FileReader(fileName);
                br = new BufferedReader(fr);
            } catch (FileNotFoundException e) {
                System.out.println(e.getStackTrace());
            }
            readTextFile();
        } else if (fileName.contains(".pdf")) {
            try {
                PdfReader reader = new PdfReader(fileName);
                int noPages = reader.getNumberOfPages();
                System.out.println(noPages);
                for (int i = 0; i < noPages; i++) {
                    String newPage = PdfTextExtractor.getTextFromPage(reader, i + 1);
                    newPage = newPage.replaceAll("\u0000", "");
                    sFileContent += newPage;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String tempString = sFileContent;
        tempString = tempString.replaceAll("/.,;:", "");
        wordCount = tempString.split("\\s+").length;
        createTextTitle();
    }

    /*********** Semantic Methods ***********/
    public void copyHash(HashMap<String, String> keys) {
        if (ontoKey.isEmpty()) {
            ontoKey = keys;
        } else {
            ontoKey.putAll(keys);
        }
    }

    private static boolean isContain(String source, String searchTerm){
        String pattern = "\\b"+searchTerm+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(source);
        return m.find();
    }

    // creates ArrayList of OntClasses that exist in this ELNDocument
    public void addSemanticTags(OntModel model, ArrayList<OntClass> ontoLabels) {
        for (int i = 0; i < ontoLabels.size(); i++) {
            OntClass thisClass = ontoLabels.get(i);
            String ontoPhrase = thisClass.getLabel("").toLowerCase().trim();
            if (isContain(sFileContent.toLowerCase(), ontoPhrase)) {
                if (!semanticTags.contains(ontoPhrase)) {
                    semanticTags.add(thisClass);
                    semanticLabels.add(ontoPhrase);
                    int noSuperClasses = ontoLabels.get(i).listSuperClasses().toList().size();
                    ontologyTags.put(ontoPhrase, getTagWeight(ontoPhrase, true, noSuperClasses));
                }
            }
        }
    }

    //Create HashMap of tags to strip if a superclass/subclass clash occurs
    public void stripSemanticTags() {
        if (semanticTags.size() > 0) {
            for (int i = 0; i < semanticTags.size(); i++) {
                for (int j = 0; j < semanticTags.size(); j++) {
                    if (semanticTags.get(i) != semanticTags.get(j)) {
                        String semanticI = semanticTags.get(i).getLabel("").trim().toLowerCase();
                        String semanticJ = semanticTags.get(j).getLabel("").trim().toLowerCase();
                        if (semanticTags.get(i).hasSubClass(semanticTags.get(j))) {
                            semanticHash.put(semanticI, semanticJ);
                        } else if (semanticTags.get(i).getLabel("").trim().toLowerCase().contains(semanticTags.get(j).getLabel("").trim().toLowerCase())) {
                            if (!semanticTags.get(i).hasSubClass(semanticTags.get(j)) && !semanticTags.get(j).hasSubClass(semanticTags.get(i))) {
                                semanticHash.put(semanticJ, semanticI);
                            }
                        }
                    }
                }
            }
        }
    }

    /*********** Web Methods ***********/
    public void createOntologyMarkup() {
        for (int i = 0; i < semanticLabels.size(); i++) {
            String semanticLabel = "(?i)" + "[\\.,\\s!;?:\"]+" + semanticLabels.get(i) + "[\\.,\\s!;?:\"]+";
            sFileMarkup = sFileMarkup.replaceAll(semanticLabel, " " + semanticLabels.get(i).toUpperCase() + " ");
        }

        //add auto markup for the subclasses of the tags
        Object[] semanticSet = semanticHash.values().toArray();
        for (int i = 0; i < semanticSet.length; i++) {
            sFileMarkup = sFileMarkup.replace(semanticSet[i].toString().toUpperCase(), " <div class='tooltip ontology-tooltip' data-markup='ontologies'>" + semanticSet[i].toString() + "<span class='tooltiptext'>" + ontoKey.get(semanticSet[i]) + "</span></div> ");
        }
        for (int i = 0; i < semanticLabels.size(); i++) {
            if (!semanticHash.containsValue(semanticLabels.get(i))) {
                sFileMarkup = sFileMarkup.replace(semanticLabels.get(i).toString().toUpperCase(), " <div class='tooltip ontology-tooltip'  data-markup='ontologies'>" + semanticLabels.get(i).toString() + "<span class='tooltiptext'>" + ontoKey.get(semanticLabels.get(i).toString()) + "</span></div> ");
            }
        }

        sFileMarkup = sFileMarkup.replaceAll(" ,", ",");
        sFileMarkup = sFileMarkup.replaceAll(" \\.", ".");
        sFileMarkup = sFileMarkup.replaceAll("\\( ", "(");
        sFileMarkup = sFileMarkup.replaceAll(" \\)", ")");
        sFileMarkup = sFileMarkup.replaceAll("&lt;/br&gt; ", "\\</br>");
        sFileMarkup = sFileMarkup.replaceAll("&lt;/br&gt;", "\\</br>");
        sFileMarkup = sFileMarkup.replaceAll("&lt;/br&gt", "\\</br>");
    }

    public HashMap<String, Double> getAllTags(){
        HashMap<String, Double> allTags = new HashMap<String, Double>();

        allTags.putAll(gateTags);
        allTags.putAll(chemTaggerTags);
        allTags.putAll(ontologyTags);

        return allTags;
    }


    public String getTagsDouble(HashMap<String, Double> tags) {
        String tagCSV = "";

        List<String> tagsArr = new ArrayList<String>(tags.keySet());
        Collections.sort(tagsArr, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Double tf1 = tags.get(s1);
                Double tf2 = tags.get(s2);
                return tf2.compareTo(tf1);
            }
        });

        for(int i = 0; i < tagsArr.size(); i++){
            String currTag = tagsArr.get(i);

            tagCSV += currTag.toString() + ":" + tags.get(currTag).toString();

            if(i+1 < tagsArr.size()){
                tagCSV += ", ";
            }
        }
        return tagCSV;
    }

    public String getTagsDoubleForWeb(HashMap<String, Double> tags) {
        String tagCSV = "";

        List<String> tagsArr = new ArrayList<String>(tags.keySet());
        Collections.sort(tagsArr, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Double tf1 = tags.get(s1);
                Double tf2 = tags.get(s2);
                return tf2.compareTo(tf1);
            }
        });

        for(int i = 0; i < tagsArr.size(); i++){
            String currTag = tagsArr.get(i);

            tagCSV += currTag.toString();

            if(i+1 < tagsArr.size()){
                tagCSV += ", ";
            }
        }
        return tagCSV;
    }

    public String getTagsInt(HashMap<String, Integer> tags) {
        String tagCSV = "";

        List<String> tagsArr = new ArrayList<String>(tags.keySet());
        Collections.sort(tagsArr, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Integer tf1 = tags.get(s1);
                Integer tf2 = tags.get(s2);
                return tf2.compareTo(tf1);
            }
        });

        for(int i = 0; i < tagsArr.size(); i++){
            String currTag = tagsArr.get(i);

            tagCSV += currTag.toString() + ":" + tags.get(currTag).toString();

            if(i+1 < tagsArr.size()){
                tagCSV += ", ";
            }
        }
        return tagCSV;
    }

    public String getTagsIntForWeb(HashMap<String, Integer> tags) {
        String tagCSV = "";

        List<String> tagsArr = new ArrayList<String>(tags.keySet());
        Collections.sort(tagsArr, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Integer tf1 = tags.get(s1);
                Integer tf2 = tags.get(s2);
                return tf2.compareTo(tf1);
            }
        });

        for(int i = 0; i < tagsArr.size(); i++){
            String currTag = tagsArr.get(i);

            tagCSV += currTag.toString();

            if(i+1 < tagsArr.size()){
                tagCSV += ", ";
            }
        }
        return tagCSV;
    }


    public Double getTagWeight(String tag, boolean ontology, int noSuperClasses) {
        Double doc_matches = new Double(StringUtils.countMatches(sFileContent.toLowerCase(), tag.toLowerCase()));
        Double title_matches = new Double(StringUtils.countMatches(title.toLowerCase(), tag.toLowerCase()));
        Double doc_prop = doc_matches / wordCount;
        Double title_prop = title_matches / titleCount;
        Double score = doc_prop + title_prop;
        return score;
    }

    /*********** Text File Reader Methods ***********/
    private Boolean readyToRead() {
        try {
            return br.ready();
        } catch (IOException e) {
            return false;
        }
    }

    private void readTextFile() {
        while (readyToRead() == true) {
            try {
                String newLine = br.readLine();
                sFileContent += (newLine + "</br>");
                sUnmarkedContent += (newLine + "</br>");
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    public void writeTextFile() {
        try {
            /****
             * 1 - Word Count
             * 2 - Title Count
             * 3 - Title
             * 4 - OpenCalais Tags
             * 5 - Gate Tags
             * 6 - Ontology Tags
             * 7 - ChemTagger Tags
             * 8 - File Content
             * 9 - Markup
             */
            PrintWriter writer = new PrintWriter(writeFileTitle, "UTF-8");
            writer.println(getWordCount());
            writer.println("<splitter>");
            writer.println(getTitleCount());
            writer.println("<splitter>");
            writer.println(getTextTitle());
            writer.println("<splitter>");
            writer.println(getTagsInt(openCalaisTags));
            writer.println("<splitter>");
            writer.println(getTagsDouble(gateTags));
            writer.println("<splitter>");
            writer.println(getTagsDouble(ontologyTags));
            writer.println("<splitter>");
            writer.println(getTagsDouble(chemTaggerTags));
            writer.println("<splitter>");
            writer.println(getUnmarkedContent());
            writer.println("<splitter>");
            writer.println(getOntologyMarkup());
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

    public boolean needsProcessing() {
        boolean returnVal;
        File markupFile = new File(writeFileTitle);
        File actualFile = new File(title);
        if (markupFile.exists() && actualFile.exists()) {
            Date lastModifiedMarkupDate = new Date(markupFile.lastModified());
            Date lastModifiedActualDate = new Date(actualFile.lastModified());

            //if markup file has been modified before real file, assume real file has changed therefore needs to be re-submitted through the system
            if (lastModifiedMarkupDate.before(lastModifiedActualDate)) {
                returnVal = true;
            } else {
                returnVal = false;
            }
        } else {
            returnVal = false;
        }

        return returnVal;
    }

    public void createMatrixFile(String fileTitle, ArrayList<String> ontTags) {
        PrintWriter writer;
        int count;
        int termNo = 0;
        String fileLine = "term,count\n";
        try {
            for (String tag : ontTags) {
                count = StringUtils.countMatches(sFileContent.toLowerCase(), tag.toLowerCase());
                if(count > 0){
                    fileLine += (tag + "," + Integer.toString(count) + "\n");
                    termNo++;
                }
            }
            if(termNo > 0){
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

    public void createRxnoMatrix(int docNo, ArrayList<String> ontTags) {
        String fileTitle = "matricies/document-" + Integer.toString(docNo) + rxno_matrixFileTitle;
        createMatrixFile(fileTitle, ontTags);
    }


    public void createChmoMatrix(int docNo, ArrayList<String> ontTags) {
        String fileTitle = "matricies/document-" + Integer.toString(docNo) + chmo_matrixFileTitle;
        createMatrixFile(fileTitle, ontTags);
    }

    public void createMopMatrix(int docNo, ArrayList<String> ontTags) {
        String fileTitle = "matricies/document-" + Integer.toString(docNo) + mop_matrixFileTitle;
        createMatrixFile(fileTitle, ontTags);
    }

    public void createPhysicsMatrix(int docNo, ArrayList<String> ontTags) {
        String fileTitle = "matricies/document-" + Integer.toString(docNo) + physics_matrixFileTitle;
        createMatrixFile(fileTitle, ontTags);
    }


    public void createCellMatrix(int docNo, ArrayList<String> ontTags) {
        String fileTitle = "matricies/document-" + Integer.toString(docNo) + cl_matrixFileTitle;
        createMatrixFile(fileTitle, ontTags);
    }

    public void createPlantMatrix(int docNo, ArrayList<String> ontTags) {
        String fileTitle = "matricies/document-" + Integer.toString(docNo) + po_matrixFileTitle;
        createMatrixFile(fileTitle, ontTags);
    }

    public void readSavedFile() {
        String saveContent = "";
        try {
            FileReader fr = new FileReader(writeFileTitle);
            br = new BufferedReader(fr);

            while (br.ready()) {
                saveContent += br.readLine();
            }

            String[] saveds = saveContent.split("<splitter>");

            if (saveds.length == 9) {
                if (saveds[0].trim() != "") {
                    setWordCount(Integer.parseInt(saveds[0].trim()));
                }
                if (saveds[1].trim() != "") {
                    setTitleCount(Integer.parseInt(saveds[1].trim()));
                }
                if (saveds[2].trim() != "") {
                    setTextTitle(saveds[2].trim());
                }
                if (saveds[3].trim() != "") {
                    setTagsInt(openCalaisTags, saveds[3].trim().split(", "));
                }
                if (saveds[4].trim() != "") {
                    setTagsDouble(gateTags, saveds[4].trim().split(", "));
                }
                if (saveds[5].trim() != "") {
                    setTagsDouble(ontologyTags, saveds[5].trim().split(", "));
                }
                if (saveds[6].trim() != "") {
                    setTagsDouble(chemTaggerTags, saveds[6].trim().split(", "));
                }
                if (saveds[7].trim() != "") {
                    setUnmarkedContent(saveds[7]);
                }
                if (saveds[8].trim() != "") {
                    setOntologyMarkup(saveds[8].trim());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFilePath(String filePath) {
        writeFileTitle = filePath + writeFileTitle;
    }

    public void setTagsInt(HashMap<String, Integer> tags, String[] tagsString) {
        for (int i = 0; i < tagsString.length; i++) {
            if (tagsString[i].trim() != "" && tagsString[i].trim().contains(":")) {
                String[] tagSplit = tagsString[i].split(":");
                tags.put(tagSplit[0], Integer.parseInt(tagSplit[tagSplit.length - 1]));
            }
        }
    }

    public void setTagsDouble(HashMap<String, Double> tags, String[] tagsString) {
        for (int i = 0; i < tagsString.length; i++) {
            if (tagsString[i].trim() != "" && tagsString[i].trim().contains(":")) {
                String[] tagSplit = tagsString[i].split(":");
                tags.put(tagSplit[0], Double.parseDouble(tagSplit[tagSplit.length - 1]));
            }
        }
    }


    /*********** Getters and Setters ***********/
    public String getFileContent() {
        return sFileContent;
    }

    public String getUnmarkedContent() {
        return sUnmarkedContent;
    }

    public void setUnmarkedContent(String sUnmarkedContent) {
        this.sUnmarkedContent = sUnmarkedContent;
    }

    public String getOntologyMarkup() {
        return sFileMarkup;
    }

    public void setOntologyMarkup(String ontologyMarkup) {
        sFileMarkup = ontologyMarkup;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public String getDate() {
        return date;
    }

    public void setChemTaggerDocument(nu.xom.Document chemTaggerDocument) {
        this.chemTaggerDocument = chemTaggerDocument;
    }

    public nu.xom.Document getChemTaggerDocument() {
        return chemTaggerDocument;
    }

    public void addGateMarkup() {

        for (String element : gateElements) {
            String match1 = "<div class=' tooltip gate-tooltip' data-markup='gate' data-category='chemicalElement'>" + element + "<span  class='tooltiptext'>Chemical Element</span></div>";
            String regex1 = " " + element + " ";
            String regex2 = ">" + element + "<";
            String regex3 = element + ".";
            String regex4 = element + ",";
            if(element.length() > 2 && (sFileMarkup.contains(regex1) || sFileMarkup.contains(regex2) || sFileMarkup.contains(regex3) || sFileMarkup.contains(regex4)) && !sFileMarkup.contains(match1) && !sFileMarkup.contains(match1.toLowerCase())) {
                sFileMarkup = sFileMarkup.replaceAll(element, match1);
            }
        }

        for (String compound : gateCompounds) {
            String match2 = "<div class=' tooltip gate-tooltip' data-markup='gate' data-category='chemicalCompound'>" + compound + "<span  class='tooltiptext'>Chemical Compound</span></div>";
            String regex1 = " " + compound + " ";
            String regex2 = ">" + compound + "<";
            String regex3 = compound + ".";
            String regex4 = compound + ",";
            if(compound.length() > 2 && (sFileMarkup.contains(regex1) || sFileMarkup.contains(regex2) || sFileMarkup.contains(regex3) || sFileMarkup.contains(regex4)) && !sFileMarkup.contains(compound) && !sFileMarkup.contains(match2.toLowerCase())) {
                sFileMarkup = sFileMarkup.replaceAll(compound, match2);
            }
        }

    }

    public String getGateDocument() {
        return gateDocument;
    }

    public void setGateMarkup(String gateMarkup) {
        this.gateMarkup = gateMarkup;
        //sFileMarkup = gateMarkup;
    }

    public String getGateMarkup() {
        return sFileMarkup;
    }

    public void setOpenCalaisMarkup(String openCalaisMarkup) {
        this.openCalaisMarkup = openCalaisMarkup;
    }

    public void setOpenCalaisTags(HashMap<String, Integer> openCalaisTags) {
        this.openCalaisTags = openCalaisTags;
    }

    public String getOntologyTags() {
        return getTagsDouble(ontologyTags);
    }

    public String getOntologyTagsForWeb() {
        return getTagsDoubleForWeb(ontologyTags);
    }

    public String getAllTagsForWeb(){
        return getTagsDoubleForWeb(getAllTags());
    }

    public TreeMap<String,Integer> getOntologyTagsAsHash(){
        Map<String,Double> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(ontologyTags);
        return (TreeMap) map;
    }

    public Set<String> getOntologyKeys() {
        return ontologyTags.keySet();
    }

    public String getOpenCalaisTags() {
        return getTagsInt(openCalaisTags);
    }

    public String getOpenCalaisTagsForWeb() {
        return getTagsIntForWeb(openCalaisTags);
    }

    public TreeMap<String,Integer> getOpenCalaisTagsAsHash(){
        Map<String,Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(openCalaisTags);
        return (TreeMap) map;
    }

    public String getGateTags() {
        return getTagsDouble(gateTags);
    }

    public String getGateTagsForWeb() {
        return getTagsDoubleForWeb(gateTags);
    }

    public Set<String> getGateTagsAsArray(){
        return gateTags.keySet();
    }

    public TreeMap<String,Integer> getGateTagsAsHash(){
        Map<String,Double> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(gateTags);
        return (TreeMap) map;
    }

    public void setGateTags(HashMap<String, Double> gateTags) {
        this.gateTags = gateTags;
    }

    public void setGateCompounds(ArrayList<String> gateCompounds) {
        this.gateCompounds = gateCompounds;
    }

    public void setGateElements(ArrayList<String> gateElements) {
        this.gateElements = gateElements;
    }

    public String getChemTaggerTags() {
        return getTagsDouble(chemTaggerTags);
    }

    public String getChemTaggerTagsForWeb() {
        return getTagsDoubleForWeb(chemTaggerTags);
    }

    public Set<String> getChemTaggerTagsAsArray(){
        return chemTaggerTags.keySet();
    }

    public TreeMap<String,Integer> getChemTaggerTagsAsHash(){
        Map<String,Double> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(chemTaggerTags);
        return (TreeMap) map;
    }

    public void setChemTaggerTags(HashMap<String, Double> chemTaggerTags) {
        this.chemTaggerTags = chemTaggerTags;
    }

    public void setChemTaggerActions(ArrayList<String> chemTaggerActions) {
        this.chemTaggerActions = chemTaggerActions;
    }

    public void setChemTaggerMarkup(String chemTaggerMarkup) {
        this.chemTaggerMarkup = chemTaggerMarkup;
        sFileMarkup = chemTaggerMarkup;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getTitleCount(){return titleCount;}

    public void setTitleCount(int titleCount){this.titleCount = titleCount;}

    public String getChemTaggerMarkup() {
        return chemTaggerMarkup;
    }

    public String getImportanceLevel1Tags() {
        return getImportanceLevelTags(importance1Tags);
    }

    public String getImportanceLevel2Tags() {
        return getImportanceLevelTags(importance2Tags);
    }

    public String getImportanceLevel3Tags() {
        return getImportanceLevelTags(importance3Tags);
    }

    public String getImportanceLevelTags(ArrayList<String> tagArray) {
        String tagString = "";
        for (int i = 0; i < tagArray.size(); i++) {
            tagString += tagArray.get(i);

            if (i + 1 < tagArray.size()) {
                tagString += ", ";
            }
        }
        return tagString;
    }

    public boolean getShowMatrix(int i, String filePath, String matrix) {
        boolean matrixExists = false;
        String matrixFilePath = filePath + "matricies/document-" + Integer.toString(i) + "-" + matrix + ".csv";
        File matrixFile = new File(matrixFilePath);
        if(matrixFile.exists()){
            matrixExists = true;
        }
        return matrixExists;
    }

    public String getMatrix(String filePath){
        String fileResponse = "";
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            while (br.ready()) {
                fileResponse += (br.readLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileResponse;
    }

    public void setSearchScore(Double searchScore){
        this.searchScore = searchScore;
    }

    public Double getSearchScore(){
        return searchScore;
    }

    @Override
    public int compareTo(ELNDocument document) {
        int compareVal = 0;
        Double score = document.getSearchScore();

        if(this.getSearchScore() > score){
            compareVal = -1;
        }else if(this.getSearchScore() < score){
            compareVal = 1;
        }

        return compareVal;
    }

    public int getDocPosition(){
        return docPosition;
    }

    public void setDocPosition(int docPosition){
        this.docPosition = docPosition;
    }

    public void createTextTitle(){
        if(title.contains("_")) {
            textTitle = title.replaceAll("_", " ");
        }else{
            textTitle = title;
        }
        textTitle = textTitle.split(".txt")[0];
        titleCount = textTitle.split("\\s+").length;
    }

    public void setTextTitle(String textTitle){
        this.textTitle = textTitle;
    }

    public String getTextTitle(){
        return textTitle;
    }
}

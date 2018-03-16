<%@ page import="org.apache.commons.io.FileUtils" %><%--
  Created by IntelliJ IDEA.
  User: samikanza
  Date: 16/04/2017
  Time: 15:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <title>Drive API Quickstart</title>
    <meta charset='utf-8' />
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <link rel="icon" href="images/favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" type="text/css" href="css/index.css">
</head>
<body>
<div class="main">
    <div class="top_bar">
        <div class="top_left"><span>SEM<span id="header_cat">A</span>NTI-C</span><span id="header_cat">A</span><span>T</span></div>
        <div class="top_right">
            <button class="top-button left-button" id="authorize-button" style="display: none;">Authorize</button>
            <button class="top-button right-button" id="signout-button" style="display: none;">Sign Out</button>
        </div>
    </div>
    <div class="document_list">
        <form id="file_form" action="${pageContext.request.contextPath}/myservlet" method="post">
            <button class="google-button" type="submit" name="button" value="btn-download">Download Files</button>
            <div id="checkbox-div"></div>
        </form>
    </div>
</div>

<script async defer src="js/api.js"
        onload="this.onload=function(){};handleClientLoad()"
        onreadystatechange="if (this.readyState === 'complete') this.onload()">
</script>
<script type="text/javascript" src="js/semantic_eln.js"></script>
<script>


</script>
</body>
</html>




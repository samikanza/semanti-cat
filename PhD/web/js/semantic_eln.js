var CLIENT_ID = '354319147106-69dq099t71jcgtrgvopt0f8gd18s2dts.apps.googleusercontent.com';
var SCOPES = 'https://www.googleapis.com/auth/drive.metadata.readonly';
var API_KEY = 'AIzaSyBBGgmwPsOtzn0jsFmdnIjTa0T99yFqGbA';
var DISCOVERY_DOCS = ["https://www.googleapis.com/discovery/v1/apis/drive/v3/rest"];
var authorizeButton = $("#authorize-button");
var signoutButton = $("#signout-button");

$(document).ready(function(){

    authorizeButton.click(function(){
        handleAuthClick(this);
    });

    signoutButton.click(function(){
        handleSignoutClick(this);
    });
});

function linkCheckboxes(){
    $("#cbx-all").change(function(){
        console.log(this.checked);
        if(this.checked) {
            $('.cbx-file').attr('checked','checked');
        }else{
            $('.cbx-file').removeAttr('checked');
        }
    });
}

/**
 *  On load, called to load the auth2 library and API client library.
 */
function handleClientLoad() {
    gapi.load('client:auth2', initClient);
}

/**
 *  Initializes the API client library and sets up sign-in state
 *  listeners.
 */
function initClient() {
    gapi.client.init({
        apiKey: API_KEY,
        discoveryDocs: DISCOVERY_DOCS,
        clientId: CLIENT_ID,
        scope: SCOPES
    }).then(function () {
        // Listen for sign-in state changes.
        gapi.auth2.getAuthInstance().isSignedIn.listen(updateSigninStatus);

        // Handle the initial sign-in state.
        updateSigninStatus(gapi.auth2.getAuthInstance().isSignedIn.get());
    });
}

function updateSigninStatus(isSignedIn) {
    // When signin status changes, this function is called.
    // If the signin status is changed to signedIn, we make an API call.
    if (isSignedIn) {
        authorizeButton.css({"display" : "none"});
        signoutButton.css({"display" : "block"});
        listFiles();
    } else {
        authorizeButton.css({"display" : "block"});
        signoutButton.css({"display" : "none"});
    }
}

/**
 *  Sign in the user upon button click.
 */
function handleAuthClick(event) {
    gapi.auth2.getAuthInstance().signIn();
}


/**
 *  Sign out the user upon button click.
 */
function handleSignoutClick(event) {
    gapi.auth2.getAuthInstance().signOut();
}

/**
 * Append a pre element to the body containing the given message
 * as its text node. Used to display the results of the API call.
 *
 * @param {string} message Text to be placed in pre element.
 */
function appendForm(html) {
    var form = $('#checkbox-div');
    form.append(html);
}

function makeApiCall() {
    // Make an API call to the People API, and print the user's given name.
    gapi.client.people.people.get({
        resourceName: 'people/me'
    }).then(function(response) {
        console.log('Hello, ' + response.result.names[0].givenName);
    }, function(reason) {
        console.log('Error: ' + reason.result.error.message);
    });
}

/**
 * Check if current user has authorized this application.
 */
function checkAuth() {
    gapi.auth.authorize(
        {
            'client_id': CLIENT_ID,
            'scope': SCOPES.join(' '),
            'immediate': true
        }, handleAuthResult);
}

/**
 * Print files.
 */
function listFiles() {
    gapi.client.drive.files.list({
        'fields': "nextPageToken, files(id, name)",
        'q' : "mimeType='application/vnd.google-apps.document'"
    }).then(function(response) {

        var fileHTML = ("<div><input type='checkbox' id='cbx-all' name='all-checkbox' value='Select All' checked/><span>Select All</span></div>");

        var files = response.result.files;
        if (files && files.length > 0) {
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                var file_export_url = "https://docs.google.com/document/d/" + file.id + "/export?format=txt";
                var file_value = file.name + "&url=" + file_export_url;
                fileHTML = fileHTML + ("<div><input type='checkbox' class='cbx-file' name='file-checkbox' value='" + file_value + "' checked/><span>" + file.name + "</span></div>");
            }
        }

        appendForm(fileHTML);
        linkCheckboxes();
    });
}
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" >
        <title>structr Async UI</title>
        <link rel="stylesheet" type="text/css" media="screen" href="css/main.css" >
        <meta name="viewport" content="width=1024, user-scalable=yes, target-densityDpi=device-dpi" />
		<meta name="apple-touch-fullscreen" content="YES" /> 
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7/jquery.min.js" type="text/javascript"></script>
        <script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
        <script src="http://jquery-json.googlecode.com/files/jquery.json-2.3.min.js"></script>
        <script src="js/jquery.tooltip.pack.js" type="text/javascript"></script>
        <script src="codemirror/lib/codemirror.js"></script>
        <link rel="stylesheet" href="codemirror/lib/codemirror.css">
        <link rel="stylesheet" href="codemirror/theme/default.css">
        <!--script src="codemirror/mode/htmlmixed/htmlmixed.js" type="text/javascript"></script-->
        <link rel="stylesheet" type="text/css" media="screen" href="codemirror/lib/codemirror.css" >
        <script src="js/websocket.js" type="text/javascript"></script>
        <script src="js/ce_lib.js" type="text/javascript"></script>
        <script src="js/entities.js" type="text/javascript"></script>
        <script src="js/jquery.blockUI.js" type="text/javascript"></script>
        <script src="js/jquery.cookie.js" type="text/javascript"></script>
        <script src="js/users_and_groups.js" type="text/javascript"></script>
        <script src="js/resources.js" type="text/javascript"></script>
        <script src="js/init.js" type="text/javascript"></script>
    </head>
    <body>
        <div id="header">
            <img class="logo" src="img/structr-logo.png">
            <div id="menu" class="menu">
                <ul>
                    <li><a id="dashboard_" href="#dashboard">Dashboard</a></li>
                    <li><a id="resources_" href="#resources">Resources</a></li>
                    <li><a id="usersAndGroups_" href="#usersAndGroups">Users and Groups</a></li>
                    <li><a id="logout_" href="#logout">Login</a></li>
                </ul>
            </div>
        </div>
        <div id="main">
            <!--      <textarea id="json_input" rows="10" cols="80">
                  </textarea>
                  <input type="button" id="import_json" value="Import JSON">-->
            <!--            <div style="clear: both"></div>-->
        </div>
        <div id="footer">
        </div>

        <div id="login">
            <table>
                <tr><td><label for="username">Username:</label></td><td><input id="usernameField" type="text" name="username"></input></td></tr>
                <tr><td><label for="password">Password:</label></td><td><input id="passwordField" type="password" name="password"></input></td></tr>
                <tr><td colspan="2" class="btn"><span id="errorText"></span></td></tr>
                <tr><td colspan="2" class="btn"><button id="loginButton" name="login"><img src="icon/key.png"> Login</button></td></tr>
            </table>
        </div>

        <div id="confirmation">
            <div id="confirmationText"></div>
            <button id="yesButton"><img src="icon/tick.png">Yes</button>
            <button id="noButton"><img src="icon/cross.png">No</button>
        </div>

    </body>
</html>
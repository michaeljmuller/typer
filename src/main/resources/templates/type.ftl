<html>
<head>
  <title>Typer</title>
  <script src="js/jquery-3.2.1.min.js"></script>
  <script src="js/typer.js"></script>
  <link rel="stylesheet" type="text/css" href="css/theme.css"></link>
  <meta name="_csrf" content="${_csrf.token}"/>
  <meta name="_csrf_header" content="${_csrf.headerName}"/></head>
<body>
<div id="header"><span id="logo">Typer</span><span id="user">${userFullName} <a href="/logout">logout</a></span></div>
<div id="workArea">
<div class="lines first" id="line0"><span id="match"></span><span id="bad"></span><span id="remainder"></span></div>
<#list 1..(numLines-1) as lineNum>
  <div class="lines" id="line${lineNum}"></div>
</#list>
<div id="spacer">&nbsp;</div>
<input id="input" autocomplete="off"></input>
</div>
<div id="stats">&nbsp;</div>
</body>
</html>
<html>
<head>
  <script src="js/jquery-3.2.1.min.js"></script>
  <script src="js/typer.js"></script>
  <link rel="stylesheet" type="text/css" href="css/theme.css"></link>
</head>
<body>
<div class="lines first" id="line0"><span id="match"></span><span id="bad"></span><span id="remainder"></span></div>
<#list 1..(numLines-1) as lineNum>
  <div class="lines" id="line${lineNum}"></div>
</#list>
<div id="spacer">&nbsp;</div>
<input id="input" autocomplete="off"></input>
</body>
</html>
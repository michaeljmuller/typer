$(document).ready(function() {
    $.ajax({
        url: "http://localhost:8080/s/text/1/10"
    }).then(function(data) {
        $('#remainder').text(data.lines[0]);
        for (var i = 1; i < data.lines.length; i++) {
            $('#line'+(i+1)).text(data.lines[(i)]);
        }
        localStorage.setItem('goalText', data.lines[0]);
        localStorage.setItem('numLines', data.lines.length);
        localStorage.setItem('atLine', 1);
        $('#input').focus();
    });
    
    $("#input").keyup(function(event) {
        var typed = $("#input").val();
        var goal = localStorage.getItem('goalText');
        
        for (var i = 0; i < goal.length && i < typed.length; i++) {
            if (goal[i] != typed[i]) {
                break;
            }
        }
        
        var matchText = goal.substring(0,i);
        var badText = goal.substring(i,typed.length);
        var remainder = goal.substring(typed.length, goal.length);

        if (typed.length > goal.length) {
            typed = typed.replace(/\s+$/,'');
            badText = badText + "****************".substring(0,typed.length - goal.length);
        }

        $("#match").text(matchText);
        $("#bad").text(badText);
        $("#remainder").text(remainder);
        
        if (event.which == 13 && typed.trim() == goal.trim()) {
            nextLine();
        }
    });
    
    function nextLine() {
        var numLines = Number(localStorage.getItem('numLines'));
        var nextLine = $('#line2').text();
        var atLine = Number(localStorage.getItem('atLine'));

        var linesToSkip = 1;
        if (nextLine.trim().length == 0) {
            linesToSkip = 2;
            nextLine = $('#line3').text();
        }
        
        atLine += linesToSkip;

        localStorage.setItem('atLine', atLine);
        localStorage.setItem('goalText', nextLine);

        $('#remainder').text(nextLine);
        for (var i = 2; i < numLines; i++) {
            $('#line'+i).text($('#line'+(i+linesToSkip)).text());
        }
        $('#match').text("");
        $('#bad').text("");
        $('#input').val("");
        
        $('#line'+numLines).text("");
        if (linesToSkip == 2) {
            $('#line'+(numLines-1)).text("");
        }        
        
        $.ajax({
            url: "http://localhost:8080/s/text/" + (atLine+numLines) + "/" + linesToSkip
        }).then(function(data) {
            if (data.lines.length == 1) {
                $('#line'+numLines).text(data.lines[0]);
            }
            else {
                $('#line'+(numLines-1)).text(data.lines[0]);
                $('#line'+numLines).text(data.lines[1]);
            }
        });
    }
});
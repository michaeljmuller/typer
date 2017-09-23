// entry point when the page has loaded
$(document).ready(function() {
	
	// get the text we're supposed to type (resuming from where we last left off)
    $.ajax({
        url: "http://localhost:8080/s/resume"
    }).then(function(data) {
    	
    	/*
    	the first line is broken into three parts: match, bad, and remainder
    	match = correctly typed (on the left)
    	bad = incorrectly typed (in the middle)
    	remainder = not yet typed (on the right)
    	*/
    	
    	// start with all of the first line in the "remainder" area 
        $('#remainder').text(data.lines[0]);
        
        // fill in the other "preview" lines of text (divs are numbered starting at 0)
        for (var i = 1; i < data.lines.length; i++) {
            $('#line'+i).text(data.lines[(i)]);
        }
        
        // store the current line of text to be entered
        localStorage.setItem('goalText', data.lines[0]);
        
        // store how many lines of text are displayed at at time
        localStorage.setItem('numLines', data.lines.length);
        
        // store the index of the line of text currently being typed
        localStorage.setItem('atLine', data.pos);
        
        // put the focus into the text box where you're supposed to type
        $('#input').focus();
    });

	// register handler for when someone types
	$("#input").keyup(function(event) {
		handleKeyPress(event.which)
	});
	
});

// called for each letter the user enters
function handleKeyPress(keyPressed) {
	
	// get the text that's been typed so far
    var typed = $("#input").val();
    
    // get what that text should look like
    var goal = localStorage.getItem('goalText');
    
    // walk through both strings until we find a mismatch
    for (var i = 0; i < goal.length && i < typed.length; i++) {
        if (goal[i] != typed[i]) {
            break;
        }
    }
    
    // find the part that was typed correctly
    var matchText = goal.substring(0,i);
    
    // find the part that was typed incorrectly
    var badText = goal.substring(i,typed.length);
    
    // find the part that hasn't been typed yet
    var remainder = goal.substring(typed.length, goal.length);

    // if the user kept typing past the end of the string, 
    // append asterisks so there's some indicator they messed up
    if (typed.length > goal.length) {
        typed = typed.replace(/\s+$/,'');
        badText = badText + "****************".substring(0,typed.length - goal.length);
    }

    // update the first line to indicate progress
    $("#match").text(matchText);
    $("#bad").text(badText);
    $("#remainder").text(remainder);

    // if the user hit return and typed the current line correctly 
    if (keyPressed == 13 && typed.trim() == goal.trim()) {
        nextLine();
    }
}

// advance to the next line of text
function nextLine() {
	
	// get the next line from second row of text
    var nextLine = $('#line1').text();
    
    // if the next line is blank, then get from the third row
    // (we assume the input has been cleaned so there's never 2 or more consecutive blank lines)
    var linesToSkip = 1;
    if (nextLine.trim().length == 0) {
        linesToSkip = 2;
        nextLine = $('#line2').text();
    }

    // save the new text that the user is supposed to type
    localStorage.setItem('goalText', nextLine);
    
    // set the new first line of text (put everything in the remainder div and clear out the other two)
    $('#remainder').text(nextLine);
    $('#match').text("");
    $('#bad').text("");

    // move the rest of the lines up
    var numLines = Number(localStorage.getItem('numLines'));
    for (var i = 1; i < numLines-linesToSkip; i++) {
        $('#line'+i).text($('#line'+(i+linesToSkip)).text());
    }
    
    // blank out the last line(s)
    for (var i = 0; i < linesToSkip; i++) {
	    $('#line'+(numLines-(i+1))).text("");
    }

    // blank out the text entry area
    $('#input').val("");
    
    // increment (and save) the line of text that the user is typing
    var wasAtLine = Number(localStorage.getItem('atLine'));
    var nowAtLine = wasAtLine + linesToSkip;
    localStorage.setItem('atLine', nowAtLine);
    
    // request text to fill in the blank space at the bottom
    $.ajax({
        url: "http://localhost:8080/s/text/" + (nowAtLine+(numLines-linesToSkip)) + "/" + linesToSkip 
    }).then(function(data) {
	    
	    // fill in the last line(s)
	    for (var i = 0; i < linesToSkip; i++) {
		    $('#line'+((numLines-linesToSkip)+i)).text(data.lines[i]);
	    }
    });
}

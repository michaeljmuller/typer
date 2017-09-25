var g;

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
        
        // when was the last keypress?  (start the timer)
        localStorage.setItem('lastKeypressTime', Date.now());
        
        // how much time has the user spent typing this line?  (no time, yet)
        localStorage.setItem('timeTyping', 0);
        
        // put the focus into the text box where you're supposed to type
        $('#input').focus();
    });

	// register handler for when someone types
	$("#input").keyup(function(event) {
		
		// programmatically setting focus seems to generate a keyup event for ascii 16. idk why, so let's just ignore it.
		if (event.which != 16) {
			handleKeyPress(event.which);
		}
	});
	
    g = new JustGage({
	    id: "graph",
	    value: 0,
	    min: 0,
	    max: 100,
	    title: "WPM"
	  });
  
	
	/*
	// draw something in the graph
	var TESTER = $('#graph').get(0);
	console.log(TESTER);
	Plotly.plot( TESTER, [{
	x: [1, 2, 3, 4, 5],
	y: [1, 2, 4, 8, 16] }], {
	margin: { t: 0 } } );
*/
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

	// how much time elapsed since the last time the user pressed a key?
	// ignore any time beyond 1.5 seconds; the user probably just stopped typing
	var now = Date.now();
	var additionalTime = Math.min(now - Number(localStorage.getItem('lastKeypressTime')), 1500);
	
	// reset the time of the last keypress to now
    localStorage.setItem('lastKeypressTime', now);
	
	// calculate how much time the user has spent typing this line of text
	var timeTyping = Number(localStorage.getItem('timeTyping')) + additionalTime;
	localStorage.setItem('timeTyping', timeTyping);
	
	// calculate WPM so far for this line of text
	var numWords = matchText.length / 5.0;
	var elapsedTimeInMinutes = (timeTyping / 1000.0) / 60.0;
	var wpm = numWords / elapsedTimeInMinutes;
	
	g.refresh(wpm);
	
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
    
    // reset the amount of time spent typing
    var timeTyping = localStorage.getItem('timeTyping');
    localStorage.setItem('timeTyping', 0.0);
    
    // determine which line of text we need the server to provide for us to fill in on the bottom
    var requestLineNumStart = nowAtLine+ (numLines - linesToSkip);

    // get csrf tokens we wrote to the page so we can safely post back 
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
    	method: "POST", 
    	url: "http://localhost:8080/s/lineComplete", 
    	beforeSend: function(xhr) {
    		xhr.setRequestHeader(header, token);
    	},
	    headers: { 
	        'Accept': 'application/json',
	        'Content-Type': 'application/json' 
	    },
	    data: JSON.stringify({
    	    wasAtLine: wasAtLine,
    	    nowAtLine: nowAtLine, 
    	    elapsedTime: timeTyping, 
    	    requestedLineNumStart: requestLineNumStart, 
    	    numLinesRequested: linesToSkip    	
    	}), 
    	datatype: 'json'
    }).then(function(response) {
	    
	    // fill in the last line(s)
	    for (var i = 0; i < linesToSkip; i++) {
		    $('#line'+((numLines-linesToSkip)+i)).text(response.lines[i]);
	    }
    });

}

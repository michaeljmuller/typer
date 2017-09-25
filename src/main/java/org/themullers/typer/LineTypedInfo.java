package org.themullers.typer;

public class LineTypedInfo {
    private int wasAtLine;
    private int nowAtLine;
    private int elapsedTime;
    private int requestedLineNumStart;
    private int numLinesRequested;
    
    public int getWasAtLine() {
        return wasAtLine;
    }
    public void setWasAtLine(int wasAtLine) {
        this.wasAtLine = wasAtLine;
    }
    public int getNowAtLine() {
        return nowAtLine;
    }
    public void setNowAtLine(int nowAtLine) {
        this.nowAtLine = nowAtLine;
    }
    public int getElapsedTime() {
        return elapsedTime;
    }
    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    public int getRequestedLineNumStart() {
        return requestedLineNumStart;
    }
    public void setRequestedLineNumStart(int requestedLineNumStart) {
        this.requestedLineNumStart = requestedLineNumStart;
    }
    public int getNumLinesRequested() {
        return numLinesRequested;
    }
    public void setNumLinesRequested(int numLinesRequested) {
        this.numLinesRequested = numLinesRequested;
    }
}

package org.themullers.typer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.themullers.typer.textsource.Book;

@RestController
public class Service {
    
    private static final int NUM_LINES = 10;
    
    private static List<String> lines = null;
    
    private TextPreparationService textPrepSvc;
    
    @Autowired
    public void setTextPreparationService(TextPreparationService svc) {
        this.textPrepSvc = svc;
    }
    
    @PostConstruct
    public void init() {
        Book epub = new Book(findResourceFile("p-g-wodehouse_right-ho-jeeves.epub"));
        lines = textPrepSvc.toLines(epub.toString());
    }
    
    @RequestMapping("/s/text/{start}/{numLines}")
    TextData text(@PathVariable("start") int start, @PathVariable("numLines") int numLines) {
        start = start-1; // caller is 1-based, convert to 0-based
        TextData td = new TextData();
        populateText(td, start, numLines);
        return td;
    }
    
    @RequestMapping("/s/resume")
    ResumeData resume() {
        ResumeData rd = new ResumeData();
        int resumeAtLine = 1;
        rd.setPos(resumeAtLine);
        populateText(rd, resumeAtLine, NUM_LINES);
        return rd;
    }
    
    @RequestMapping("/s/lineComplete")
    LineCompleteResponse lineComplete(@RequestParam(value="lineNum") int lineNum) {
        
        return new LineCompleteResponse();
    }
    
    protected void populateText(TextData td, int start, int numLines) {
        List<String> response = new LinkedList<>();
        for (int i = start; i < start+numLines; i++) {
            response.add(lines.get(i));
        }
        td.setLines(response);
    }

     protected File findResourceFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
        
}

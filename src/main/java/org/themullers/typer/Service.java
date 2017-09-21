package org.themullers.typer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

@RestController
public class Service {
    
    public static final String EXPECTED_MEDIA_TYPE = "application/xhtml+xml";
    
    private static final String UNICODE_LEFT_DOUBLE_QUOTE = "\u201C";
    private static final String UNICODE_RIGHT_DOUBLE_QUOTE = "\u201D";
    private static final String UNICODE_RIGHT_SINGLE_QUOTE = "\u2019";
    private static final String UNICODE_EM_DASH = "\u2014";
    private static final String UNICODE_TWO_EM_DASH = "\u2E3A";
    private static final String UNICODE_THREE_EM_DASH = "\u2E3B";

    private static final Set<Character> OKAY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`1234567890-=~!@#$%^&*()_+[]\\;':\",./<>? \n".chars().mapToObj(i->Character.valueOf((char)i)).collect(Collectors.toSet());
    
    private static List<String> text = null;
    
    @RequestMapping("/s/text/{start}/{numLines}")
    TextData text(@PathVariable("start") int start, @PathVariable("numLines") long numLines) {
        start = start-1; // caller is 1-based, convert to 0-based
        List<String> lines = getLines();
        TextData td = new TextData();
        List<String> response = new LinkedList<>();
        for (int i = start; i < start+numLines; i++) {
            response.add(lines.get(i));
        }
        td.setLines(response);
        return td;
    }

    protected List<String> getLines() {
        if (text == null) {
            try {
                // convert a book to plain text
                String epubText = epubToPlainText(findResourceFile("p-g-wodehouse_right-ho-jeeves.epub"));
                
                // wrap the text at 80 characters 
                epubText = wrapMultiLine(epubText,80);
                System.out.println(epubText);

                // convert to an array of strings
                text = Arrays.asList(epubText.split("\n"));
            }
            catch (IOException | XMLStreamException | FactoryConfigurationError e) {
                e.printStackTrace();
            }
        }
        return text;
    }
    
    protected String cleanUnicode(String string) {
        String clean = string.replaceAll(UNICODE_RIGHT_SINGLE_QUOTE, "'")
                .replaceAll(UNICODE_LEFT_DOUBLE_QUOTE, "\"")
                .replaceAll(UNICODE_RIGHT_DOUBLE_QUOTE, "\"")
                .replaceAll(UNICODE_EM_DASH, "--")
                .replaceAll(UNICODE_TWO_EM_DASH, "--")
                .replaceAll(UNICODE_THREE_EM_DASH, "--");
        return whiteListFilter(clean);
    }
    
    protected String whiteListFilter(String in) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < in.length(); i++) {
            Character c = in.charAt(i);
            sb.append(OKAY_CHARS.contains(c) ? c : ' '); 
        }
        return sb.toString();
    }

    protected String wrapMultiLine(String text, int maxLen) {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        return reader.lines().map(f->wrapSingleLine(f, maxLen)).collect(Collectors.joining("\n"));
    }
    
    protected String wrapSingleLine(String text, int maxLen) {
        text = text.trim();
        int len = text.length();
        
        // if we're under the max length, then there's nothing to do
        if (len <= maxLen) {
            return text;
        }
        
        // start at the margin and look back for a space to break the line
        for (int i = maxLen; i > 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return text.substring(0, i) + "\n" + wrapSingleLine(text.substring(i), maxLen);
            }
        }
 
        // if there aren't any spaces within the max length, then just wrap as soon as we can after the max length
        for (int i = maxLen; i <= len; i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return text.substring(0, i) + "\n" + wrapSingleLine(text.substring(i), maxLen);
            }
        }
        
        // there weren't any spaces; give up and return the original string
        return text;
    }
    
    protected String epubToPlainText(File epub) throws IOException, XMLStreamException, FactoryConfigurationError {
        StringBuffer sb = new StringBuffer();
        
        EpubReader epubReader = new EpubReader();
        Book book = epubReader.readEpub(new FileInputStream(epub));

        for (Resource resource : book.getContents()) {

            if (EXPECTED_MEDIA_TYPE.equals(resource.getMediaType().toString())) {
            
                XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(resource.getReader());
                
                boolean inBody = false;
                boolean ignore = false;
                
                // for each tag (or text) in the XML
                while (xsr.hasNext() && !ignore) {
                    
                    int event = xsr.next();
                    
                    if (event == XMLStreamConstants.END_ELEMENT) {
                        String element = xsr.getLocalName().toLowerCase();
                        switch (element) {
                        case "br":
                            sb.append("\n");
                            break;
                        case "p":
                        case "h1":
                        case "h2":
                        case "h3":
                        case "h4":
                        case "h5":
                        case "h6":
                        case "hr":
                            sb.append("\n\n");
                            break;
                        case "body":
                            inBody = false;
                            break;
                        }
                    }
                    
                    else if (event == XMLStreamConstants.START_ELEMENT) {
                        
                        // if this is the body tag
                        if ("body".equals(xsr.getLocalName().toLowerCase())) {
                            inBody = true;
                            
                            // for each attribute
                            for (int i = 0; i < xsr.getAttributeCount(); i++) {

                                // get the attribute and value
                                String ns = xsr.getAttributeName(i).getPrefix().toLowerCase();
                                String attribute = xsr.getAttributeLocalName(i).toString().toLowerCase();
                                String value = xsr.getAttributeValue(i).toLowerCase(); 
                                
                                // if the attribute is epub:type="frontmatter" or epub:type="backmatter", then skip the rest of this resource
                                if (ns.equals("epub") && attribute.equals("type")) {
                                    if (value.contains("frontmatter") || value.contains("backmatter")) {
                                       ignore = true; 
                                    }
                                }
                            }
                        }
                    }
                    
                    else if (event == XMLStreamConstants.CHARACTERS) {
                        if (inBody) {
                            
                            // strip out all line breaks and leading white space
                            String text = xsr.getText().replaceAll("[\\n\\r]", "").replaceAll("^\\s+", "");

                            // if there's any actual text, append it
                            if (text.trim().length() > 0) {
                                sb.append(text);
                            }
                        }
                    }
                }
            }
        }

        // convert some unicode chars to ascii, blank out others
        String epubStr = cleanUnicode(sb.toString());
        
        // break into lines, trim and collect white space on each line
        List<String> lines = Arrays.asList(epubStr.split("\n")).stream().map(e->e.trim().replaceAll("\\s+", " ")).collect(Collectors.toList()); 

        // remove any blank lines from the top
        while (lines.size() > 0 && lines.get(0).trim().length() == 0) {
            lines.remove(0);
        }

        // consolidate consecutive blank lines
        for (int i = 0; i < lines.size(); i++) {
            while (i+1 != lines.size() && lines.get(i).trim().length() == 0 && lines.get(i+1).trim().length() == 0) {
                lines.remove(i+1);
            }
        }
        
        // remove any blanks lines from the bottom
        while (lines.size() > 0 && lines.get(lines.size()-1).trim().length() == 0) {
            lines.remove(lines.size()-1);
        }
        
        // re-assemble into 
        epubStr = String.join("\n", lines);
        return epubStr;
    }
    
    protected File findResourceFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
        
}

package org.themullers.typer.textsource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class Book {
    
    private static final String UNICODE_LEFT_DOUBLE_QUOTE = "\u201C";
    private static final String UNICODE_RIGHT_DOUBLE_QUOTE = "\u201D";
    private static final String UNICODE_RIGHT_SINGLE_QUOTE = "\u2019";
    private static final String UNICODE_EM_DASH = "\u2014";
    private static final String UNICODE_TWO_EM_DASH = "\u2E3A";
    private static final String UNICODE_THREE_EM_DASH = "\u2E3B";

    private static final Set<Character> OKAY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`1234567890-=~!@#$%^&*()_+[]\\;':\",./<>? \n".chars().mapToObj(i->Character.valueOf((char)i)).collect(Collectors.toSet());
    
    public static final String EXPECTED_MEDIA_TYPE = "application/xhtml+xml";

    private File epubFile;
    private String epubAsString;
    
    public Book(File file) {
        this.epubFile = file;
    }
    
    protected void convertEpubToString() {
        try {
            StringBuffer sb = new StringBuffer();
            
            EpubReader epubReader = new EpubReader();
            for (Resource resource : epubReader.readEpub(new FileInputStream(epubFile)).getContents()) {
    
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
            epubAsString = cleanUnicode(sb.toString());
            
            // break into lines, trim and collect white space on each line
            List<String> lines = Arrays.asList(epubAsString.split("\n")).stream().map(e->e.trim().replaceAll("\\s+", " ")).collect(Collectors.toList()); 
    
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
            epubAsString = String.join("\n", lines);
        }
        catch (IOException | XMLStreamException | FactoryConfigurationError x) {
            throw new EpubConversionException(x);
        }
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

    @Override
    public String toString() {
        
        if (epubAsString == null) {
            convertEpubToString();
        }
        
        return epubAsString;
    }
    
    static class EpubConversionException extends RuntimeException {

        // required for serializables
        private static final long serialVersionUID = -698001924849353020L;

        public EpubConversionException(Throwable t) {
            super(t);
        }
    }
}


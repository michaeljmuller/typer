package org.themullers.typer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class AdHoc {
    
    public static final String EXPECTED_MEDIA_TYPE = "application/xhtml+xml";
    
    @Test
    public void test() throws Exception {
        String text = epubToPlainText(findTestFile("p-g-wodehouse_right-ho-jeeves.epub"));
        System.out.println(wrapMultiLine(text, 80));
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
                            
                            // strip out all line breaks and leading white space, also compress white space
                            String text = xsr.getText().replaceAll("[\\n\\r]", "").replaceAll("^\\s+", "").replaceAll("\\s+", " ");

                            // if there's any actual text, append it
                            if (text.trim().length() > 0) {
                                sb.append(text);
                            }
                        }
                    }
                }
            }
        }
        
        return sb.toString();
    }
    
    protected File findTestFile(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile());
    }
    
}

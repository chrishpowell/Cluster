/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.xml;

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class XmlParserHandler extends DefaultHandler
{
    protected StringBuffer contentBuffer;
    protected Stack<String> tags;

    @Override
    public void startDocument()
            throws SAXException
    {
        System.out.println("XMLDumpHandler:startDocument...");
        tags = new Stack<>();
        contentBuffer = new StringBuffer();
    }

    @Override
    public void characters(char[] ch, int start, int length)
                    throws SAXException
    {
        contentBuffer.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
        System.out.format("XMLDumpHandler:startElement... uri [%s], localname [%s], qName [%s]\r\n", uri,localName,qName );
        tags.push(qName);
        contentBuffer.setLength(0);			
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        System.out.println("XMLDumpHandler:endElement... " +qName);
        tags.pop();
    }

    @Override
    public void endDocument()
            throws SAXException
    {
        //onParserEnd();
    }

    /** Returns the contents of the currently active XML element. */
    public String getContents() { return contentBuffer.toString(); }

    /** Returns whether there is a non-empty content within the currently 
     *  active XML element. */
    public boolean hasContents() { return (contentBuffer.length() > 0); }

    /** Returns the XML tag name of the parent of the currently active
     *  XML element. */
    public String getParent() { return tags.peek(); }
}



/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
 
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class UsersXmlParser
{
    public ArrayList parseXml(InputStream in)
            throws ParserConfigurationException
    {
        //Create a empty link of users initially
        ArrayList<User> users = new ArrayList<>();
        try
        {
            //Create default handler instance
            UserParserHandler handler = new UserParserHandler();
            
            //Create parser from factory
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            XMLReader reader = parser.getXMLReader();
 
            //Register handler with parser
            reader.setContentHandler(handler);
 
            //Create an input source from the XML input stream
            InputSource source = new InputSource(in);
 
            //parse the document
            reader.parse(source);
 
            //populate the parsed users list in above created empty list; You can return from here also.
            users = handler.getUsers();
 
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
        
        return users;
    }
}

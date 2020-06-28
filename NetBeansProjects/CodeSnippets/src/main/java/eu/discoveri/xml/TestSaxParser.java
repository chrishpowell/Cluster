/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;


/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class TestSaxParser
{
    public static void main(String[] args)
            throws FileNotFoundException, ParserConfigurationException
    {
        //Locate the file
        File xmlFile = new File("/home/chrispowell/NetBeansProjects/CodeSnippets/src/main/java/eu/discoveri/resources/test1.xml");
 
        //Create the parser instance
        UsersXmlParser parser = new UsersXmlParser();
 
        //Parse the file
        ArrayList users = parser.parseXml(new FileInputStream(xmlFile));
 
        //Verify the result
        System.out.println(users);
    }
}

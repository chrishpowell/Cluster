/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.test;

import com.zaxxer.hikari.HikariDataSource;

import eu.discoveri.predikt3.config.EnSetup;
import eu.discoveri.predikt3.graph.Populate;
import eu.discoveri.predikt3.graph.SentenceNode;
import eu.discoveri.predikt3.main.CorpusProcessDb;
import eu.discoveri.predikt3.sentences.LangCode;
import eu.discoveri.predikt3.sentences.Language;
import eu.discoveri.predikt3.sentences.MultiToken;
import eu.discoveri.predikt3.sentences.Token;
import eu.discoveri.predikt3.utils.DbUtils;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author chrispowell
 */
public class TokenTest
{
    public static void dumpTokensOfSentence(List<SentenceNode> lsn)
    {
        lsn.forEach(s -> {
            System.out.println("\r\nS> " +s.getSentence());
            System.out.println("  Tokens:");
            System.out.print("    ");
            s.getTokens().forEach(t -> {
                System.out.print(" [" +t.getToken()+ ":"+t.getPOS()+"]");
            });
        });
    }
    
    /**
     * M A I N
     * =======
     * @param args
     * @throws IOException
     * @throws Exception 
     */
    public static void main(String[] args)
            throws IOException, Exception
    {
        List<SentenceNode> lsn = List.of(
                new SentenceNode("S0","American inventor, Philo T. Farnsworth, an 81-year-old pioneer of television, was accorded what many believe was long overdue glory Wednesday when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol."),
                new SentenceNode("S1","American inventor Philo T. Farnsworth, a pioneer of television, was honored when a 7-foot bronze likeness of the electronics genius was dedicated in the U.S. Capitol.")   );
        
        List<Token> listToks = List.of( new Token("American",""), new Token("81",""), new Token("year",""), new Token("old",""), new Token("inventor",""), new Token("a",""), new Token("pioneer",""), new Token("television","") );
        List<Token> ltoks = new ArrayList<>(listToks); // Make dynamic from fixed
        
        // Setup
        Populate popl = new Populate( LangCode.en );
        System.out.println("... Setup [English]");
        EnSetup enSetup = new EnSetup();
        Language l = enSetup.getLanguage();
        
        // Tokens test
        System.out.println("Token test:");
        List<Token> lToks = l.remStopWords(enSetup,ltoks);
        lToks.forEach(t -> {
                System.out.print(" [" +t.getToken()+ "]");
        });
        System.out.println("\r\n.................................");
        
        // Connection pool
        System.out.println("... Connect to documents database");
        HikariDataSource docDbPool = DbUtils.getPooledDocDbConnection();
        // Get a Db connection from pools
        Connection docDb1 =  docDbPool.getConnection();
        
        // Process the 'corpus'
        System.out.println("... Setup corpus processing");
        CorpusProcessDb cp = new CorpusProcessDb( docDbPool, enSetup );
        // Sentence count
        cp.setSentCount(lsn.size());
        
//        SentenceNode snt = new SentenceNode("","American, quick brown 81-year-old fox jumps dog");
//        MultiToken mt = snt.clean(snt.getSentence());
//        System.out.println("----> " +mt.getS());
//        snt.updateSentence( l.remOOBChars(snt.getSentence()) );
//        System.out.println("----> " +ct);
        System.out.println("----------------------------");
        
        System.out.println("... Process and clean sentence list");
        System.out.println("\r\n   >>Clean sentence");
        System.out.println("\r\n   >>Raw tokenize:");
        cp.rawTokenizeSentenceCorpus(popl.getPme(),lsn);
        dumpTokensOfSentence(lsn);
        
        System.out.println("\r\n\r\n   >>>Clean tokens 1a (unapostrophe):");
        List<Token> lts = null;
        for( SentenceNode s: lsn )
        {
            try
            {
                lts = l.unApostrophe(enSetup.loadApostrophesProperties(), s.getTokens());
                s.setTokens(lts);
            }
            catch( Exception ex )
                { ex.printStackTrace(); }
        }
        System.out.println("\r\n============================================================");
        dumpTokensOfSentence(lsn);
        
        System.out.println("\r\n\r\n   >>>Clean tokens 1b (\"un-dash\"):");
        for( SentenceNode s: lsn )
        {
            try
            {
                s.cleanTokens(popl.getPme());
            }
            catch( Exception ex )
                { ex.printStackTrace(); }
        }
        System.out.println("\r\n============================================================");
        dumpTokensOfSentence(lsn);
        
        System.out.println("\r\n\r\n   >>>Clean tokens 2 (remove stopwords):");
        for( SentenceNode s: lsn )
        {
            try
            {
                lts = l.remStopWords(enSetup,s.getTokens());  // lts
                s.setTokens(lts);
            }
            catch( Exception ex )
                { ex.printStackTrace(); }
        }
        System.out.println("\r\n============================================================");
        dumpTokensOfSentence(lsn);
        
        System.out.println("\r\n\r\n   >>>Clean tokens 3 (remove numbers,unwanted unicodes):");
        for( SentenceNode s: lsn )
        {
            try
            {
                s.removeNumbers(s.getLocale());
                s.removeUnwanted();
            }
            catch( Exception ex )
                { ex.printStackTrace(); }
        }
        System.out.println("\r\n============================================================");
        dumpTokensOfSentence(lsn);
        
        System.out.println("\r\n\r\n   >>>Clean tokens 4 (\"keep\" POS tokens):");
        for( SentenceNode s: lsn )
        {
            try
            {
                s.keepTokens(enSetup.getKeepNodes());
            }
            catch( Exception ex )
                { ex.printStackTrace(); }
        }
        System.out.println("\r\n============================================================");
        dumpTokensOfSentence(lsn);
//        System.out.println("\r\n\r\n   >>Lemmatize:");
//        cp.lemmatizeSentenceCorpusViaRdb(docDb1, lsn);
//        dumpTokensOfSentence(lsn);
    }
}

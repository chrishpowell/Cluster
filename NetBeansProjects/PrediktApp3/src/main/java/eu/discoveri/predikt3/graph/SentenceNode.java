/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt3.graph;


import eu.discoveri.predikt3.cluster.DocumentCategory;
import eu.discoveri.predikt3.cluster.SentenceClusterNum;

import eu.discoveri.predikt3.exceptions.ListLengthsDifferException;
import eu.discoveri.predikt3.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.predikt3.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt3.exceptions.TokensListIsEmptyException;

import eu.discoveri.predikt3.config.Constants;
import eu.discoveri.predikt3.exceptions.SentenceNotPersistedException;
import eu.discoveri.predikt3.sentences.Token;
import eu.discoveri.predikt3.sentences.MultiToken;
import eu.discoveri.predikt3.lemmatizer.Lemmatizer;
import eu.discoveri.predikt3.sentences.LangCode;
import eu.discoveri.predikt3.utils.CharacterUtils;
import eu.discoveri.predikt3.utils.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;


/**
 * "Node" for a sentence.
 * @author Chris Powell, Discoveri OU
 * @email info@discoveri.eu
 */
public class SentenceNode extends AbstractVertex implements Comparator<SentenceNode>, Comparable<SentenceNode>
{
    // Basic attrs
    private String              origText = "";
    private String              sentence = "";
    // Scoring for this node
    private double              score = 0.d,
                                prevScore = 0.d;
    private List<Token>         tokens;
    
    // Language/Locale
    private LangCode            langCode;
    private Locale              locale;

    // Clusters
    private DocumentCategory    docCategory;
    private SentenceClusterNum  clusterNum = new SentenceClusterNum(-1);


    /**
     * Constructor generally for non-persisted Sentence).  Will trim spaces to
     * single space.
     * 
     * @param namespace
     * @param name
     * @param origText (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param tokens
     * @param docCategory
     * @param initScore 
     */
    public SentenceNode(    String name,
                            String namespace,
                            String origText,
                            LangCode langCode,
                            Locale locale,
                            List<Token> tokens,
                            DocumentCategory docCategory,
                            double initScore   )
    {
        super(name,namespace);

        this.origText = origText;

        this.langCode = langCode;
        this.locale = locale;
        this.tokens = tokens;
        this.prevScore = initScore;
        this.docCategory = docCategory;
        this.score = initScore;
        // Spaces cleanup to work with further processing
        this.sentence = origText.trim().replaceAll(" +", " ");
    }
    
    /**
     * Constructor (generally for persisted Sentence). Expects sentence to have
     * already been trimmed.
     * 
     * @param nid
     * @param namespace
     * @param name
     * @param origText (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param tokens
     * @param docCategory
     * @param initScore 
     */
    public SentenceNode(    int nid,
                            String name,
                            String namespace,
                            String origText,
                            LangCode langCode,
                            Locale locale,
                            List<Token> tokens,
                            DocumentCategory docCategory,
                            double initScore   )
    {
        super(nid, name,namespace);

        this.origText = origText;

        this.langCode = langCode;
        this.locale = locale;
        this.tokens = tokens;
        this.prevScore = initScore;
        this.docCategory = docCategory;
        this.score = initScore;
        // Spaces cleanup to work with further processing
        this.sentence = origText.trim().replaceAll(" +", " ");
    }

    /**
     * Constructor (generally for non-persisted Sentence). Sets a default namespace.
     * No DocumentCategory.  Will trim spaces to single space.
     * 
     * @param name  
     * @param origText
     * @param langCode
     * @param locale
     * @param tokens
     * @param initScore 
     */
    public SentenceNode(String name, String origText, LangCode langCode, Locale locale, List<Token> tokens, double initScore)
    {
        this(name, Constants.DEFNS, origText, langCode, locale, tokens, new DocumentCategory(-1), initScore);
    }
    
    /**
     * Constructor (generally for a persisted Sentence). Sets a default namespace.
     * No DocumentCategory.
     * 
     * @param nid Node id
     * @param name  
     * @param origText
     * @param langCode
     * @param locale
     * @param tokens
     * @param initScore 
     */
    public SentenceNode(int nid, String name, String origText, LangCode langCode, Locale locale, List<Token> tokens, double initScore)
    {
        this(nid, name, Constants.DEFNS, origText, langCode, locale, tokens, new DocumentCategory(-1), initScore);
    }
    
    /**
     * Constructor (generally for non-persisted Sentence). With default namespace
     * and language LangCode.en. Will trim spaces to single space.
     * 
     * @param name
     * @param origText Sentence text (with punctuation etc.)
     * @param docCategory
     * @param initScore 
     */
    public SentenceNode(String name, String origText, DocumentCategory docCategory, double initScore)
    {
        this(name, Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), docCategory, initScore);
    }
    
    /**
     * Constructor (generally for persisted Sentence). With default namespace and
     * language LangCode.en.
     * 
     * @param nid Node id
     * @param name
     * @param origText Sentence text (with punctuation etc.)
     * @param docCategory
     * @param initScore 
     */
    public SentenceNode(int nid, String name, String origText, DocumentCategory docCategory, double initScore)
    {
        this(nid, name, Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), docCategory, initScore);
    }
    
    /**
     * Constructor (generally for non-persisted Sentence). With default namespace,
     * initial score. Will trim spaces to single space.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param docCategory
     */
    public SentenceNode( String name, String origText, LangCode langCode, Locale locale, DocumentCategory docCategory  )
    {
        this(name,Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), docCategory, Constants.NODESCOREDEF);
    }
    
    /**
     * Constructor (generally for persisted Sentence). With default namespace, initial score.
     * 
     * @param nid Node id
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param docCategory
     */
    public SentenceNode( int nid, String name, String origText, LangCode langCode, Locale locale, DocumentCategory docCategory  )
    {
        this(nid, name, Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), docCategory, Constants.NODESCOREDEF);
    }

    /**
     * Constructor (generally for non-persisted Sentence). With default namespace.
     * Will trim spaces to single space.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param docCategory
     * @param initScore
     */
    public SentenceNode( String name, String origText, LangCode langCode, Locale locale, DocumentCategory docCategory, double initScore )
    {
        this(name, Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), docCategory, initScore);
    }
    
    /**
     * Constructor (generally for persisted Sentence). With default namespace.
     * 
     * @param nid
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param docCategory
     * @param initScore
     */
    public SentenceNode( int nid, String name, String origText, LangCode langCode, Locale locale, DocumentCategory docCategory, double initScore )
    {
        this(nid, name, Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), docCategory, initScore);
    }
    
    /**
     * Constructor (generally for persisted Sentence).
     * 
     * @param nid
     * @param name
     * @param origText
     * @param langCode
     * @param locale 
     */
    public SentenceNode( int nid, String name, String origText, LangCode langCode, Locale locale)
    {
        this(nid, name, Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), new DocumentCategory(-1), Constants.NODESCOREDEF);
    }

    /**
     * Constructor (generally for non-persisted Sentence). With default namespace,
     * initial score and  and language LangCode.en. Will trim spaces to single space.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param docCategory
     */
    public SentenceNode( String name, String origText, DocumentCategory docCategory )
    {
        this(name, Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), docCategory, Constants.NODESCOREDEF);
    }
    
    /**
     * Constructor (generally from persisted Sentence) and expected to have tokens.
     * With default namespace, initial score and  and language LangCode.en.
     *
     * @param nid 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param tokens
     */
    public SentenceNode( int nid, String name, String origText, List<Token> tokens )
    {
        this(nid, name, Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, tokens, new DocumentCategory(-1), Constants.NODESCOREDEF);
    }
    
    /**
     * Constructor (generally for persisted Sentence). With default namespace,
     * initial score and  and language LangCode.en.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     */
    public SentenceNode( String name, String origText )
    {
        this(name, Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), new DocumentCategory(-1), Constants.NODESCOREDEF);
    }
    
    /**
     * Constructor (generally for persisted Sentence). With default namespace,
     * initial score and  and language LangCode.en.
     * 
     * @param nid Node id
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     */
    public SentenceNode( int nid, String name, String origText )
    {
        this(nid, name, Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), new DocumentCategory(-1), Constants.NODESCOREDEF);
    }

    
    /**
     * Get count of sentences on Db.
     * @param conn
     * @return
     * @throws SQLException 
     */
    public static int sentenceCount( Connection conn )
            throws SQLException
    {
        int sentCount = 0;

        Statement st = conn.createStatement();
        ResultSet res = st.executeQuery("select count(*) as c from Sentence");
        while( res.next() ) { sentCount = res.getInt("c"); }
        
        return sentCount;
    }

    /**
     * Modifiers.
     * @return 
     */
    public String getSentence() { return sentence; }
    public void updateSentence( String s ) { sentence = s; }
    public String getOrigText() { return origText; }
    
    public DocumentCategory getDocumentCategory() { return docCategory; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public double getPrevScore() { return prevScore; }
    public void setPrevScore(double prevScore) { this.prevScore = prevScore; }

    public List<Token> getTokens() { return tokens; }
    public void setTokens(List<Token> tokens) { this.tokens = tokens; }
    
    public LangCode getLangCode(){ return langCode; }
    public Locale getLocale() { return locale; }
    
    public SentenceClusterNum getSentenceClusterNum() { return clusterNum; }
    public void setSentenceClusterNum( SentenceClusterNum clusterNum ) { this.clusterNum = clusterNum; }
    

    /**
     * Persist this sentence (no tokens).  Uses auto-commit.
     * @param conn
     * @return 
     * @throws java.sql.SQLException 
     */
    public int persistSingleSentence( Connection conn )
            throws SQLException
    {
        int     snId = -1;

        // Prepare.
        PreparedStatement sent = conn.prepareStatement("insert into documents.Sentence values(default,?,?,?,?,?,?,default)",Statement.RETURN_GENERATED_KEYS);

        sent.setString(1, sentence);
        sent.setString(2, langCode.name());
        sent.setString(3, locale.toString());
        sent.setDouble(4, score);
        sent.setDouble(5, prevScore);
        sent.setLong(6, this.getDocumentCategory().getCategoryNum());
        sent.execute();

        //The id
        ResultSet rs = sent.getGeneratedKeys();
        if( rs.next() )
            snId = rs.getInt(1);
        
        return snId;
    }
    
    /**
     * Persist a list of sentences.  Auto-commit per sentence.
     * @param conn
     * @param lsn
     * @throws java.sql.SQLException
     */
    public static void persistListSentences( Connection conn, List<SentenceNode> lsn )
            throws SQLException
    {
        // Prepare.
        PreparedStatement sent = conn.prepareStatement("insert into documents.Sentence values(default,?,?,?,?,?,?,default)");
        
        for( SentenceNode sn: lsn )
        {
            sent.setString(1, sn.getSentence());
            sent.setString(2, sn.getLangCode().name());
            sent.setString(3, sn.getLocale().toString());
            sent.setDouble(4, sn.getScore());
            sent.setDouble(5, sn.getPrevScore());
            sent.setLong(6,sn.getDocumentCategory().getCategoryNum());
            sent.execute();
        }
    }

    /**
     * Persist this sentence (along with its tokens).
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    public int persistwithTokens( Connection conn )
            throws SQLException
    {
        int     snId = -1;
        
        try
        {
            // First, the Sentence...
            PreparedStatement sent = conn.prepareStatement("insert into documents.Sentence values(default,?,?,?,?,?,?,default)",Statement.RETURN_GENERATED_KEYS);
            // ...then the Tokens
            PreparedStatement tkn = conn.prepareStatement("insert into documents.Token values(default,?,?,?,?,?");

            // Start transaction
            conn.setAutoCommit(false);

            sent.setString(1, sentence);
            sent.setString(2, langCode.name());
            sent.setString(3, locale.toString());
            sent.setDouble(4, score);
            sent.setDouble(5, prevScore);
            sent.setInt(6,0);
            sent.execute();

            // The id
            ResultSet rs = sent.getGeneratedKeys();
            if( rs.next() )
            {
                snId = rs.getInt(1);
                setNid(snId);
            }

            // Second, the tokens
            for( Token t: tokens )
            {
                tkn.setString(1, t.getToken());
                tkn.setString(2, t.getStem());
                tkn.setString(3, t.getLemma());
                tkn.setString(4, t.getPOS());
                tkn.setInt(5, snId);
            }

            conn.commit();
        }
        catch( SQLException sex )
        {
            conn.rollback();
            throw new SQLException(sex);
        }
        
        return snId;
    }
    
    /**
     * Persist this sentence's tokens.
     * 
     * @param conn
     * @throws TokensListIsEmptyException
     * @throws SentenceNotPersistedException
     * @throws SQLException 
     */
    public void updateDbWithTokens( Connection conn )
            throws TokensListIsEmptyException, SentenceNotPersistedException, SQLException
    {
        int snId = this.getNid();
        
        if( snId < 1 )
            { throw new SentenceNotPersistedException("Nid not set from Db?"); }
        if( tokens.isEmpty() )
        {
            System.out.println(" [No useful tokens in sentence: " +snId+ "]");
            return;
        }

//        try
//        {
            // Prepare
            PreparedStatement tkn = conn.prepareStatement("insert into documents.Token values(default,?,?,?,?,?)");
            
            // Second, the tokens
            for( Token t: tokens )
            {
                tkn.setString(1, t.getToken());
                tkn.setString(2, t.getStem());
                tkn.setString(3, t.getLemma());
                tkn.setString(4, t.getPOS());
                tkn.setInt(5, snId);
                tkn.execute();
            }
//        }
//        catch( SQLException sex )
//        {
//            conn.rollback();
//            throw new SQLException(sex);
//        }
    }
    
    /**
     * Clean string of punctuation, keeping apostrophes.  Note@ Tokenizing the
     * sentence renders this moot.
     * 
     * @param in
     * @return 
     */
    public MultiToken clean( String in )
    {
        String s = "";
        int tokCount = 1;
        
        // Ditch punctuation, leave just characters and whitespace and apostrophes
        for( Character c: in.toCharArray() )
        {
            if(c.equals('-') || Character.getType(c) == Character.DASH_PUNCTUATION )
            {
                s += " ";
                ++tokCount;
            }
            else
                if(c.equals('\'') || c.equals('\u2019'))
                    s += "'";
            else
                if(Character.isLetterOrDigit(c) || Character.isWhitespace(c))
                    s += c;
            // Section below  disappears (ie token not added)
//            else
//            {
//                s += "?";
//                System.out.println("...> DODGY CHAR: " +c+ ", num. val: " +Character.getNumericValue(c)+ ", type: " +Character.getType(c));
//            }
        }
        
        return new MultiToken(s,tokCount);
    }
    
    /**
     * Clean original sentence text, remove punctuation and set to lowercase.
     * NB: Do not lowercase sentence before tokenization as this affects the
     * (OpenNLP) token process in peculiar ways!
     * 
     * @return 
     */
    public String cleanSentence()
    {
        // Ditch punctuation etc.
        sentence = clean(sentence).getS();

        return sentence;
    }
    
    /**
     * Clean (input) tokens (normally for sentence not tokenised via method). Ditch
     * any nulls and also remove "'s" from tokens.("is" is a stop word anyway and
     * possession can be ignored).
     * 
     * @param pme
     * @return 
     * @throws eu.discoveri.predikt3.exceptions.TokensListIsEmptyException 
     */
    public List<Token> cleanTokens(POSTaggerME pme)
            throws TokensListIsEmptyException
    {
        List<Token> newToks = new ArrayList<>();

        for( Iterator<Token> t = this.tokens.iterator(); t.hasNext(); )
        {
            Token tokn = t.next();
            String tok = tokn.getToken();

            // Not interested in null, empty or single space
            if( tok == null || tok.isEmpty() || tok.equals(" ") )
                t.remove();
            else
            {
                MultiToken mtok = clean(tok.toLowerCase(locale));
                String stok = mtok.getS();
                // @TODO: Check for German (wie geht's) ********************************[l.unApostrophe?]*****************************************************
                if( stok.contains("'s") )
                    stok = stok.replaceAll("'s", "");
                
                // Do we need to add to Token list? ('-' became space)
                if( mtok.getTokCount() > 1 )
                {
                    t.remove();
                    List<String> ls = Arrays.asList(stok.split("\\s*[ ]\\s*"));
                    ls.forEach(tk -> {
                        newToks.add(new Token(tk,""));
                    });
            
                    // POS tag these 'new' tokens (if any)
                    if( !newToks.isEmpty() )
                        posTagTokenList(pme,newToks);
                }
                else
                    tokn.setToken(stok);
            }
        }
        
        // Append any new tokens
        if( !newToks.isEmpty() )
            tokens.addAll(newToks);
        
        return this.tokens;
    }
    
    /**
     * Remove pure numbers (eg: 85 or 8.5e3 but not M6)
     * @param locl
     * @return list of tokens
     */
    public List<Token> removeNumbers( Locale locl )
    {
        NumberFormat nf = NumberFormat.getInstance(locl);
        for( Iterator<Token> t = this.tokens.iterator(); t.hasNext(); )
        {
            Token tok = t.next();
            // Hokey way of dealing with numbers
            try
            {
                Number n = nf.parse(tok.getToken());
                t.remove();
            }
            catch( ParseException pex ){}                                       // If not a number, good
        }
        
        return this.tokens;
    }
    
    /**
     * Remove abbreviations/elisions and stop words.
     * @return 
     * @throws NoLanguageSetEntriesException 
     * @throws eu.discoveri.predikt.exceptions.TokensListIsEmptyException 
     */
//    public List<Token> reduceTokens()
//            throws NoLanguageSetEntriesException, TokensListIsEmptyException
//    {
//        if( LanguageLocaleSet.getLangMap().size() < 1 )
//            throw new NoLanguageSetEntriesException("SentenceNode:reduceTokens: LanguageSet not initialised!");
//        
//        LangResources lr = LanguageLocaleSet.getValue(langCode);
//        Language l = lr.getLanguage();
//        
//        // Remove apostrophes (based on language)
//        List<Token> redToks = l.unApostrophe(lr.getApostrophes(), tokens);
//        // Remove stopwords
//        redToks = l.remStopWords(langCode,redToks);
//        // Remove numbers
//        redToks = removeNumbers(Locale.ENGLISH);
//        
//        setTokens(redToks);
//        return redToks;
//    }
    
    /**
     * Keep tokens that match the POS input list, remove otherwise.
     * @param keepToks
     * @return 
     */
    public List<Token> keepTokens( List<String> keepToks )
    {
        // For each token...
        for( Iterator<Token> t = this.tokens.iterator(); t.hasNext(); )
        {
            Token tok = t.next();
            boolean matched = false;
            
            //... check if in 'keep' list.  Match any inlist.
            for( String kt: keepToks )
            {
                if( tok.getPOS().equals(kt) )
                {
                    matched = true;
                    break;
                }
            }
            
            if( !matched )
                t.remove();
        }
        
        return this.tokens;
    }
    
    /**
     * Remove unwanted tokens (owing to eccentric Unicode tokenization).
     * See https://en.wikipedia.org/wiki/List_of_Unicode_characters.
     * @return 
     */
    public List<Token> removeUnwanted()
    {
        for( Iterator<Token> t = this.tokens.iterator(); t.hasNext(); )
        {
            String tok = t.next().getToken();
            // Others may be added
            if( tok.equals("\u2019\u0073") )                // 's
                t.remove();
            if( tok.equals("") )                            // 'null'
                t.remove();
        }
        
        return this.tokens;
    }

    /**
     * Tokenise the sentence.  Adds tokens to token list of this sentence.
     * 
     * @param tme
     * @return
     * @throws SentenceIsEmptyException 
     */
    public List<Token> tokenizeThisSentence( TokenizerME tme )
            throws SentenceIsEmptyException
    {
        // No sentence?
        if( sentence.isEmpty() )
            throw new SentenceIsEmptyException("SentenceNode:tokenizeThisSentence()");
        
        // Add tokens to (Token) List with probabilites
        Arrays.asList(tme.tokenize(this.sentence)).forEach(tok-> {
            // Add token to list
            this.tokens.add(new Token(tok));
        });

        return this.tokens;
    }

    /**
     * Tokenise the sentence. Adds tokens to token list of this sentence.
     * 
     * @param st
     * @return
     * @throws SentenceIsEmptyException 
     */
    public List<Token> simpleTokenizeThisSentence( SimpleTokenizer st )
            throws SentenceIsEmptyException
    {
        // No sentence?
        if( sentence.isEmpty() )
            throw new SentenceIsEmptyException("SentenceNode:simpleTokenizeThisSentence()");
        
        // Add tokens to (Token) List with probabilites
        Arrays.asList(st.tokenize(this.sentence)).forEach(tok-> {
            // Add token to list
            this.tokens.add(new Token(tok));
        });

        return this.tokens;
    }
    
    /**
     * Tokenize the sentence using StringReader and whitespace check.
     * Adds 'raw' tokens to token list of this sentence.
     * NB: Will not work with symbolic languages (Mandarin). Hindi is UTF-8
     * compliant (with caveats).  See: https://github.com/taranjeet/hindi-tokenizer/blob/master/HindiTokenizer.py
     * 
     * @return
     * @throws SentenceIsEmptyException 
     * @throws java.io.IOException 
     */
    public List<Token> rawTokenizeThisSentence()
            throws SentenceIsEmptyException, IOException
    {
        // No sentence?
        if( sentence.isEmpty() )
            throw new SentenceIsEmptyException("SentenceNode:rawTokenizeThisSentence()");
        
        StringBuilder tok = new StringBuilder();
        // StringReader allows Unicode
        try( StringReader sr = new StringReader(sentence) )
        {
            // Read through sentence
            while(true)
            {
                int ch = sr.read();
                if( ch == -1 )
                {
                    this.tokens.add(new Token(tok.toString()));
                    break;
                }
                
                // If whitespace, new token else append to current
                if( Character.isWhitespace(ch) )
                {
                    this.tokens.add(new Token(tok.toString()));
                    tok = new StringBuilder();
                }
                else
                    tok.append((char)ch);
            }
        }

        return this.tokens;
    }
    
    /**
     * Get the POS tags (as in PennPOS) for this sentence.
     * Implements POS simplification: POSsimple().
     * 
     * Note: This converts tokens into String arrays (as that's how the OpenNLP
     * tokenizer accepts arguments), updates the Token class and returns a
     * String array of POStags.
     * 
     * @param pme
     * @return 
     * @throws TokensListIsEmptyException 
     */
    public List<String> posTagThisSentence( POSTaggerME pme )
            throws TokensListIsEmptyException
    {
        // No tokens?
        if( this.tokens.isEmpty() )
            throw new TokensListIsEmptyException("No tokens, SentenceNode:postagThisSentence()");
        
        // Get list of POStags
        Token[] toks = this.tokens.toArray(new Token[0]);
        String[] ptags = pme.tag(this.tokens.stream().map(t -> t.getToken()).toArray(String[]::new));
        
        // Map simplified tag into Token class
        for( int ii=0; ii<ptags.length; ii++ )
            toks[ii].setPos(POSsimple(ptags[ii]));
        
        // Convert to List
        return Arrays.asList(ptags);
    }
    
    /**
     * POS tag a list of tokens.
     * 
     * @param pme
     * @param inToks
     * @return
     * @throws TokensListIsEmptyException 
     */
    public List<Token> posTagTokenList( POSTaggerME pme, List<Token> inToks )
            throws TokensListIsEmptyException
    {
        // No tokens?
        if( inToks.isEmpty() )
            throw new TokensListIsEmptyException("No tokens, SentenceNode:postagTokenList(), sentence id: " +this.getNid()+ ", document id: " +this.getDocumentCategory().getCategoryNum());
        
        // Get list of POStags
        Token[] toks = inToks.toArray(new Token[0]);
        String[] ptags = pme.tag(inToks.stream().map(t -> t.getToken()).toArray(String[]::new));
        
        // Map simplified tag into Token class
        for( int ii=0; ii<ptags.length; ii++ )
            toks[ii].setPos(POSsimple(ptags[ii]));
        
        // Convert to List
        return Arrays.asList(toks);
    }

    
    /**
     * Tokenise and get POS tags for a sentence.See tokeniseThisSentence and
     * posTagThisSentence.
     * Note: Not thread safe.
     * 
     * @param tme
     * @param pme
     * @return
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException 
     */
    public List<Token> tokPosThisSentence( TokenizerME tme, POSTaggerME pme )
            throws SentenceIsEmptyException, TokensListIsEmptyException
    {
        // Generate tokens
        this.tokens = tokenizeThisSentence( tme );
        
        // Add POS tags to tokens
        posTagThisSentence( pme );
        
        return this.tokens;
    }
    
    /**
     * Simple tokenizer with POS tagging.
     * Note: Not thread safe.
     * 
     * @param st
     * @param pme
     * @return
     * @throws SentenceIsEmptyException
     * @throws TokensListIsEmptyException 
     */
    public List<Token> simpleTokPosThisSentence( SimpleTokenizer st, POSTaggerME pme )
            throws SentenceIsEmptyException, TokensListIsEmptyException
    {
        // Generate tokens
        this.tokens = simpleTokenizeThisSentence( st );
        
        // Add POS tags to tokens
        posTagThisSentence( pme );
        
        return this.tokens;
    }
    
    /**
     * Raw tokenizer with POS tagging.Note: Not thread safe.
     * 
     * @param pme
     * @return 
     * @throws eu.discoveri.predikt3.exceptions.SentenceIsEmptyException 
     * @throws eu.discoveri.predikt3.exceptions.TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public List<Token> rawTokPosThisSentence( POSTaggerME pme )
            throws SentenceIsEmptyException, TokensListIsEmptyException, IOException
    {
        // Generate tokens
        this.tokens = rawTokenizeThisSentence();
        
        // Add POS tags to tokens
        posTagThisSentence( pme );
        
        return this.tokens;
    }
    
    /**
     * Simplify POStags for discoveri tokens. 
     * @param POStag
     * @return 
     */
    public static String POSsimple( String POStag )
    {
        String POS = "";
        switch( POStag )
        {
            case "CC": POS = "CC"; break;
            case "CD": POS = "CD"; break;
            case "DT": POS = "DT"; break;
            case "EX": POS = "EX"; break;
            case "FW": POS = "FW"; break;
            case "IN": POS = "IN"; break;
            case "JJ": POS = "JJ"; break;
            case "JJR": POS = "JJ"; break;
            case "JJS": POS = "JJ"; break;
            case "LS": POS = "LS"; break;
            case "MD": POS = "MD"; break;
            case "NN": POS = "NN"; break;
            case "NNS": POS = "NN"; break;
            case "NNP": POS = "NN"; break;
            case "NNPS": POS = "NN"; break;
            case "PDT": POS = "PDT"; break;
            case "POS": POS = "POS"; break;
            case "PRP": POS = "PRP"; break;
            case "PRP$": POS = "PRP$"; break;
            case "RB": POS = "RB"; break;
            case "RBR": POS = "RB"; break;
            case "RBS": POS = "RB"; break;
            case "RP": POS = "RP"; break;
            case "SYM": POS = "SYM"; break;
            case "TO": POS = "TO"; break;
            case "UH": POS = "UH"; break;
            case "VB": POS = "VB"; break;
            case "VBD": POS = "VB"; break;
            case "VBG": POS = "VB"; break;
            case "VBN": POS = "VB"; break;
            case "VBP": POS = "VB"; break;
            case "VBZ": POS = "VB"; break;
            case "WDT": POS = "WDT"; break;
            case "WP": POS = "WP"; break;
            case "WP$": POS = "WP"; break;
            case "WRB": POS = "WP"; break;
            default: POS = "XX";
        }
        
        return POS;
    }

//    /**
//     * Sequences/outcomes of the sentence.
//     * 
//     * @param pme
//     * @return
//     * @throws TokensListIsEmptyException 
//     */
//    public List<Sequence> addSequences2Sentence( POSTaggerME pme )
//            throws TokensListIsEmptyException
//    {
//        // No tokens?
//        if( this.tokens.isEmpty() )
//            throw new TokensListIsEmptyException("No tokens, SentenceNode:addSequences2Sentence(): " +getName());
//
//        // Get tokens into String array
//        String[] stoks = new String[this.tokens.size()];
//        int idx = 0;
//        for( Token t: this.tokens )
//        {
//            stoks[idx++] = t.getToken();
//        }
//        
//        // Get list of sequences/outcomes
//        return Arrays.asList(pme.topKSequences(stoks));
//    }

//    /**
//     * Tokenise the sentence into spans (position in sentence).
//     * 
//     * @param tme
//     * @return
//     * @throws SentenceIsEmptyException 
//     */
//    public List<Span> spanThisSentence( TokenizerME tme )
//            throws SentenceIsEmptyException
//    {
//        // No sentence?
//        if( sentence.isEmpty() )
//            throw new SentenceIsEmptyException("Empty sentence, SentenceNode:spanThisSentence() " +getName());
//        
//        // Put spans in List
//        this.spans.addAll(Arrays.asList(tme.tokenizePos(this.sentence)));
//        
//        return this.spans;
//    }
//    
//    /**
//     * Add sentence probabilities to Sentence.  Note: must be done after sentDetect.
//     * 
//     * @param inprobs
//     * @return 
//     */
//    public List<Double> addSentenceProbs( double[] inprobs )
//    {
//        // Convert to list
//        this.sprobs = DoubleStream.of(inprobs).boxed().collect(Collectors.toList());
//        
//        return this.sprobs;
//    }
    
    /**
     * Get lemmas of the tokens.
     * 
     * @param lemmer
     * @param match2NN Match Pell NNP/NNPS to NN on dictionary
     * @return tok-string,lemma
     * @throws TokensListIsEmptyException 
     * @throws ListLengthsDifferException 
     * @throws POSTagsListIsEmptyException 
     */
    public Map<Token,String> lemmatizeThisSentence( Lemmatizer lemmer, boolean match2NN )
            throws TokensListIsEmptyException, ListLengthsDifferException, POSTagsListIsEmptyException
    {
        // Lemmatized tokens
        Map<Token,String>  lemmas;
        
        // Get token strings into List
        List<String> tags = this.tokens.stream().map(t -> t.getPOS()).collect(Collectors.toList());

        /*
         * Form lemmas from <token-string,POStag> 'key' into dictionary. Returns
         * tok-string,lemma map. Throws exception if List lengths differ
         */
        lemmas = lemmer.lemmatize2Token(this.tokens, tags, match2NN);
        lemmas.forEach((k,v) -> Utils.findListEntry(this.tokens,k).get().setLemma(v));

        return lemmas;
    }
    
    /**
     * Get a page of sentences (with its tokens/lemmas).
     * 
     * @param conn
     * @param offset
     * @param pageSize
     * @return
     * @throws SQLException 
     */
    public static List<SentenceNode> sentenceByPage( Connection conn, int offset, int pageSize )
            throws SQLException
    {
        // What to get
        PreparedStatement sent = conn.prepareStatement("select Sentence.id as nid, sentence from Sentence limit ?,?");
        PreparedStatement tkn = conn.prepareStatement("select token,lemma from Token where sn = ?");
        
        // Get a page of SentenceNodes (Qs), create list of Sentence objects
        List<SentenceNode> lsSN = new ArrayList<>();
        try
        {
            sent.setInt(1,offset); sent.setInt(2,pageSize);
            ResultSet ress = sent.executeQuery();
            while( ress.next() )
            {
                List<Token> ltkn = new ArrayList<>();
                tkn.setInt(1,ress.getInt("nid"));
                ResultSet restk = tkn.executeQuery();
                while( restk.next() )
                    { ltkn.add(new Token(restk.getString("token"),restk.getString("lemma"))); }
                lsSN.add(new SentenceNode(ress.getInt("nid"),"",ress.getString("sentence"),ltkn));
            }
        } catch (SQLException ex) {
            Logger.getLogger("JOOQTest").log(Level.SEVERE, null, ex);
        }
        
        return lsSN;
    }

    
    /**
     * Dump out tokens.
     */
    public void dumpTokens()
    {
        this.tokens.forEach(t -> System.out.print(" [" +t.getToken()+ "]"));
        System.out.println("");
    }
    
    /**
     * Dump token plus POS plus lemma.
     */
    public void dumpFullTokens()
    {
        this.tokens.forEach(t -> System.out.print(" ["+t.getToken()+":"+t.getPOS()+":"+t.getLemma()+"]"));
        System.out.println("");
    }
    
    /**
     * Dump tokens plus POS.
     */
    public void dumpTokenPOS()
    {
        this.tokens.forEach(t -> {
            System.out.println("[" +t.getToken()+ "]: " +t.getPOS());
        });
        
        System.out.println("");
    }
    
    /**
     * Dump chars of tokens (could be Unicode...)
     */
    public void dumpTokenChars()
    {
        this.tokens.forEach(t -> {
            System.out.print(" [" +t.getToken()+ "]: " +t.getPOS());
            CharacterUtils.bytestoIntStream(t.getToken()).forEach(c -> System.out.print(" "+c));
            System.out.println("");
        });
        
        System.out.println("");
    }

    /**
     * Comparator: Sort SentenceNodes by number tokens (used in comparing sentences).
     * @param s1
     * @param s2
     * @return 
     */
    @Override
    public int compare(SentenceNode s1, SentenceNode s2)
    {
        return s1.tokens.size() - s2.tokens.size();
    }
    
    /**
     * Comparable: Sort SentenceNodes by number tokens (used in comparing sentences).
     * @param other
     * @return 
     */
    @Override
    public int compareTo(SentenceNode other)
    {
        return this.tokens.size() - other.tokens.size();
    }

    /**
     * Hash.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + this.getNid();
        return hash;
    }

    /**
     * Equals.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj)
    {
        final SentenceNode other = (SentenceNode) obj;
        
        if(this == other) { return true; }
        if(other == null) { return false; }
        if( getClass() != other.getClass() ) { return false; }
        
        return this.getNid() == other.getNid();
    }
    
    /**
     * Simple output
     * @return 
     */
    @Override
    public String toString()
    {
        return getName()+" ("+this.tokens.size()+")";
    }
}

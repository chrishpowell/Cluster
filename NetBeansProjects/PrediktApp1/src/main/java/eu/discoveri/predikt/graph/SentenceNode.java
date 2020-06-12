/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.graph;


import eu.discoveri.predikt.exceptions.ListLengthsDifferException;
import eu.discoveri.predikt.exceptions.POSTagsListIsEmptyException;
import eu.discoveri.predikt.exceptions.SentenceIsEmptyException;
import eu.discoveri.predikt.exceptions.TokensListIsEmptyException;

import eu.discoveri.predikt.config.Constants;
import eu.discoveri.predikt.sentences.Token;
import eu.discoveri.predikt.lemmatizer.Lemmatizer;
import eu.discoveri.predikt.sentences.LangCode;
import eu.discoveri.predikt.utils.CharacterUtils;
import eu.discoveri.predikt.utils.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;

import org.neo4j.driver.Query;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;


/**
 * Neo4j node for a sentence.
 * @author Chris Powell, Discoveri OU
 * @email info@discoveri.eu
 */
@NodeEntity
public class SentenceNode extends AbstractVertex
{
    // Basic attrs
    private String          origText = "";
    private String          sentence = "";
    // Scoring for this node
    private double          score = 0.d, prevScore = 0.d;
    private List<Token>     tokens;
    
    // Language/Locale
    private LangCode        langCode;
    private Locale          locale;
    
    // Subgraph label
    private String          subgraph = null;
    
    // Distance
    private double          dist = Double.MAX_VALUE;
    // Between
    private double          between = 0.d, cbetween = 0.d;
    // Shortest paths (will be changed by edge weight)
    private double          spath = 0.d;
    // 'Prev.' node along path
    @Relationship(type="PREV")
    private SentenceNode    prev = null;

    // Predecessors
    private List<SentenceNode>      preds = new ArrayList<>();
    // Spans
    private final List<Span>        spans = new ArrayList<>();
    // Sequences
    private final List<Sequence>    seqs = new ArrayList<>();
    // Sentence probability
    private List<Double>            sprobs = new ArrayList<>();

    // Neo4j params
    @Properties
    private Map<String,Object>      params = new HashMap<>();


    /**
     * Constructor.
     * 
     * @param namespace
     * @param name
     * @param origText (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param tokens
     * @param initScore 
     */
    public SentenceNode(String name, String namespace, String origText, LangCode langCode, Locale locale, List<Token> tokens, double initScore)
    {
        super(name,namespace);

        this.origText = origText;

        this.langCode = langCode;
        this.locale = locale;
        this.tokens = tokens;
        this.prevScore = initScore;
        this.score = initScore;
        this.sentence = origText;

        // Build map for graph (tokens need to be processed separately)
        params = Map.of( "name",name,
                         "sentence",origText,
                         "score",score);
    }
    
    /**
     * Constructor.Sets a default namespace.
     * 
     * @param name doesnt 
     * @param origText
     * @param langCode
     * @param locale
     * @param tokens
     * @param initScore 
     */
    public SentenceNode(String name, String origText, LangCode langCode, Locale locale, List<Token> tokens, double initScore)
    {
        this(name,Constants.DEFNS, origText, langCode, locale, tokens, initScore);
    }
    
    /**
     * Constructor. With default namespace and language LangCode.en.
     * 
     * @param name
     * @param origText Sentence text (with punctuation etc.)
     * @param initScore 
     */
    public SentenceNode(String name, String origText, double initScore)
    {
        this(name,Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), initScore);
    }
    
    /**
     * Constructor. With default namespace, initial score.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param langCode
     * @param locale
     */
    public SentenceNode( String name, String origText, LangCode langCode, Locale locale )
    {
        this(name,Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), Constants.NODESCOREDEF);
    }
    
        
    /**
     * Constructor.With default namespace.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     * @param langCode
     * @param locale
     * @param initScore
     */
    public SentenceNode( String name, String origText, LangCode langCode, Locale locale, double initScore )
    {
        this(name,Constants.DEFNS, origText, langCode, locale, new ArrayList<Token>(), initScore);
    }
    
    
    /**
     * Constructor. With default namespace, initial score and  and language
     * LangCode.en.
     * 
     * @param name Name or Id
     * @param origText Sentence text (with punctuation etc.)
     */
    public SentenceNode( String name, String origText )
    {
        this(name,Constants.DEFNS, origText, LangCode.en, Locale.ENGLISH, new ArrayList<Token>(), Constants.NODESCOREDEF);
    }
    
    /**
     * No arg constructor for loading from db.
     */
    private SentenceNode()
    {
        this("","","",LangCode.en,Locale.ENGLISH,null,0.0d);
    }


    public String getSentence() { return sentence; }
    public void updateSentence( String s ) { sentence = s; }
    public String getOrigText() { return origText; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public List<SentenceNode> getPreds() { return preds; }
    public void setPreds(List<SentenceNode> preds) { this.preds = preds; }
    
    public double getDist() { return dist; }
    public void setDist(double dist) { this.dist = dist; }

    public double getBetween() { return between; }
    public void setBetween(double between) { this.between = between; }

    public double getCbetween() { return cbetween; }
    public void setCbetween(double cbetween) { this.cbetween = cbetween; }

    public double getSpath() { return spath; }
    public void setSpath(double spath) { this.spath = spath; }
    public void incrSPath(double vpath) { this.spath += vpath; }

    public SentenceNode getPrev() { return prev; }
    public void setPrev(SentenceNode prev) { this.prev = prev; }

    public List<Token> getTokens() { return tokens; }
    public void setTokens(List<Token> tokens) { this.tokens = tokens; }
    
    public String getSubgraph() { return subgraph; }
    public void setSubgraph(String subgraph) { this.subgraph = subgraph; }
    
    public LangCode getLangCode(){ return langCode; }
    public Locale getLocale() { return locale; }

    
    /**
     * Clean string of punctuation, keeping apostrophes.  Note@ Tokenizing the
     * sentence renders this moot.
     * 
     * @param in
     * @return 
     */
    public String clean( String in )
    {
        String s = "";
        
        // Ditch punctuation, leave just characters and whitespace and apostrophes
        for( Character c: in.toCharArray() )
        {
            if(c.equals('-'))
                s += " ";
            else
                if(c.equals('\'') || c.equals('\u2019'))
                    s += "'";
            else
                if(Character.isLetterOrDigit(c) || Character.isWhitespace(c))
                    s += c;
        }
        
        return s;
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
        sentence = clean(sentence);

        return sentence;
    }
    
    /**
     * Clean (input) tokens (normally for sentence not tokenised via method).
     * Ditch any nulls and also remove "'s" from tokens.  ("is" is a stop word
     * anyway and possession can be ignored).
     * @return 
     */
    public List<Token> cleanTokens()
    {
        for( Iterator<Token> t = tokens.iterator(); t.hasNext(); )
        {
            Token tokn = t.next();
            String tok = tokn.getToken();
            
            if( tok == null || tok.isEmpty() )
                t.remove();
            else
            {
                tok = clean(tok.toLowerCase(locale));

                if( tok.contains("'s") )
                    tok = tok.replaceAll("'s", "");
                tokn.setToken(tok);
            }
        }
        
        return tokens;
    }
    
    /**
     * Remove pure numbers (eg: 85 or 8.5e3 but not M6)
     * @param locl
     * @return list of tokens
     */
    public List<Token> removeNumbers( Locale locl )
    {
        NumberFormat nf = NumberFormat.getInstance(locl);
        for( Iterator<Token> t = tokens.iterator(); t.hasNext(); )
        {
            Token tok = t.next();
            try
            {
                Number n = nf.parse(tok.getToken());
                t.remove();
            }
            catch( ParseException pex ){}                                       // If not a number, good
        }
        
        return tokens;
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
     * Keep tokens that match the input list, remove otherwise.
     * @param keepToks
     * @return 
     */
    public List<Token> keepTokens( List<String> keepToks )
    {
        // For each token...
        for( Iterator<Token> t = tokens.iterator(); t.hasNext(); )
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
            
            if(!matched) t.remove();
        }
        
        return tokens;
    }
    
    /**
     * Remove unwanted tokens (owing to eccentric Unicode tokenization).
     * See https://en.wikipedia.org/wiki/List_of_Unicode_characters.
     * @return 
     */
    public List<Token> removeUnwanted()
    {
        for( Iterator<Token> t = tokens.iterator(); t.hasNext(); )
        {
            Token tok = t.next();
            // Others may be added
            if( tok.getToken().equals("\u2019\u0073") ) // 's
            {
                t.remove();
            }
        }
        
        return tokens;
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
            tokens.add(new Token(tok));
        });

        return tokens;
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
            tokens.add(new Token(tok));
        });

        return tokens;
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
        try (StringReader sr = new StringReader(sentence))
        {
            // Read through sentence
            while(true)
            {
                int ch = sr.read();
                if( ch == -1 )
                {
                    tokens.add(new Token(tok.toString()));
                    break;
                }
                
                // If whitespace, new token else append to current
                if( Character.isWhitespace(ch) )
                {
                    tokens.add(new Token(tok.toString()));
                    tok = new StringBuilder();
                }
                else
                    tok.append((char)ch);
            }
        }
        
        return tokens;
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
        if( tokens.isEmpty() )
            throw new TokensListIsEmptyException("No tokens, SentenceNode:postagThisSentence()");
        
        // Get list of POStags
        Token[] toks = tokens.toArray(new Token[0]);
        String[] ptags = pme.tag(tokens.stream().map(t -> t.getToken()).toArray(String[]::new));
        
        // Map simplified tag into Token class
        for( int ii=0; ii<ptags.length; ii++ )
            toks[ii].setPos(POSsimple(ptags[ii]));
        
        // Convert to List
        return Arrays.asList(ptags);
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
        tokens = tokenizeThisSentence( tme );
        
        // Add POS tags to tokens
        posTagThisSentence( pme );
        
        return tokens;
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
        tokens = simpleTokenizeThisSentence( st );
        
        // Add POS tags to tokens
        posTagThisSentence( pme );
        
        return tokens;
    }
    
    /**
     * Raw tokenizer with POS tagging.Note: Not thread safe.
     * 
     * @param pme
     * @return 
     * @throws eu.discoveri.predikt.exceptions.SentenceIsEmptyException 
     * @throws eu.discoveri.predikt.exceptions.TokensListIsEmptyException 
     * @throws java.io.IOException 
     */
    public List<Token> rawTokPosThisSentence( POSTaggerME pme )
            throws SentenceIsEmptyException, TokensListIsEmptyException, IOException
    {
        // Generate tokens
        tokens = rawTokenizeThisSentence();
        
        // Add POS tags to tokens
        posTagThisSentence( pme );
        
        return tokens;
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

    /**
     * Sequences/outcomes of the sentence.
     * 
     * @param pme
     * @return
     * @throws TokensListIsEmptyException 
     */
    public List<Sequence> addSequences2Sentence( POSTaggerME pme )
            throws TokensListIsEmptyException
    {
        // No tokens?
        if( tokens.isEmpty() )
            throw new TokensListIsEmptyException("No tokens, SentenceNode:addSequences2Sentence(): " +getName());

        // Get tokens into String array
        String[] stoks = new String[tokens.size()];
        int idx = 0;
        for( Token t: tokens )
        {
            stoks[idx++] = t.getToken();
        }
        
        // Get list of sequences/outcomes
        return Arrays.asList(pme.topKSequences(stoks));
    }

    /**
     * Tokenise the sentence into spans (position in sentence).
     * 
     * @param tme
     * @return
     * @throws SentenceIsEmptyException 
     */
    public List<Span> spanThisSentence( TokenizerME tme )
            throws SentenceIsEmptyException
    {
        // No sentence?
        if( sentence.isEmpty() )
            throw new SentenceIsEmptyException("Empty sentence, SentenceNode:spanThisSentence() " +getName());
        
        // Put spans in List
        spans.addAll(Arrays.asList(tme.tokenizePos(this.sentence)));
        
        return spans;
    }
    
    /**
     * Add sentence probabilities to Sentence.  Note: must be done after sentDetect.
     * 
     * @param inprobs
     * @return 
     */
    public List<Double> addSentenceProbs( double[] inprobs )
    {
        // Convert to list
        sprobs = DoubleStream.of(inprobs).boxed().collect(Collectors.toList());
        
        return sprobs;
    }
    
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
        List<String> tags = tokens.stream().map(t -> t.getPOS()).collect(Collectors.toList());

        /*
         * Form lemmas from <token-string,POStag> 'key' into dictionary. Returns
         * tok-string,lemma map. Throws exception if List lengths differ
         */
        lemmas = lemmer.lemmatize2Token(tokens, tags, match2NN);
        lemmas.forEach((k,v) -> Utils.findListEntry(tokens,k).get().setLemma(v));

        return lemmas;
    }
    
    /**
     * Create a new sentence node, ready for persisting.
     * @return Ordered set of Queries
     */
    public Set<Query> buildSentenceQuerySet()
    {
        // Ordered Set
        Set<Query> lhs = new LinkedHashSet<>();
        // Map for tokens
        Map<String,Object> ps = new HashMap<>();
        
        // Add first query
        String s = "CREATE ("+getName()+":Sentence) SET "+getName()+"={sid:$sid,name:$name,sentence:$sentence,score:$score}";
        lhs.add( new Query(s,params) );

        // Create tokens of the sentence
        StringBuilder q0 = new StringBuilder();
        int idx = 0;
        for( Token t: tokens )
        {
            q0.append("CREATE (t").append(idx).append(getName())
                    .append(":Token {text:$text").append(idx)
                    .append(",tid:$tid").append(idx).append("}) ");

            ps.put("text"+idx,t.getToken());
            ps.put("tid"+idx,"t"+idx+getName());
            // Incr idx
            ++idx;
        }
        
        // Create the query
        lhs.add( new Query(q0.toString(),ps) );
        
        // Build edges
        idx = 0;
        for( Token t: tokens )
        {
            StringBuilder q1 = new StringBuilder();
            q1.append(" MATCH (src:Sentence {sid:$sid}) MATCH (tgt:Token {tid:$tid})")
                    .append(" MERGE (src)-[:TOK]-(tgt)");
            
            // Create the query
            lhs.add( new Query(q1.toString(),parameters("sid",getName(),"tid","t"+idx+getName())) );
            // Incr idx
            ++idx;
        }

        return lhs;
    }
    
    /**
     * Dump out tokens.
     */
    public void dumpTokens()
    {
        tokens.forEach(t -> System.out.print(" [" +t.getToken()+ "]"));
        System.out.println("");
    }
    
    /**
     * Dump token plus POS plus lemma.
     */
    public void dumpFullTokens()
    {
        tokens.forEach(t -> System.out.print(" ["+t.getToken()+":"+t.getPOS()+":"+t.getLemma()+"]"));
        System.out.println("");
    }
    
    /**
     * Dump tokens plus POS.
     */
    public void dumpTokenPOS()
    {
        tokens.forEach(t -> {
            System.out.println("[" +t.getToken()+ "]: " +t.getPOS());
        });
        
        System.out.println("");
    }
    
    /**
     * Dump chars of tokens (could be Unicode...)
     */
    public void dumpTokenChars()
    {
        tokens.forEach(t -> {
            System.out.print(" [" +t.getToken()+ "]: " +t.getPOS());
            CharacterUtils.bytestoIntStream(t.getToken()).forEach(c -> System.out.print(" "+c));
            System.out.println("");
        });
        
        System.out.println("");
    }

    
    /**
     * Save on session
     * @param ss
     * @return 
     */
    public SentenceNode persist( SentenceNodeService ss )
    {
        return ss.createOrUpdate(this);
    }

    /**
     * Delete on session
     * @param ss 
     */
    public void delete( SentenceNodeService ss )
    {
        ss.delete(getNid());
    }
    
    /**
     * Find a SentenceNode via nid
     * @param nid
     * @param ss
     * @return 
     */
    public SentenceNode find( SentenceNodeService ss, Long nid )
    {
        return ss.find(nid);
    }

    /**
     * Hash.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + getUUID().hashCode();
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
        
        return this.getUUID().equals(other.getUUID());
    }
    
    /**
     * Simple output
     * @return 
     */
    @Override
    public String toString()
    {
        return getName()+" ("+tokens.size()+")";
    }
}

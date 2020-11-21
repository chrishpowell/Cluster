/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.searchapi.test;

import java.util.Map;


/**
 *
 * @author chrispowell
 */
public class SearchResults
{
    private final Map<String,String>  relevantHeaders;
    private final String              jsonResponse;

    public SearchResults(Map<String,String> relevantHeaders,String jsonResponse)
    {
        this.relevantHeaders = relevantHeaders;
        this.jsonResponse = jsonResponse;
    }

    public Map<String, String> getRelevantHeaders() { return relevantHeaders; }
    public String getJsonResponse() { return jsonResponse; }
}

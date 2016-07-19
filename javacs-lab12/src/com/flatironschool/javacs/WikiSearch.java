package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the query term(s) to relevance score
	//key = URL, val = relevance
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // FILL THIS IN!
		Map<String,Integer> either = new HashMap<String,Integer>(map); //initialize w/ values for this's terms'
		for (String word : that.map.keySet()){
			int relav1 = this.getRelevance(word);
			int relav2 = that.getRelevance(word);
			int totalRel = totalRelevance(relav1, relav2);
			either.put(word, totalRel); //replace value if shared or new, otherwise old value untouched
		}

		return new WikiSearch(either);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
		Map<String,Integer> both = new HashMap<String,Integer>(); //empty to start because shared terms unknown
		for(String word: that.map.keySet()){
			if(this.map.containsKey(word)){ //only add if mutual
				int relav1 = this.getRelevance(word);
				int relav2 = that.getRelevance(word);
				int totalRel = totalRelevance(relav1, relav2);
				both.put(word,totalRel);
			}
		}

		return new WikiSearch(both);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> exclusive = new HashMap<String, Integer>(map); //initialize to words in this 
		for(String word: that.map.keySet()){
			exclusive.remove(word); //remove terms that are mutual
		}
		
		return new WikiSearch(exclusive);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
        // FILL THIS IN!
		//use original values in map
		List<Entry<String, Integer>> list = new LinkedList<>(map.entrySet());

		//use new comparator for in-place sort
		Comparator comp = new Comparator<Entry<String, Integer>>()
    		{
        		public int compare( Entry<String, Integer> entry1,Entry<String, Integer> entry2 )
        		{
            		return ( entry1.getValue() ).compareTo( entry2.getValue() ); //compare relevance scores
        		}
    		};
    	Collections.sort( list,  comp);

    	return list;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}

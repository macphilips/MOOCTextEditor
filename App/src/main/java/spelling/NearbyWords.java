/**
 * 
 */
package spelling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @author UC San Diego Intermediate MOOC team
 *
 */
public class NearbyWords implements SpellingSuggest {
	// THRESHOLD to determine how many words to look through when looking
	// for spelling suggestions (stops prohibitively long searching)
	// For use in the Optional Optimization in Part 2.
	private static final int THRESHOLD = 1000;

	Dictionary dict;

	public NearbyWords(Dictionary dict) {
		this.dict = dict;
	}

	/**
	 * Return the list of Strings that are one modification away from the input
	 * string.
	 * 
	 * @param s
	 *            The original String
	 * @param wordsOnly
	 *            controls whether to return only words or any String
	 * @return list of Strings which are nearby the original string
	 */
	public List<String> distanceOne(String s, boolean wordsOnly) {
		List<String> retList = new ArrayList<String>();
		insertions(s, retList, wordsOnly);
		substitution(s, retList, wordsOnly);
		deletions(s, retList, wordsOnly);
		return retList;
	}

	/**
	 * Add to the currentList Strings that are one character mutation away from
	 * the input string.
	 * 
	 * @param s
	 *            The original String
	 * @param currentList
	 *            is the list of words to append modified words
	 * @param wordsOnly
	 *            controls whether to return only words or any String
	 * @return
	 */
	public void substitution(String s, List<String> currentList, boolean wordsOnly) {
		// for each letter in the s and for all possible replacement characters
		for (int index = 0; index < s.length(); index++) {
			for (int charCode = (int) 'a'; charCode <= (int) 'z'; charCode++) {
				// use StringBuffer for an easy interface to permuting the
				// letters in the String
				StringBuffer sb = new StringBuffer(s);
				sb.setCharAt(index, (char) charCode);

				// if the item isn't in the list, isn't the original string, and
				// (if wordsOnly is true) is a real word, add to the list
				if (!currentList.contains(sb.toString()) && (!wordsOnly || dict.isWord(sb.toString()))
						&& !s.equals(sb.toString())) {
					currentList.add(sb.toString());
				}
			}
		}
	}

	/**
	 * Add to the currentList Strings that are one character insertion away from
	 * the input string.
	 * 
	 * @param s
	 *            The original String
	 * @param currentList
	 *            is the list of words to append modified words
	 * @param wordsOnly
	 *            controls whether to return only words or any String
	 * @return
	 */
	public void insertions(String s, List<String> currentList, boolean wordsOnly) {

		String alpha = "abcdefghijklmnopqrstuvwxyz";
		for (char c : alpha.toCharArray()) {
			for (int i = 0, n = s.length(); i < n + 1; i++) {
				String s1 = (i == 0) ? "" : s.substring(0, i);
				String s2 = (i == n) ? "" : s.substring(i, n);
				String newstr = s1 + c + s2;
				if (!currentList.contains(newstr) && (!wordsOnly || dict.isWord(newstr)) && !s.equals(newstr)) {
					currentList.add(newstr);
				}
			}
		}
	}

	/**
	 * Add to the currentList Strings that are one character deletion away from
	 * the input string.
	 * 
	 * @param s
	 *            The original String
	 * @param currentList
	 *            is the list of words to append modified words
	 * @param wordsOnly
	 *            controls whether to return only words or any String
	 * @return
	 */
	public void deletions(String s, List<String> currentList, boolean wordsOnly) {
		for (int i = 0, n = s.length(); i < n; i++) {
			String s1 = (i == 0) ? "" : s.substring(0, i);
			String s2 = (i == n) ? "" : s.substring(i + 1, n);
			String newstr = s1 + s2;
			if (!currentList.contains(newstr) && (!wordsOnly || dict.isWord(newstr)) && !s.equals(newstr)) {
				currentList.add(newstr);
			}
		}
	}

	/**
	 * Add to the currentList Strings that are one character deletion away from
	 * the input string.
	 * 
	 * @param word
	 *            The misspelled word
	 * @param numSuggestions
	 *            is the maximum number of suggestions to return
	 * @return the list of spelling suggestions
	 */

	@Override
	public List<String> suggestions(String word, int numSuggestions) {

		// initial variables
		List<String> queue = new LinkedList<String>(); // String to explore
		HashSet<String> visited = new HashSet<String>();
		// to avoid exploring the same string multiple times

		List<String> retList = new LinkedList<String>(); // words to return

		// insert first node
		queue.add(word);
		visited.add(word);

		// TODO: Implement the remainder of this method, see assignment for
		// algorithm
		/*
		 * Input: word for which to provide number of spelling suggestions
		 * Input: number of maximum suggestions to provide Output: list of
		 * spelling suggestions
		 * 
		 * Create a queue to hold words to explore Create a visited set to avoid
		 * looking at the same String repeatedly Create list of real words to
		 * return when finished
		 * 
		 * Add the initial word to the queue and visited
		 * 
		 * while the queue has elements and we need more suggestions remove the
		 * word from the start of the queue and assign to curr get a list of
		 * neighbors (strings one mutation away from curr) for each n in the
		 * list of neighbors if n is not visited add n to the visited set add n
		 * to the back of the queue if n is a word in the dictionary add n to
		 * the list of words to return
		 * 
		 * return the list of real words
		 */
		int i = 0;
		while (!queue.isEmpty() && i < numSuggestions){
			String s = queue.remove(0);
			List<String> ls = this.distanceOne(s, true);
			for(String ss:ls){
				if(visited.add(ss)){
					queue.add(ss);
					if (dict.isWord(ss)){
						retList.add(ss);
						i++;
					}
				}
			}
		}

		return retList;

	}

	int count = 0;
	String from = "can", to = "man";

	private void dorecursion(String s, List<String> currentList, HashSet<String> visited, boolean wordsOnly) {

		List<String> queue = this.distanceOne(s, wordsOnly);

		for (String s1 : queue) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (s.equals(to)) {
				// System.out.println("Layer " + layer);
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (visited.add(s1))
				dorecursion(s1, currentList, visited, wordsOnly);

		}
		currentList.addAll(queue);
	}

	public static void main(String[] args) {
		// basic testing code to get started
		String word = "tail";
		// Pass NearbyWords any Dictionary implementation you prefer
		Dictionary d = new DictionaryHashSet();
		DictionaryLoader.loadDictionary(d, "data/dict.txt");
		NearbyWords w = new NearbyWords(d);
		List<String> l = w.distanceOne(word, true);
		System.out.println("One away word Strings for \"" + word + "\" are:");

		for (String s : l) {
			System.out.println(s);
		}
		word = "tailo";
		List<String> suggest = w.suggestions(word, 10);
		System.out.println("Spelling Suggestions for \"" + word + "\" are:");
		System.out.println(suggest);

	}

}

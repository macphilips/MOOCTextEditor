package spelling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An trie data structure that implements the Dictionary and the AutoComplete
 * ADT
 *
 * @author You
 */
public class AutoCompleteDictionaryTrie implements Dictionary, AutoComplete {

    private TrieNode root;
    private int size = 0;

    public AutoCompleteDictionaryTrie() {
        root = new TrieNode();
    }

    /**
     * Insert a word into the trie. For the basic part of the assignment (part
     * 2), you should convert the string to all lower case before you insert it.
     * <p>
     * This method adds a word by creating and linking the necessary trie nodes
     * into the trie, as described outlined in the videos for this week. It
     * should appropriately use existing nodes in the trie, only creating new
     * nodes when necessary. E.g. If the word "no" is already in the trie, then
     * adding the word "now" would add only one additional node (for the 'w').
     *
     * @return true if the word was successfully added or false if it already
     * exists in the dictionary.
     */
    public boolean addWord(String word) {
        // TODO: Implement this method.
        char[] c = word.toLowerCase().toCharArray();

        TrieNode predptr = root;
        boolean isWord = false;

        for (int i = 0, n = c.length; i < n; i++) {
            TrieNode next = predptr.insert(c[i]);

            if (next != null) {
                predptr = next;
            } else {
                predptr = predptr.getChild(c[i]);
            }

            if (i == n - 1) {
                if (!predptr.endsWord()) {
                    size++;
                    isWord = true;
                    predptr.setEndsWord(true);
                }
            }

        }
        return isWord;
    }

    /**
     * Return the number of words in the dictionary. This is NOT necessarily the
     * same as the number of TrieNodes in the trie.
     */
    public int size() {
        // TODO: Implement this method
        return size;
    }

    /**
     * Returns whether the string is a word in the trie, using the algorithm
     * described in the videos for this week.
     */
    @Override
    public boolean isWord(String s) {
        // TODO: Implement this method
        char[] c = s.toLowerCase().toCharArray();
        TrieNode predptr = root;
        for (int i = 0, n = c.length; i < n; i++) {
            TrieNode next = predptr.getChild(c[i]);
            if (next != null) {
                predptr = next;
            } else {
                return false;
            }

        }
        return predptr.endsWord();
    }

    @Override
    public List<String> predictCompletions(String prefix, int numCompletions) {
        // First find the node of the last letter
        TrieNode curr = root;
        TrieNode next = null;
        List<String> temp = new LinkedList<String>();
        for (Character c : prefix.toCharArray()) {
            next = curr.getChild(c);
            if (next == null) {
                return temp;
            }
            curr = next;
        }
        // Now build the list of predictions
        LinkedList<TrieNode> queue = new LinkedList<>();
        queue.add(curr);
        int n = 0;
        while (!queue.isEmpty()) {
            next = queue.removeFirst();
            if (next.endsWord()) {
                temp.add(next.getText());
            }
            for (Character cnext : next.getValidNextCharacters()) {
                queue.add(next.getChild(cnext));
            }
        }

        List<String> d = new ArrayList<>();
        if (numCompletions != 0) {
            for (int j = 0; j < Math.min(numCompletions, temp.size()); j++) {
                d.add(temp.get(j));
            }
        }
        return d;
    }
    public void listWode(TrieNode curr, List<String> ls) {
        if (curr == null)
            return;

        if (isWord(curr.getText())) {
            ls.add(curr.getText());
        }

        TrieNode next = null;
        for (Character c : curr.getValidNextCharacters()) {
            next = curr.getChild(c);
            listWode(next, ls);
        }
    }

    // For debugging
    public void printTree() {
        printNode(root);
    }

    /**
     * Do a pre-order traversal from this node down
     */
    public void printNode(TrieNode curr) {
        if (curr == null)
            return;

        System.out.println(curr.getText());

        TrieNode next = null;
        for (Character c : curr.getValidNextCharacters()) {
            next = curr.getChild(c);
            printNode(next);
        }
    }
}
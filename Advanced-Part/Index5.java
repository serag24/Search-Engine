import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

class Index5 {
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---";

    private final Map<Integer, String> titleByDocId;
    private final CompactTrie trie;

    // collect the titles from the file and store them in a map: document number -> title
    private static Map<Integer, String> collectTitles(String filename) {
        Map<Integer, String> map = new HashMap<>();
        try (Scanner input = new Scanner(new File(filename), StandardCharsets.UTF_8.name())) {
            if (!input.hasNext()) {
                return map;
            }
            int docNum = 1;
            String title = input.next();
            map.put(docNum, title); // store the first title in the map
            while (input.hasNext()) {
                String word = input.next();
                if (word.equals(END_OF_DOCUMENT)) {
                    String nextTitle = null;
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        if (!line.trim().isEmpty()) {
                            nextTitle = line.trim();
                            break;
                        }
                    }
                    if (nextTitle == null) {
                        break;
                    }
                    docNum++;
                    map.put(docNum, nextTitle); // store the title in the map
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
        return map;
    }

    private static void buildTrie(String filename, CompactTrie trie) {
        try (Scanner input = new Scanner(new File(filename), StandardCharsets.UTF_8.name())) {
            if (!input.hasNext()) {
                return;
            }
            int docNum = 1;
            input.next();  // skip the title
            if (!input.hasNext()) {
                return;
            }
            String word = input.next();
            trie.insert(word, docNum); // insert the first word
            while (input.hasNext()) {
                if (word.equals(END_OF_DOCUMENT)) {
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        if (!line.trim().isEmpty()) {
                            break;
                        }
                    }
                    docNum++; // increment the document number
                }
                if (!input.hasNext()) {
                    break;
                }
                word = input.next();
                trie.insert(word, docNum); // insert the next word and the document number in the list of the leaf node
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }

    public Index5(String filename) {
        this.titleByDocId = collectTitles(filename); // make a map: document number -> title
        this.trie = new CompactTrie(); // create a new compact trie
        buildTrie(filename, trie); // build the trie
    }

    public void search(String query) {
        if (query.endsWith("*")) { // if the query ends with a *, then it is a prefix search
            String prefix = query.substring(0, query.length() - 1);
            Set<Integer> ids = trie.collectByPrefix(prefix); // collect the document numbers that match the prefix
            printTitles(ids);
        } else {
            Set<Integer> ids = trie.collectExact(query); // collect the document numbers that match the exact query
            printTitles(ids);
        }
    }

    private void printTitles(Set<Integer> docIds) {
        if (docIds.isEmpty()) {
            System.out.println("No matching documents");
            return;
        }
        List<Integer> sorted = new ArrayList<>(docIds); // convert the set to a list
        Collections.sort(sorted); // sort the list of document numbers
        for (int id : sorted) {
            String t = titleByDocId.get(id); // get the title with key id from the map
            if (t != null) {
                System.out.println(t);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        long preprocessStartNanos = System.nanoTime();
        Index5 index = new Index5(args[0]);
        long preprocessEndNanos = System.nanoTime();
        long preprocessMs = (preprocessEndNanos - preprocessStartNanos) / 1_000_000L;
        System.out.println("Preprocessing time: " + preprocessMs + " ms");
        Scanner console = new Scanner(System.in);
        for (;;) {
            System.out.println("Input search string or type exit to stop (use trailing * for prefix)");
            String searchstr = console.nextLine();
            long searchStartNanos = System.nanoTime();
            if (searchstr.equals("exit")) {
                break;
            }
            index.search(searchstr);
            long searchEndNanos = System.nanoTime();
            long searchMs = (searchEndNanos - searchStartNanos) / 1_000_000L;
            System.out.println("Search time: " + searchMs + " ms");
        }
        console.close();
    }

    private static final class CompactTrie {
        private final TrieNode root = new TrieNode();

        // Add "$" to the end of the word for a prefix-free trie. Start insertion from the root node.
        void insert(String word, int docId) {
            insert(root, word + "$", docId);
        }

        private void insert(TrieNode node, String s, int docId) {
            char first = s.charAt(0);
            Edge edge = node.edges.get(first); // get the edge with the first character of s from the map
            if (edge == null) {               // case 1: the word is not in the trie and there is no edge with the first character of s
                TrieNode leaf = new TrieNode(); // create a new leaf node
                leaf.docIds = new TreeSet<>(); // create a new tree set to store the document numbers
                leaf.docIds.add(docId);        // add doc nr., here is constant time but  O(log occ) after that
                node.edges.put(first, new Edge(s, leaf)); // put the character c -> edge in the map (creating new edge)
                return;
            }
            String label = edge.label;
            int k = commonPrefixLength(label, s);
            if (k == label.length() && k == s.length()) { // case 2: the word is an edge label in the trie
                mergeDocs(edge.child, docId);
                return;
            }
            if (k == label.length()) {             // case 3: the prefix of the word is an edge label in the trie
                insert(edge.child, s.substring(k), docId);  // insert the rest of the word into the trie
                return;
            }
            if (k == s.length()) {                     // case 4: the word is a prefix of the label
                TrieNode mid = new TrieNode();          // create a new middle node
                String rest = label.substring(k);       // get the rest of the label
                mid.edges.put(rest.charAt(0), new Edge(rest, edge.child)); // put the rest of the label -> edge in the map. Give it child node of previous edge.
                TrieNode term = new TrieNode();         // create a new terminal node for the word s
                term.docIds = new TreeSet<>();          
                term.docIds.add(docId);                 // add the document number to the list
                mid.edges.put('$', new Edge("$", term)); // put the $ -> edge in the map as a child of the middle node
                edge.child = mid;                       // set the middle node as the child of the previous edge
                edge.label = s;                         // set the label of the previous edge to be the word s
                return;
            }
            // case 5: the word and the label have a common prefix but the word is not a prefix of the label
            TrieNode mid = new TrieNode();              // create a new middle node
            String labelRest = label.substring(k);
            String sRest = s.substring(k);
            mid.edges.put(labelRest.charAt(0), new Edge(labelRest, edge.child)); // edge for the rest of the label extending from the middle node
            TrieNode newLeaf = new TrieNode(); // create a new leaf node for the word s
            newLeaf.docIds = new TreeSet<>();
            newLeaf.docIds.add(docId);         // add the document number to the list
            mid.edges.put(sRest.charAt(0), new Edge(sRest, newLeaf)); // edge for the rest of the word extending from the middle node
            edge.child = mid;                                       // set the middle node as the child of the previous edge
            edge.label = label.substring(0, k);                     // set the label of the previous edge to be the common prefix
        }

        // Merge the document number into the list of document numbers in the leaf node.
        private static void mergeDocs(TrieNode n, int docId) {
            if (n.docIds == null) { // if the list of document numbers is not found, then create a new tree set
                n.docIds = new TreeSet<>();
            }
            n.docIds.add(docId); // add the document number to the list
        }

        // Collect the document numbers that match the exact query. This is the regular search.
        Set<Integer> collectExact(String word) {
            TrieNode n = navigateExact(root, word + "$"); // navigate to the leaf node of the search string
            if (n == null || n.docIds == null) {
                return Collections.emptySet();
            }
            return new HashSet<>(n.docIds); // return the set of document numbers
        }

        private TrieNode navigateExact(TrieNode node, String s) {
            if (s.isEmpty()) { // return list of document numbers if leaf is reached
                return node.docIds != null ? node : null;
            }
            char first = s.charAt(0);
            Edge e = node.edges.get(first); // get the edge with the first character of s from the map
            if (e == null) {
                return null;
            }
            String L = e.label;
            if (s.length() < L.length()) { // if s is shorter than the label, then return null - word not found
                return null;
            }
            if (!s.startsWith(L)) { // if s is not a prefix of the label, then return null - word not found
                return null;
            }
            return navigateExact(e.child, s.substring(L.length())); // navigate to the next node
        }

        // Collect the document numbers that match the prefix. This is the prefix search.
        Set<Integer> collectByPrefix(String prefix) {
            TrieNode n = navigateToPrefixEnd(root, prefix); // navigate to the node at the end of the prefix
            if (n == null) {
                return Collections.emptySet();
            }
            Set<Integer> out = new HashSet<>(); // create a new set to store the document numbers
            collectSubtreeDocs(n, out); // collect the document numbers from the subtree rooted at n
            return out; // return the set of document numbers
        }

        private TrieNode navigateToPrefixEnd(TrieNode node, String prefix) {
            if (prefix.isEmpty()) {
                return node;
            }
            int i = 0;
            TrieNode cur = node;
            while (i < prefix.length()) {
                char c = prefix.charAt(i);
                Edge e = cur.edges.get(c);
                if (e == null) {
                    return null;
                }
                String L = e.label;
                int need = prefix.length() - i; // var to keep track of the remaining length of the prefix we search for
                if (L.length() <= need) {             // case 1: the label is shorter than the prefix we search for
                    if (!prefix.startsWith(L, i)) {   //case 1.1: the label is not a prefix of the prefix we search for
                        return null;
                    }
                                                      //case 1.2: the label is a prefix of the prefix we search for -> continue to the next node
                    i += L.length(); // increment the index by the length of the label
                    cur = e.child; // set the current node to the child node
                } else {                                      // case 2: the label is longer than the prefix we search for
                    if (!L.startsWith(prefix.substring(i))) { //case 2.1: the prefix is not a prefix of the label
                        return null;
                    }
                    return e.child;                          //case 2.2: the prefix is a prefix of the label -> return the child node
                }
            }
            return cur;  // case when the prefix searched for ends exactly in a node -> return that node
        }

        // Collect all doc numbers from the subtree rooted at n.
        private void collectSubtreeDocs(TrieNode n, Set<Integer> out) {
            if (n.docIds != null) {
                out.addAll(n.docIds); // add the document numbers to the set
            }
            for (Edge e : n.edges.values()) { // iterate over the edges of the node
                collectSubtreeDocs(e.child, out); // collect the document numbers from the child node
            }
        }

        // Calculate the length of the common prefix of two strings. Needed when splitting an edge.
        private static int commonPrefixLength(String a, String b) {
            int n = Math.min(a.length(), b.length()); // get the minimum length of the two strings
            int i = 0;
            while (i < n && a.charAt(i) == b.charAt(i)) { // compare the characters of the two strings
                i++;
            }
            return i; // return the length of the common prefix
        }
    }

    private static final class TrieNode {
        final Map<Character, Edge> edges = new HashMap<>();
        TreeSet<Integer> docIds;
    }

    // Edge class represents an edge in the trie. Edge has a label and leads to a child node.
    private static final class Edge {
        String label;
        TrieNode child;

        Edge(String label, TrieNode child) {
            this.label = label;
            this.child = child;
        }
    }
}


//  For 100KB: driven, dropped, drugs.

   // First compile using $ javac Advanced-Part/Index5.java

    // Run using $ java Advanced-Part/Index5.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx12g Advanced-Part/Index5.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 12GB.
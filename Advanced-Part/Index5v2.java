import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Index5v2 {
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---";

    private Map<Integer, String> titleByDocId;
    private CompactTrie trie;

    private List<Integer> predecessorArray;
    private List<List<Integer>> sparseTable;

    // gather the titles from the file and store them in titleByDocId: docId -> title
    private void createTitleByDocIdMap(String filename) {
        titleByDocId = new HashMap<>();
        try (Scanner input = new Scanner(new File(filename), StandardCharsets.UTF_8.name())) {
            int docId = 1;
            String title = input.next();
            titleByDocId.put(docId, title); // store the first title in the map
            while (input.hasNext()) {
                String word = input.next();
                if (word.equals(END_OF_DOCUMENT)) {
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        if (!line.isEmpty()) {
                            title = line;
                            break;
                        }
                    }
                    if (title == null) { //end of file
                        break;
                    }
                    docId++;
                    titleByDocId.put(docId, title); // store the title in the map
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }

    private static void buildTrie(String filename, CompactTrie trie) {
        try (Scanner input = new Scanner(new File(filename), StandardCharsets.UTF_8.name())) {
            int docId = 1;
            input.next();  // skip the title
            String word = input.next();
            trie.insert(word, docId); // insert the first word
            while (input.hasNext()) {
                if (word.equals(END_OF_DOCUMENT)) {
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        if (!line.isEmpty()) {
                            break;
                        }
                    }
                    docId++; // increment the doc id
                }
                if (!input.hasNext()) { // in case of end of file
                    break;
                }
                word = input.next();
                trie.insert(word, docId); // insert the next word
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }

    public Index5v2(String filename) {
        createTitleByDocIdMap(filename); // make a map: doc id -> title
        trie = new CompactTrie(); 
        buildTrie(filename, trie); // build the trie
        trie.dfs(trie.root); // create doc array + lv/rv assignments for each node
        createPredecessorArray(trie.docArray);
        createSparseTable();
    }

    public void search(String query) {
        if (query.endsWith("*")) { // if the query ends with a *, then it is a prefix search
            String prefix = query.substring(0, query.length() - 1);
            TrieNode n = trie.nodeAtEndOfPrefix(trie.root, prefix);
            if (n == null) {
                System.out.println("No matching documents");
                return;
            }
            printTitlesInSubtree(n.lv, n.rv, n.lv);
        } else {
            Map<Integer, Integer> results = trie.collectExact(query); // docId -> occurrences of the exact word
            printTitles(results);
        }
    }

    // color listing algorithm
    private void printTitlesInSubtree(int i, int j, int lv) {
        if (i > j) {
            return;
        }
        int p = RMQ(i, j);
        if (predecessorArray.get(p) >= lv) { //end recursion
            return;
        }
        System.out.println(titleByDocId.get(trie.docArray.get(p)));
        printTitlesInSubtree(i, p - 1, lv); //recurse in left part
        printTitlesInSubtree(p + 1, j, lv); //recurse in right part
    }

    // print titles in descending order of rank
    private void printTitles(Map<Integer, Integer> docIdToCount) {
        if (docIdToCount.isEmpty()) {
            System.out.println("No matching documents");
            return;
        }
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(docIdToCount.entrySet());
        entries.sort(
                Comparator.<Map.Entry<Integer, Integer>>comparingInt(Map.Entry::getValue).reversed());
        for (Map.Entry<Integer, Integer> e : entries) {
            String t = titleByDocId.get(e.getKey());
            System.out.println(t);
        }
    }

    private void createPredecessorArray(List<Integer> docArray) {
        predecessorArray = new ArrayList<>();
        HashMap<Integer, Integer> lastSeen = new HashMap<>();
        for (int i = 0; i < docArray.size(); i++) {
            int docId = docArray.get(i);
            predecessorArray.add(lastSeen.getOrDefault(docId, -1));
            lastSeen.put(docId, i);
        }
    }

    private void createSparseTable() {
        int n = predecessorArray.size();
        sparseTable = new ArrayList<>();
        int k = (int) Math.floor(Math.log(n) / Math.log(2)); // floor(log2(n))
        List<Integer> row0 = new ArrayList<>(n);
        for (int j = 0; j < n; j++) {
            row0.add(j);  //first row of indices
        }
        sparseTable.add(row0);
        for (int i = 1; i < k; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j <= n - (1 << i); j++) { // 1<<i is 2^i
                int a = sparseTable.get(i - 1).get(j);
                int b = sparseTable.get(i - 1).get(j + (1 << (i - 1)));
                row.add(predecessorArray.get(a) <= predecessorArray.get(b) ? a : b); // add index of the smaller value
            }
            sparseTable.add(row);
        }
    }

    private int RMQ(int l, int r) {
        int len = r - l + 1;
        int j = (int) Math.floor(Math.log(len) / Math.log(2)); // floor(log2(len)), finding correct row in sparse table
        int leftIdx = sparseTable.get(j).get(l);
        int rightIdx = sparseTable.get(j).get(r - (1 << j) + 1); // 1<<j is 2^j
        return predecessorArray.get(leftIdx) <= predecessorArray.get(rightIdx) // choosing index whose value in L is smaller
                ? leftIdx
                : rightIdx;
    }

    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        long preprocessStart = System.nanoTime();
        Index5v2 index = new Index5v2(args[0]);
        long preprocessEnd = System.nanoTime();
        long preprocessMs = (preprocessEnd - preprocessStart) / 1_000_000L;
        System.out.println("Preprocessing time: " + preprocessMs + " ms");
        Scanner console = new Scanner(System.in);
        for (;;) {
            System.out.println("Input search string or type exit to stop");
            String searchstr = console.nextLine();
            long searchStart = System.nanoTime();
            if (searchstr.equals("exit")) {
                break;
            }
            index.search(searchstr);
            long searchEnd = System.nanoTime();
            long searchMs = (searchEnd - searchStart) / 1_000_000L;
            System.out.println("Search time: " + searchMs + " ms");
        }
        console.close();
    }

    private static final class CompactTrie {
        private final TrieNode root = new TrieNode();
        private final List<Integer> docArray = new ArrayList<>();

        // DFS algorithm
        private void dfs(TrieNode n) {
            n.lv = docArray.size();
            if (n.docCounts != null) {
                for (Map.Entry<Integer, Integer> e : n.docCounts.entrySet()) {
                    docArray.add(e.getKey()); //adding docIds to the docArray
                }
            }
            for (Edge e : n.edges.values()) {
                dfs(e.child);
            }
            n.rv = docArray.size() - 1;
        }

        // Adding "$" to the end of the word for a prefix-free trie
        private void insert(String word, int docId) {
            insert(root, word + "$", docId);
        }

        private void insert(TrieNode node, String s, int docId) {
            char first = s.charAt(0);
            Edge edge = node.edges.get(first); // get the edge with the first character of s from the map
            if (edge == null) {               // case 1: the word is not in the trie and there is no edge with the first character of s
                TrieNode leaf = new TrieNode(); // create a new leaf node
                leaf.docCounts = new HashMap<>();
                leaf.docCounts.put(docId, 1); // add doc id -> rank
                node.edges.put(first, new Edge(s, leaf)); // put the entry c -> edge in the map (creating new edge)
                return;
            }
            String label = edge.label;
            int k = commonPrefixLength(label, s);
            if (k == label.length() && k == s.length()) { // case 2: the word is an edge label in the trie
                TrieNode leaf = edge.child;           // leaf node
                if (leaf.docCounts == null) {         
                    leaf.docCounts = new HashMap<>();
                }
                leaf.docCounts.merge(docId, 1, Integer::sum); // increment the rank of docId by 1
                return;
            }
            if (k == label.length()) {             // case 3: the prefix of the word is an edge label in the trie
                insert(edge.child, s.substring(k), docId);  // insert the rest of the word into the trie
                return;
            }
            if (k == s.length()) {                     // case 4: the word is a prefix of the label
                TrieNode mid = new TrieNode();          // create a new middle node
                String rest = label.substring(k);       // get the rest of the label
                mid.edges.put(rest.charAt(0), new Edge(rest, edge.child)); // edge for the rest of the label extending from the middle node
                TrieNode leaf = new TrieNode();         // create a new leaf node for the word s
                leaf.docCounts = new HashMap<>();
                leaf.docCounts.put(docId, 1);
                mid.edges.put('$', new Edge("$", leaf)); // edge from mid node to leaf node with label '$'
                edge.child = mid;                       // set the middle node extending from the previous edge
                edge.label = s;                         // set the label of the previous edge to be the word s
                return;
            }
            // case 5: the word and the label have a common prefix but the word is not a prefix of the label
            TrieNode mid = new TrieNode();              // create a new middle node
            String labelRest = label.substring(k);
            String sRest = s.substring(k);
            mid.edges.put(labelRest.charAt(0), new Edge(labelRest, edge.child)); // edge for the rest of the label extending from the middle node
            TrieNode leaf = new TrieNode(); // create a new leaf node for the word s
            leaf.docCounts = new HashMap<>();
            leaf.docCounts.put(docId, 1);
            mid.edges.put(sRest.charAt(0), new Edge(sRest, leaf)); // edge for the rest of the word extending from the middle node
            edge.child = mid;                                       // set the middle node extending from the previous edge
            edge.label = label.substring(0, k);                     // set the label of the previous edge to be the common prefix
        }

        // calculating length of the common prefix of edge label and pattern
        private static int commonPrefixLength(String a, String b) {
            int n = Math.min(a.length(), b.length()); //  minimum length of the two strings
            int i = 0;
            while (i < n && a.charAt(i) == b.charAt(i)) { // compare the characters of the two strings
                i++;
            }
            return i; // return the length of the common prefix
        }

        // Regular search. Collect docId -> doc rank for the exact word.
        Map<Integer, Integer> collectExact(String word) {
            TrieNode n = navigateExact(root, word + "$"); // navigate to the leaf node of the search string
            if (n == null || n.docCounts == null || n.docCounts.isEmpty()) {
                return Collections.emptyMap();
            }
            return new HashMap<>(n.docCounts); // return the hash map of document ids and their ranks
        }

        private TrieNode navigateExact(TrieNode node, String s) {
            if (s.isEmpty()) { // return leaf node if reached
                return node;
            }
            char first = s.charAt(0);
            Edge e = node.edges.get(first); // get the edge with the first character of s from the map
            if (e == null) {
                return null;
            }
            String l = e.label;
            if (s.length() < l.length()) { // if s is shorter than the label, then return null - word not found
                return null;
            }
            if (!s.startsWith(l)) { // if s doesn't start with the label, then return null - word not found
                return null;
            }
            return navigateExact(e.child, s.substring(l.length())); // navigate to the next node
        }

        // navigate to the node at the end of the prefix
        private TrieNode nodeAtEndOfPrefix(TrieNode node, String prefix) {
            int i = 0;
            TrieNode cur = node;
            while (i < prefix.length()) {
                char c = prefix.charAt(i);
                Edge e = cur.edges.get(c);
                if (e == null) {
                    return null;
                }
                String l = e.label;
                int need = prefix.length() - i; // var to keep track of the remaining length of the prefix we search for
                if (l.length() <= need) {             // case 1: the label is shorter than the prefix we search for (or same length)
                    if (!prefix.startsWith(l, i)) {   //case 1.1: the label is not a prefix of the prefix we search for
                        return null;
                    }
                                                      //case 1.2: the label is a prefix of the prefix we search for -> continue to the next node
                    i += l.length(); // increment the index by the length of the label
                    cur = e.child; // set the current node to the child node
                } else {                                      // case 2: the label is longer than the prefix we search for
                    if (!l.startsWith(prefix.substring(i))) { //case 2.1: the prefix is not a prefix of the label
                        return null;
                    }
                    return e.child;                          //case 2.2: the prefix is a prefix of the label -> return the child node
                }
            }
            return cur;  // case when the prefix searched for ends exactly in a node -> return that node
        }
    }

    private static final class TrieNode {
        Map<Character, Edge> edges = new HashMap<>(); // character -> Edge
        Map<Integer, Integer> docCounts; // docId -> rank , is null for non-leaf nodes
        
        int lv; // index of the leftmost docId in the subtree
        int rv; // index of the rightmost docId in the subtree
    }

    private static final class Edge {
        String label; // edge label
        TrieNode child; // child node

        Edge(String label, TrieNode child) {
            this.label = label;
            this.child = child;
        }
    }
}


//  For 100KB: driven, dropped, drugs.

   // First compile using $ javac Advanced-Part/Index5v2.java

    // Run using $ java Advanced-Part/Index5v2.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx12g Advanced-Part/Index5v2.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 12GB.
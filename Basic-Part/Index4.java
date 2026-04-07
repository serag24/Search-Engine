import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
 
class Index4 {
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---";

    private static class WikiItem {
        String str;
        Document doc;
        WikiItem next;

        WikiItem(String s, Document d) {
            str = s;
            doc = d;
            next = null;
        }
    }

    private static class Document {
        String title;
        Document next;

        Document(String t, Document n) {
            title = t;
            next = n;
        }
    }

    
    //When the number of distinct words (WikiItems) reaches table length, rehash to 2*m+1 and new (a,b)
    private static class HashTable {
        private static final long P = 1_000_000_007L;    //a large prime number
        private static final int INITIAL_CAPACITY = 17;

        private WikiItem[] table;
        
        private int numKeys;   // keeps track of how many distinct words are in the hash table
        private long hashA;
        private long hashB;

        // Instance constructor for creating the new hash table and its hash parameters
        public HashTable() {
            table = new WikiItem[INITIAL_CAPACITY];
            chooseHashParameters();
        }

        private void chooseHashParameters() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            // Params for (a * key + b) mod P
            hashA = r.nextLong(1, P); // random long number from 1 up to P-1 inclusive
            hashB = r.nextLong(0, P); // random long number from 0 up to P-1 inclusive
        }

        // Computing the key h fed into (a·h + b) mod P later. (pollinomial rolling hash)
        private long stringKey(String s) {
            long h = 0;
            for (int i = 0; i < s.length(); i++) {
                h = (h * 257L + s.charAt(i)) % P;
            }
            return h;
        }

        // s.charAt(i) is the i-th character of the string s, converted to a long value (ex. 'A' → 65, 'a' → 97).
        // For each character c (in order, left to right), update:
        // h = (h * 257 + c) % P
        // h is treated like a number in base 257, where 

// ------------------------------------------------------------------------------------------------

        // Calculating the index for the word in the hash table.
        private int indexFor(String word) {
            long k = stringKey(word);
            long mixed = (hashA * k + hashB) % P;
            // If hashA * k exceeds Long.MAX_VALUE, the result of the sum can become negative
            if (mixed < 0) {
                mixed += P;
            }
            return (int) (mixed % table.length);
        }

        // Rehashing the hash table when the number of distinct words reaches table length.
        // Takes each WikiItem in old table and inserts it into the new table.
        private void rehash() {
            WikiItem[] old = table;
            int newLen = old.length * 2 + 1;
            table = new WikiItem[newLen];
            for (int i = 0; i < newLen; i++) {
                table[i] = null;
            }
            chooseHashParameters();
            for (WikiItem head : old) {
                WikiItem cur = head;
                while (cur != null) {
                    WikiItem next = cur.next;
                    cur.next = null;
                    insertNodeAtBucket(cur);
                    cur = next;
                }
            }
        }

        // Insert an existing node (used only during rehash). Does not change numKeys.
        private void insertNodeAtBucket(WikiItem node) {
            int idx = indexFor(node.str);
            node.next = table[idx];
            table[idx] = node;
        }

        private static void addTitleIfMissing(Document head, String title) {
            Document doc = head;
            while (true) {
                if (doc.title.equals(title)) {
                    return;
                }
                if (doc.next == null) {
                    doc.next = new Document(title, null);
                    return;
                }
                doc = doc.next;
            }
        }

        
        // If word exists in table, append title to its document list if title is new; else new bucket head.
        // Rehash when numKeys == table.length before adding a new distinct word.
         
        public void addWord(String word, String title) {
            int idx = indexFor(word);
            WikiItem cur = table[idx];
            while (cur != null) {
                if (cur.str.equals(word)) {
                    addTitleIfMissing(cur.doc, title);
                    return;
                }
                cur = cur.next;
            }
            if (numKeys == table.length) {
                rehash();
                idx = indexFor(word);
            }
            WikiItem item = new WikiItem(word, new Document(title, null));
            item.next = table[idx];
            table[idx] = item;
            numKeys++;
        }

        public Document getTitles(String word) {
            int idx = indexFor(word);
            WikiItem current = table[idx];

            while (current != null) {
                if (current.str.equals(word)) {
                    return current.doc;
                }
                current = current.next;
            }
            return null;
        }
    }

    public static void printDocumentTitles(Document doc) {
        if (doc == null) {
            System.out.println("Word not found");
            return;
        }
        Document current = doc;
        while (current != null) {
            System.out.println(current.title);
            current = current.next;
        }
    }

    public static void search(String searchstr, HashTable hashTable) {
        printDocumentTitles(hashTable.getTitles(searchstr));
    }

    public Index4(String filename, HashTable hashTable) {
        try {
            Scanner input = new Scanner(new File(filename), StandardCharsets.UTF_8.name());
            if (!input.hasNext()) {
                input.close();
                return;
            }
            String title = input.next();
            if (!input.hasNext()) {
                input.close();
                return;
            }
            String word = input.next();
            hashTable.addWord(word, title);

            while (input.hasNext()) {
                if (word.equals(END_OF_DOCUMENT)) {
                    String nextTitle = null;
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        if (!line.trim().isEmpty()) {
                            nextTitle = line.trim();  // trim() removes leading and trailing whitespace
                            break;
                        }
                    }
                    if (nextTitle == null) {
                        break;
                    }
                    title = nextTitle;
                }
                if (!input.hasNext()) {
                    break;
                }
                word = input.next();
                hashTable.addWord(word, title);
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }

    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        HashTable hashTable = new HashTable();
        Index4 i = new Index4(args[0], hashTable);
        Scanner console = new Scanner(System.in);
        for (;;) {
            System.out.println("Input search string or type exit to stop");
            String searchstr = console.nextLine();
            if (searchstr.equals("exit")) {
                break;
            }
            search(searchstr, hashTable);
        }
        console.close();
    }

    // First compile using $ javac Basic-Part/Index4.java

    // Run using $ java Basic-Part/Index4.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx128m Index4.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
}

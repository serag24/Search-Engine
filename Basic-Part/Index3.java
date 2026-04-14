import java.io.*;
import java.util.Scanner;
 
class Index3 {
 
    WikiItem start;
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---"; // constant string to check for end of document
 
    private class WikiItem {    // linked list 
        String str;             // word
        Document doc;           // linked list of document titles
        WikiItem next;         // next word
 
        WikiItem(String s, Document d, WikiItem n) { // constructor
            str = s;
            doc = d;
            next = n;
        }
    }

    private class Document { // linked list of document titles
        String title;        // title
        Document next;       // next title

        Document(String t, Document n) { // constructor
            title = t;
            next = n;
        }
    }

        // Iterates through the file (outer while loop) taking one word at a time and creating a WikiItem object for it.
        // Then it iterates through all words in the file (inner while loop), saving the titles of all documents that contain the current word.
        public Index3(String filename) {
            String word, title;
            WikiItem current;
            try {
                Scanner input = new Scanner(new File(filename), "UTF-8"); // Scanner
                title = input.next();
                word = input.next();
    
                start = new WikiItem(word, new Document(title, null), null); // first WikiItem
                current = start;
                while (input.hasNext()) {   // Read all words in input

                    if(word.equals(END_OF_DOCUMENT)) {
                        // Title is the next non-empty line (whole line; may contain '.' anywhere)
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
                        title = nextTitle;
                        //System.out.println(title); // TODO: remove this
                    }
                    
                    word = input.next();
                    
                    while (true) {
                        if(current.str.equals(word)) {
                            Document head = current.doc;
                            if (head == null || !head.title.equals(title)) {
                                current.doc = new Document(title, head);
                            }
                            break;
                        }
                        
                        if(current.next == null) break;
                        current = current.next;
                    }

                    if (current.next == null && !current.str.equals(word)) {
                        current.next = new WikiItem(word, new Document(title, null), null);
                    }

                    current = start;
                }
                input.close();
            } catch (FileNotFoundException e) {
                System.out.println("Error reading file " + filename);
            }
        }
 
    // Linearly searches through the WikiItem object to see if searchstr is one of the strings in the file.
    // If it is, it prints the titles of all documents that contain the searchstr.
    public boolean search(String searchstr) {
        WikiItem current = start;

        while (current != null) {
            if (current.str.equals(searchstr)) {

                printDocumentTitles(current.doc);
                return true;
            }
            
            current = current.next;
        }
        return false;
    }

    // Prints the titles of all documents in the linked list.
    public void printDocumentTitles(Document doc) {
        Document current = doc;
        while (current != null) {
            System.out.println(current.title);
            current = current.next;
        }
    }
 
    // Main method that takes in a file name as param (run in terminal).
    // Type "exit" to stop the program.
    // Otherwise, it will search in the file for the string given in terminal and print whether it exists or not.
    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        long preprocessStartNanos = System.nanoTime();
        Index3 i = new Index3(args[0]);
        long preprocessEndNanos = System.nanoTime();
        long preprocessMs = (preprocessEndNanos - preprocessStartNanos) / 1_000_000L;
        System.out.println("Preprocessing time: " + preprocessMs + " ms");
        Scanner console = new Scanner(System.in);
        for (;;) {
            System.out.println("Input search string or type exit to stop");
            String searchstr = console.nextLine();
            long searchStartNanos = System.nanoTime();
            if (searchstr.equals("exit")) {
                break;
            }
            i.search(searchstr);
            long searchEndNanos = System.nanoTime();
            long searchMs = (searchEndNanos - searchStartNanos) / 1_000_000L;
            System.out.println("Search time: " + searchMs + " ms");
        }
        console.close();
    }

    // First compile using $ javac Basic-Part/Index3.java

    // Run using $ java Basic-Part/Index3.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx128m Index3.java DataFiles/WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
}



/// question to teacher:
/// I am currently skipping the title of the first document. 
// I am saying that it only appears in first document. Is this a problem?
/// 
/// Is the WikiItem linked list here supposed to have repeating words? If yes, it feels like
/// there is no asymptotic improvement in running time compared to Index2.
/// Searching takes same time as Index2. Maybe there are cases where a word that appears in multiple documents is found early on, so it can be faster
/// in that sense. But asimptotically it is the same time as Index2 - O(n).
// Preprocessing takes longer - O(n^2) compared to O(n) for Index2.
///
/// Also ask if it is ok to use ArrayList in the createDocument method for this basic part.
import java.io.*;
import java.util.Scanner;
 
class Index2 {
 
    WikiItem start;
    private static final String END_OF_DOCUMENT = "---END.OF.DOCUMENT---";
 
    private class WikiItem {
        String str;
        WikiItem next;
 
        WikiItem(String s, WikiItem n) {
            str = s;
            next = n;
        }
    }
 
    // Iterates through the file, prints word by word, and creates WikiItem object: start = Word(firstWord, Word(secondWord, Word(...)))
    public Index2(String filename) {
        String word;
        WikiItem current, tmp;
        try {
            Scanner input = new Scanner(new File(filename), "UTF-8");
            word = input.next();
            start = new WikiItem(word, null);
            current = start;
            while (input.hasNext()) {   // Read all words in input
                word = input.next();
                tmp = new WikiItem(word, null);
                current.next = tmp;
                current = tmp;
            }
            input.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file " + filename);
        }
    }
 
// Linearly searches through the WikiItem object to see if searchstr is one of the strings in the file
// Uppercase and lowercase are treated as different strings. Ex: "In" and "in" or "I" and "i".
    public boolean search(String searchstr) {
        WikiItem current = start;
        String title = start.str;

        while (current != null) {
            if (current.str.equals(searchstr)) {
                System.out.println(title);

                while(!current.str.equals(END_OF_DOCUMENT)) {
                    current = current.next;
                }
            }

            if(current.str.equals(END_OF_DOCUMENT) && current.next != null) {
                title = current.next.str;
            }
            
            current = current.next;
        }
        return false;
    }
 
    // Main method that takes in a file name as param (run in terminal).
    // Type "exit" to stop the program.
    // Otherwise, it will search in the file for the string given in terminal and print whether it exists or not.
    public static void main(String[] args) {
        System.out.println("Preprocessing " + args[0]);
        long preprocessStart = System.nanoTime();
        Index2 i = new Index2(args[0]);
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
            i.search(searchstr);
            long searchEnd = System.nanoTime();
            long searchMs = (searchEnd - searchStart) / 1_000_000L;
            System.out.println("Search time: " + searchMs + " ms");
        }
        console.close();
    }

    // First compile using $ javac Basic-Part/Index2.java

    // Run using $ java Basic-Part/Index2.java DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx12g Basic-Part/Index2.java DataFiles/WestburyLab.wikicorp.201004_800MB.txt sets the maximum space to 12GB.
}
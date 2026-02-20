import java.io.*;
import java.util.Scanner;
 
class Index1 {
 
    WikiItem start;
 
    private class WikiItem {
        String str;
        WikiItem next;
 
        WikiItem(String s, WikiItem n) {
            str = s;
            next = n;
        }
    }
 
    // Iterates through the file, prints word by word, and creates WikiItem object: start = Word(firstWord, Word(secondWord, Word(...)))
    public Index1(String filename) {
        String word;
        WikiItem current, tmp;
        try {
            Scanner input = new Scanner(new File(filename), "UTF-8");
            word = input.next();
            start = new WikiItem(word, null);
            current = start;
            while (input.hasNext()) {   // Read all words in input
                word = input.next();
                System.out.println(word);
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
        while (current != null) {
            if (current.str.equals(searchstr)) {
                return true;
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
        Index1 i = new Index1(args[0]);
        Scanner console = new Scanner(System.in);
        for (;;) {
            System.out.println("Input search string or type exit to stop");
            String searchstr = console.nextLine();
            if (searchstr.equals("exit")) {
                break;
            }
            if (i.search(searchstr)) {
                System.out.println(searchstr + " exists");
            } else {
                System.out.println(searchstr + " does not exist");
            }
        }
        console.close();
    }

    // First compile using $ javac Index1.java

    // Run using $ java Index1 DataFiles/WestburyLab.wikicorp.201004_100KB.txt

    // To succesfully run some of the large files you may have to increase the 
    // size of the maximum space to be used by the Java interpreter using the -Xmx flag. 
    // For instance, java -Xmx128m Index1 WestburyLab.wikicorp.201004_50MB.txt sets the maximum space to 128MB.
}
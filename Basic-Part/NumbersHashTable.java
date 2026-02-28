import java.util.Scanner;

public class NumbersHashTable {
    
    // Node class for chained hashing (linked list node)
    private static class Node {
        int value;
        Node next;
        
        Node(int value) {
            this.value = value;
            this.next = null;
        }
    }
    
    // HashTable class using chained hashing
    private static class HashTable {
        private static final int TABLE_SIZE = 7;
        private Node[] table; // Array of linked lists
        
        public HashTable() {
            table = new Node[TABLE_SIZE];
            // Initialize all buckets to null
            for (int i = 0; i < TABLE_SIZE; i++) {
                table[i] = null;
            }
        }
        
        // Hash function: a % 7
        private int hash(int value) {
            return value % 7;
        }
        
        // Insert a value into the hash table
        public void insert(int value) {
            int index = hash(value);
            Node newNode = new Node(value);
            
            // If bucket is empty, insert at head
            if (table[index] == null) {
                table[index] = newNode;
            } else {
                // Insert at the beginning of the chain
                newNode.next = table[index];
                table[index] = newNode;
            }
        }
        
        // Search for a value in the hash table
        public boolean search(int value) {
            int index = hash(value);
            Node current = table[index];
            
            // Traverse the chain at this index
            while (current != null) {
                if (current.value == value) {
                    return true;
                }
                current = current.next;
            }
            return false;
        }
        
        // Remove a value from the hash table
        public boolean remove(int value) {
            int index = hash(value);
            Node current = table[index];
            
            // If bucket is empty
            if (current == null) {
                return false;
            }
            
            // If value is at the head
            if (current.value == value) {
                table[index] = current.next;
                return true;
            }
            
            // Search for value in the chain
            while (current.next != null) {
                if (current.next.value == value) {
                    current.next = current.next.next;
                    return true;
                }
                current = current.next;
            }
            return false;
        }
        
        // Display the hash table
        public void display() {
            for (int i = 0; i < TABLE_SIZE; i++) {
                System.out.print("Bucket " + i + ": ");
                Node current = table[i];
                if (current == null) {
                    System.out.println("empty");
                } else {
                    while (current != null) {
                        System.out.print(current.value);
                        if (current.next != null) {
                            System.out.print(" -> ");
                        }
                        current = current.next;
                    }
                    System.out.println();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        HashTable hashTable = new HashTable();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Hash Table with Chained Hashing (hash function: value % 7)");
        System.out.println("Commands: insert <num>, search <num>, remove <num>, display, quit");
        
        while (true) {
            System.out.print("Enter command: ");
            String command = scanner.next();
            
            if (command.equals("quit")) {
                break;
            } else if (command.equals("insert")) {
                int value = scanner.nextInt();
                hashTable.insert(value);
                System.out.println("Inserted " + value);
            } else if (command.equals("search")) {
                int value = scanner.nextInt();
                if (hashTable.search(value)) {
                    System.out.println(value + " found in hash table");
                } else {
                    System.out.println(value + " not found in hash table");
                }
            } else if (command.equals("remove")) {
                int value = scanner.nextInt();
                if (hashTable.remove(value)) {
                    System.out.println("Removed " + value);
                } else {
                    System.out.println(value + " not found, cannot remove");
                }
            } else if (command.equals("display")) {
                hashTable.display();
            } else {
                System.out.println("Unknown command: " + command);
            }
        }
        
        scanner.close();
    }
}

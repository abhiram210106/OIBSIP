package com.oasis.exam.service;

import com.oasis.exam.model.Question;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a standardized question bank for the Java Professional Certification Exam.
 */
public class QuestionBankService {

    public static List<Question> getCertificationQuestions() {
        List<Question> list = new ArrayList<>();

        list.add(new Question(
            1,
            "In Java memory management, where are local primitive variables and object references stored during method execution?",
            new String[]{
                "Java Heap Space",
                "Call Stack (Thread Stack)",
                "Method Area / Metaspace",
                "Native Memory Space"
            },
            1, // B
            "Local primitive variables and references to objects are allocated on the Call Stack specific to the executing thread. The actual objects themselves reside on the Heap.",
            "JVM Architecture",
            1.0
        ));

        list.add(new Question(
            2,
            "Which of the following guarantees does the 'volatile' keyword provide in Java multi-threaded applications?",
            new String[]{
                "Both thread visibility and mutual exclusion (atomicity) of compound operations",
                "Guarantees visibility of variable updates across threads, preventing CPU cache staleness",
                "Prevents the class from being garbage collected by JVM",
                "Locks the monitor of the variable's enclosing object"
            },
            1, // B
            "'volatile' ensures visibility by establishing a happens-before relationship: writes to a volatile variable are immediately made visible to other threads from main memory. It does not guarantee atomicity for compound operations like count++.",
            "Concurrency & Threads",
            1.0
        ));

        list.add(new Question(
            3,
            "What will happen when evaluating the expression: String s1 = \"Java\"; String s2 = new String(\"Java\"); System.out.println(s1 == s2); ?",
            new String[]{
                "Prints true because both strings have identical content",
                "Throws a Compile-time error due to incompatible type comparison",
                "Prints false because s1 points to the String Constant Pool while s2 points to a newly allocated Heap object",
                "Throws NullPointerException at runtime"
            },
            2, // C
            "The '==' operator checks reference identity. s1 refers to the literal in the String Constant Pool, whereas 'new String()' explicitly allocates a distinct object on the Java Heap.",
            "Core Java & Strings",
            1.0
        ));

        list.add(new Question(
            4,
            "In Java 8 and later, how does HashMap handle severe hash bucket collisions when the number of entries in a bucket exceeds the TREEIFY_THRESHOLD (8)?",
            new String[]{
                "It throws a ConcurrentModificationException immediately",
                "It converts the linked list bucket into a balanced Red-Black Tree (TreeNode), reducing lookup time from O(n) to O(log n)",
                "It automatically discards the oldest entry using LRU eviction",
                "It doubles the hash table capacity and deletes all conflicting keys"
            },
            1, // B
            "In Java 8+, when a bucket exceeds 8 nodes and total map capacity is at least 64, the linked list bucket is treeified into a balanced Red-Black Tree, optimizing worst-case search complexity to O(log n).",
            "Collections Framework",
            1.0
        ));

        list.add(new Question(
            5,
            "Which interface must a resource class implement to be safely managed inside a Java 'try-with-resources' statement?",
            new String[]{
                "java.lang.Cloneable",
                "java.io.Serializable",
                "java.lang.AutoCloseable (or java.io.Closeable)",
                "java.lang.Runnable"
            },
            2, // C
            "Any resource used in try-with-resources must implement java.lang.AutoCloseable (or java.io.Closeable, which extends AutoCloseable), ensuring its close() method is automatically invoked.",
            "Exception Handling",
            1.0
        ));

        list.add(new Question(
            6,
            "What is the key difference between intermediate operations and terminal operations in the Java Stream API?",
            new String[]{
                "Intermediate operations execute eagerly; terminal operations execute lazily",
                "Intermediate operations are lazy and return a new Stream; terminal operations trigger pipeline traversal and produce a final result or side-effect",
                "Intermediate operations can only be invoked once; terminal operations can be chained indefinitely",
                "Terminal operations always execute in parallel across multiple threads"
            },
            1, // B
            "Intermediate operations (like filter, map, sorted) are lazy and assemble the pipeline without processing elements until a terminal operation (like collect, forEach, reduce) is invoked.",
            "Java Streams & Lambdas",
            1.0
        ));

        list.add(new Question(
            7,
            "What happens during compilation regarding Java Generics due to 'Type Erasure'?",
            new String[]{
                "Generic type parameters are preserved at bytecode level and enforced at runtime",
                "Type parameters are replaced with their bounds (or Object) and appropriate type casts are inserted, ensuring backward compatibility with older JVMs",
                "The compiler duplicates the class bytecode for every distinct generic type argument (like C++ templates)",
                "Generics generate dynamic proxies for each instantiated type"
            },
            1, // B
            "Type erasure removes generic type annotations during compilation, replacing them with raw types or upper bounds and inserting casts, so the JVM bytecode contains no generic type info.",
            "Generics & Typing",
            1.0
        ));

        list.add(new Question(
            8,
            "According to the contract between equals() and hashCode() in Java, which statement is MANDATORY?",
            new String[]{
                "If two objects have the same hashCode(), their equals() method must return true",
                "If two objects are equal according to equals(), calling hashCode() on each must produce the exact same integer result",
                "Two unequal objects must always generate distinct hash codes",
                "Overriding equals() automatically regenerates a compliant hashCode() by the JVM"
            },
            1, // B
            "The Java specification dictates: If obj1.equals(obj2) is true, then obj1.hashCode() MUST equal obj2.hashCode(). However, unequal objects may have hash collisions.",
            "Object-Oriented Design",
            1.0
        ));

        list.add(new Question(
            9,
            "What happens if an unboxing operation is performed on a wrapper object whose value is null (e.g., Integer val = null; int num = val;)?",
            new String[]{
                "num is assigned the default primitive value 0",
                "A compile-time type mismatch error occurs",
                "A NullPointerException is thrown at runtime during the automatic .intValue() invocation",
                "num is set to Integer.MIN_VALUE"
            },
            2, // C
            "Auto-unboxing calls val.intValue() under the hood. Since val is null, dereferencing it triggers a java.lang.NullPointerException at runtime.",
            "Core Java Pitfalls",
            1.0
        ));

        list.add(new Question(
            10,
            "Which of the following rules applies when overriding a method in a Java subclass?",
            new String[]{
                "The overriding method can declare broader checked exceptions than the superclass method",
                "The overriding method can have a more restrictive access modifier (e.g., public to private)",
                "The overriding method cannot reduce visibility and cannot declare new or broader checked exceptions",
                "The overriding method must change the return type to java.lang.Object"
            },
            2, // C
            "Method overriding rules state that the overriding method cannot have more restrictive access (e.g., public cannot become protected) and cannot throw new or broader checked exceptions.",
            "Inheritance & OOP",
            1.0
        ));

        list.add(new Question(
            11,
            "What is the difference between Thread.sleep() and Object.wait() in Java concurrency?",
            new String[]{
                "Thread.sleep() releases object monitors; Object.wait() keeps all locks acquired",
                "Thread.sleep() does not release the monitor lock; Object.wait() releases the monitor lock and waits for notify()/notifyAll()",
                "Both can only be called from outside a synchronized block",
                "Object.wait() is a static method; Thread.sleep() is an instance method"
            },
            1, // B
            "sleep() suspends the thread without releasing any synchronized locks. wait() releases the monitor on the synchronized object, allowing other threads to acquire it.",
            "Multithreading",
            1.0
        ));

        list.add(new Question(
            12,
            "Why is String designed to be immutable in Java?",
            new String[]{
                "To optimize String Constant Pool caching, thread safety without synchronization, and security for class loading/network sockets",
                "Because Java does not support array-based characters",
                "To force developers to use StringBuffer for all operations",
                "To prevent strings from ever being collected by the Garbage Collector"
            },
            0, // A
            "String immutability enables safe sharing in the String Pool, inherently guarantees thread safety across threads, allows hashCode caching, and secures sensitive parameters like file paths and database URLs.",
            "Core Java Design",
            1.0
        ));

        list.add(new Question(
            13,
            "Which of the following is an effective technique to prevent memory leaks in Java enterprise applications?",
            new String[]{
                "Calling System.gc() after every method invocation",
                "Closing resources (streams, connections) and clearing static collections or listener registrations when no longer needed",
                "Declaring all variables as static",
                "Setting heap size to the maximum available system RAM"
            },
            1, // B
            "Java memory leaks commonly arise from forgotten object references in static collections, unclosed I/O streams/database connections, or unregistered listeners/callbacks.",
            "Performance & JVM",
            1.0
        ));

        list.add(new Question(
            14,
            "What is the difference between Comparable and Comparator interfaces in Java?",
            new String[]{
                "Comparable provides multiple custom sorting orders; Comparator provides only natural sorting",
                "Comparable is implemented by the class itself via compareTo(); Comparator is implemented as a separate sorting strategy via compare()",
                "Comparable works only for primitive types, while Comparator works only for Collections",
                "There is no functional difference; they are interchangeable aliases"
            },
            1, // B
            "Comparable imposes a single natural ordering directly inside the domain class (compareTo(T o)). Comparator represents external, pluggable ordering strategies (compare(T o1, T o2)).",
            "Collections & Sorting",
            1.0
        ));

        list.add(new Question(
            15,
            "Introduced as a final feature in Java 21, what are 'Virtual Threads' (Project Loom) primarily designed to achieve?",
            new String[]{
                "Replacing the Garbage Collector with manual C-style malloc/free memory control",
                "High-throughput concurrent server applications using lightweight, JVM-scheduled user-mode threads that do not tie up OS threads during blocking I/O",
                "Eliminating all checked exceptions across standard Java libraries",
                "Allowing direct execution of Java bytecode on GPU hardware"
            },
            1, // B
            "Virtual threads are lightweight threads managed directly by the JVM rather than 1:1 OS kernel threads. When a virtual thread encounters blocking I/O, the underlying carrier OS thread is unmounted to execute other tasks.",
            "Modern Java (Java 21)",
            1.0
        ));

        return Collections.unmodifiableList(list);
    }
}

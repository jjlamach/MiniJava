import Scanner.*;
import Parser.parser;
import Parser.sym;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java.io.*;
import java.util.List;

import AST.*;
import AST.Program;
import AST.Visitor.CodeTranslateVisitor;
//import AST.Statement;
import AST.Visitor.PrettyPrintVisitor;
import AST.Visitor.SemanticAnalysisVisitor;
import AST.Visitor.SymTableVisitor;

/**
 * 
 * @author Julio Lama
 *
 */
public class MiniJava {
	
	public static void main (String[] args) {
		int statusOfProgram = 0;
		System.out.println("Enter: 'java -cp build/classes:lib/java-cup-11b.jar MiniJava -S or -P or -T or "
				+ "-A"
				+ " ** Full path where your .java file is. ** '");
		if (args.length == 2 && args[0].equals("-S") && args[1].equals(null)) {
			System.out.println("Missing: name of the file to read.");
			statusOfProgram = 1;
			System.exit(statusOfProgram);
		}
		else if (args.length == 2 && args[0].equals("-S")) {
			System.out.println("Starting Lexical analysis...");
			statusOfProgram = scannerTester(args[1]);
		}
		else if (args.length == 2 && args[0].equals("-P")) {
			System.out.println("Starting Parser analysis...");
			statusOfProgram = parserTester(args[1]);
		}
		
		else if (args.length == 2 && args[0].equals("-T")) {
			System.out.println("Creating Symbol Tables");
			statusOfProgram = FileParserSymtab(args[1]);
		}
		else if (args.length == 2 && args[0].equals("-A")) {
			System.out.println("Starting Semantic analysis:");
			statusOfProgram = FileParserSemanticAnalysis(args[1]);
		}
		else if (args.length == 2 && args[0].equals("-C")) {
			System.out.println("Starting Code Generation: ");
			statusOfProgram = fileTranslator(args[1]);
			
		}
		else if (args.length == 2 && !(args[0].equals("-S")) || !(args[0].equals("-P")) || !(args[0].equals("-T"))) {
			System.out.printf("Wrong command line argument,'%s'\n", args[0]);
			statusOfProgram = 1;
			System.exit(statusOfProgram);
		}
		System.exit(statusOfProgram);
	}
	
	/**
	 * A function that tests the Scanner. Method taken from TestScanner.java
	 * @param fileToRead
	 * @return
	 */
	private static int scannerTester(String fileToRead) {
		int status = 0;
		try {
			ComplexSymbolFactory sf = new ComplexSymbolFactory();
			Reader in = new BufferedReader(new FileReader(fileToRead));
			scanner scanner = new scanner(in, sf);
			
			Symbol token = scanner.next_token();
			while (token.sym != sym.EOF) {
				/* If found an error, just return. */
				if (token.sym == sym.error) {
					status = 1;
					System.out.printf("Found an error, stopping lexical analysis, exiting with status: %d"
							+ ", %s\n", status, scanner.symbolToString(token));
					return status;
				}
				System.out.println(scanner.symbolToString(token) + " ");
				token = scanner.next_token();
			}
			System.out.println("Lexical analysis completed.");
		} 
		catch(Exception exception) {
			System.err.println("Error in parsing your tokens." + exception.toString());
			exception.printStackTrace();
			status = 1;
		}
		return status;
	}
	
	/**
	 * Function for testing the parser. Taken from TestParser.java
	 * @param fileToRead
	 * @return
	 */
	public static int parserTester(String fileToRead) {
		int status = 0;
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(fileToRead));
	        
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();
            Program program = (Program)root.value;
            
            /**
             * You should add Visitor code to print a nicely indented 
             * representation of the AST on standard output using the supplied PrettyPrintVisitor class
             */
            /*
             *   public Program(MainClass am, ClassDeclList acl, Location pos) {
    				super(pos);
    				m=am; cl=acl; 
  				}
             */
			program.accept(new PrettyPrintVisitor());
	   
	        System.out.println("\nParsing completed for testing Parser.");
	        System.exit(status);
	    } 
	    catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	        status = 1;
	        System.exit(status);
	    }
	    return status;
	}
	/**
	 * 
	 * @param fileToRead
	 * @return
	 */
	public static int FileParserSymtab(String fileToRead){
		int status = 0;
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(fileToRead));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();  
            Program program = (Program)root.value;
            
            SymTableVisitor symbolTableVisitor = new SymTableVisitor();
            program.accept(symbolTableVisitor);
            symbolTableVisitor.print();
	    
	        System.out.println("\nParsing completed, for Symbol Table Visitor."); 
	        System.exit(status);
	    } 
	    catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	        status = 1;
	        System.exit(status);
	    }
	    
	    return status;
	}
	
	/**
	 * Semantic Analysis.
	 * @param fileToRead
	 * @return
	 */
	public static int FileParserSemanticAnalysis(String fileToRead) {
		int errors = 0;
		int status = 0;
		try {
			// create a parser on the input file
			ComplexSymbolFactory sf = new ComplexSymbolFactory();
			Reader in = new BufferedReader(new FileReader(fileToRead));
			scanner s = new scanner(in, sf);
			parser p = new parser(s, sf);
			Symbol root;
			// replace p.parse() with p.debug_parse() in next line to see trace of
			// parser shift/reduce actions during parse
			root = p.parse();
			Program program = (Program) root.value;
			
			/* To print the ".java" file being analyzed. */
			program.accept(new PrettyPrintVisitor());
			
			SymTableVisitor st = new SymTableVisitor();
			program.accept(st);
			
			SemanticAnalysisVisitor sa = new SemanticAnalysisVisitor();
			sa.setSymtab(st.getSymtab());
			program.accept(sa);
			errors += sa.errors;

			System.out.println("\nCompiler completed");
			System.out.println(errors + " errors were found for Semantic Analysis.");
			System.exit(status);
		} 
		catch (Exception e) {
			// yuck: some kind of error in the compiler implementation
			// that we're not expecting (a bug!)
			System.err.println("Unexpected internal compiler error: " + e.toString());
			// print out a stack dump
			e.printStackTrace();
			status = 1;
			System.exit(status);
		}
		return status;
	}
	
	
	/**
	 * Code Translator.
	 * @param fileToRead
	 * @return
	 */
	public static int fileTranslator(String fileToRead){
		int status = 0;
	    try {
	        // create a parser on the input file
            ComplexSymbolFactory sf = new ComplexSymbolFactory();
	        Reader in = new BufferedReader(new FileReader(fileToRead));
            scanner s = new scanner(in, sf);
            parser p = new parser(s, sf);
            Symbol root;
		    // replace p.parse() with p.debug_parse() in next line to see trace of
		    // parser shift/reduce actions during parse
            root = p.parse();  
            Program program = (Program)root.value;
            
            
            SymTableVisitor st = new SymTableVisitor();
            program.accept( st );

            
            SemanticAnalysisVisitor sa = new SemanticAnalysisVisitor();
            sa.setSymtab(st.getSymtab());
            program.accept( sa );
            
            CodeTranslateVisitor ct = new CodeTranslateVisitor();
            ct.setSymtab(st.getSymtab());
            program.accept( ct );
            
            System.out.println("Code Translation completed.");
            System.exit(status);

	    } 
	    catch (Exception e) {
	        // yuck: some kind of error in the compiler implementation
	        // that we're not expecting (a bug!)
	        System.err.println("Unexpected internal compiler error: " + 
	                    e.toString());
	        // print out a stack dump
	        e.printStackTrace();
	        status = 1;
	        System.exit(status);
	    }
	    return status;
	}	
	
}
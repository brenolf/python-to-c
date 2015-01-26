import AST.*;
import java.io.*;

public class Main {
	
	public static void main( String []args ) {
		
		File file;
		FileReader stream;
		int numChRead;
		FileInput program;
		boolean quiet = false;
		
		if (args.length != 1 && args.length != 2 && args.length != 3)  {
			System.out.println("Usage:\n	Main input [--quiet] [output]");
			System.out.println("input is the file to be compiled");
			System.out.println("--quiet produces output suitable for logging, omitting Java stack.");
			System.out.println("output is the file where the generated code will be stored");
		}
		else {
			file = new File(args[0]);
			
			if ( ! file.exists() || ! file.canRead() ) {
				System.out.println("Either the file " + args[0] + " does not exist or it cannot be read");
				return ;
			}

			try { 
				stream = new FileReader(file);  
			} catch ( FileNotFoundException e ) {
				System.out.println("Something wrong: file does not exist anymore");
				throw new RuntimeException();
			}
				// one more character for '\0' at the end that will be added by the
				// compiler
			char []input = new char[ (int ) file.length() + 1 ];
			
			try {
				numChRead = stream.read( input, 0, (int ) file.length() );
			} catch ( IOException e ) {
				System.out.println("Error reading file " + args[0]);
				return ;
			}

			if ( numChRead != file.length() ) {
				System.out.println("Read error");
				return ;
			}
			try {
				stream.close();
			} catch ( IOException e ) {
				System.out.println("Error in handling the file " + args[0]);
				return ;
			}


			Compiler compiler = new Compiler();
			
			String outputFileName = args[0];
			int lastIndex;
			if ( (lastIndex = outputFileName.lastIndexOf('.')) == -1 )
				lastIndex = outputFileName.length();
			StringBuffer sb = new StringBuffer(outputFileName.substring(0, lastIndex));
			sb.append(".c");
			outputFileName = sb.toString();

			if ( args.length == 2 || args.length == 3 ){
				if(args.length == 3){
					quiet = true;
					outputFileName = args[2];
				} else {
					if(args[1].equals("--quiet"))
						quiet = true;
					else
						outputFileName = args[1];
				}
			}
			
			FileOutputStream  outputStream;
			try { 
				outputStream = new FileOutputStream(outputFileName);
			} catch ( IOException e ) {
				System.out.println("File " + outputFileName + " was not found");
				return ;
			}
			PrintWriter printWriter = new PrintWriter(outputStream);
			program = null;
			  // the generated code goes to a file and so are the errors
			program  = compiler.compile(input, new PrintWriter(System.out), quiet);
			
			if ( program != null ) {
				Pw pw = new Pw();
				pw.set(printWriter);
				program.genC( pw );
				if ( printWriter.checkError() ) {
					System.out.println("There was an error in the output");
				}
			}
		}
	}
}

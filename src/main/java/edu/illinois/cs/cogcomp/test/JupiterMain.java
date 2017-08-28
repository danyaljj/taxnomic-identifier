package edu.illinois.cs.cogcomp.test;

import org.apache.log4j.PropertyConfigurator;

import edu.illinois.cs.cogcomp.utils.InteractiveShell;

/**
 * 
 * @author dxquang
 *
 */

public class JupiterMain {

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		
		PropertyConfigurator.configure("jupiter-log4j.properties");
		
		InteractiveShell<AllTests> tester = new InteractiveShell<AllTests>(AllTests.class);
		
		if(args.length == 0)
			tester.ShowDocumentation();
		else
			tester.RunCommand(args);
		
	}

}

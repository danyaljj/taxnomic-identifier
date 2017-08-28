package edu.illinois.cs.cogcomp.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;

public class InteractiveShell<T>
{
	Class<T> type;
	
	public InteractiveShell(Class<T> className)
	{
		type = className;		
	}
	
	
	public void ShowDocumentation()
	{
		File jarFile = new File("Jupiter.jar");
		long lastModified = jarFile.lastModified();
		
		Date d = new Date(lastModified);
		System.out.println("Jupiter.jar. Last modified: " + d.toString());
		
		System.out.println("\nAvailable Commands:\n\n");
		int i =1;
		for(Method elem: type.getMethods())
		{
			if(Modifier.isStatic(elem.getModifiers()))
			{
			
				System.out.println(i + ". " + elem.getName());
				i++;
				if(elem.isAnnotationPresent(CommandDescription.class))
				{
					System.out.println(elem.getAnnotation(CommandDescription.class).description());
				}
				else
				{
					System.out.println("No documentation available");
				}
				
				System.out.println();
			}
		}
	}
	
	
		
	public void RunCommand(String[] args) throws Exception
	{

		String[] ss = new String[args.length -1];
		for(int i=1;i<args.length;i++)
		{
			ss[i-1] = args[i];
		}
		
		
		boolean foundMethod = false;
		
		Method[] mList = type.getMethods();
		for(Method m: mList)
		{
			if(Modifier.isStatic(m.getModifiers()))
			{
				if(m.getName().equals(args[0]))
				{
					foundMethod = true;
					try
					{
						m.invoke(null, (Object[])ss);
					}
					catch(Exception ex)
					{
						System.out.println("ERROR:");
						ex.printStackTrace();
						
						System.out.println("Documentation");						
						
						if(m.isAnnotationPresent(CommandDescription.class))
						{
							
							System.out.println(m.getAnnotation(CommandDescription.class).description());
						}
						else
						{
							System.out.println("No documentation available");
						}
						
						System.out.println();
					}
				}
			}
		}
		
		if(!foundMethod)
		{
			System.out.println("Unable to find " + args[0]);
			this.ShowDocumentation();
		}
	}
	
	
}

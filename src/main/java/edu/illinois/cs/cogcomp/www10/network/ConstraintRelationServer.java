/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.network;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import edu.illinois.cs.cogcomp.www10.constraints.MainConstraintRelationIdentification;

/**
 * @author dxquang Jun 19, 2009
 */
public class ConstraintRelationServer {

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Please give the port as the first argument.");
			System.exit(1);
		}
	
		String port = args[0];
		int iPort = Integer.parseInt(port);
		
		try {

			System.out.println("Attempting to start XML-RPC Server...");

			WebServer server = new WebServer(iPort);

			XmlRpcServer xmlRpcServer = server.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();

			phm.addHandler("MainConstraintRelationIdentification",
					MainConstraintRelationIdentification.class);

			xmlRpcServer.setHandlerMapping(phm);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer
					.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(false);

			server.start();

			System.out.println("Started successfully.");
			System.out.println("Accepting requests. (Halt program to stop.)");

		} catch (Exception exception) {
			System.err.println("RelationServer: " + exception);
		}
	}

}

/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.network;

import java.net.URL;
import java.util.HashMap;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

/**
 * @author dxquang Jun 19, 2009
 */
public class BareRelationClient {

	public static void main(String[] args) {

		if (args.length != 4) {
			System.out
					.println("Please give the host and port as the first two arguments, and the two concepts in the other two arguments.");
			System.exit(1);
		}

		String host = args[0];
		String port = args[1];
		String concept1 = args[2];
		String concept2 = args[3];

		try {

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			// config.setServerURL(new URL("http://127.0.0.1:6789/xmlrpc"));
			config.setServerURL(new URL("http://" + host + ":" + port
					+ "/xmlrpc"));
			config.setEnabledForExtensions(true);
			config.setConnectionTimeout(60 * 1000);
			config.setReplyTimeout(60 * 1000);

			XmlRpcClient client = new XmlRpcClient();
			client
					.setTransportFactory(new XmlRpcCommonsTransportFactory(
							client));
			client.setConfig(config);

			HashMap<String, String> mapNames = new HashMap<String, String>();
			mapNames.put("FIRST_STRING", concept1);
			mapNames.put("SECOND_STRING", concept2);

			Object[] params = new Object[] { mapNames };

			HashMap<String, String> result = (HashMap<String, String>) client
					.execute("MainRelationIdentification.identify", params);

			System.out.println("Score: " + result.get("SCORE"));
			System.out.println("Relation: " + result.get("REASON"));

		} catch (Exception exception) {
			System.err.println("JavaClient: " + exception);
		}
	}

}

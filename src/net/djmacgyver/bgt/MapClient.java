package net.djmacgyver.bgt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;

import com.google.android.maps.GeoPoint;

public class MapClient extends Thread {
	private UserOverlay users;
	private HttpClient client;
	private Context context;
	private boolean terminate = false;
	
	public MapClient(UserOverlay users, Context context) {
		this.users = users;
		this.context = context;
	}
	
	private HttpClient getClient()
	{
		if (client == null) {
			client = new HttpClient(context);
		}
		return client;
	}

	@Override
	public void run() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder builder;
		InputSource in = new InputSource();
		XPathFactory xfactory = XPathFactory.newInstance();
		XPath x = xfactory.newXPath();
		XPathExpression userExpr;
		XPathExpression locationExpr;
		XPathExpression quitExpr;
		try {
			userExpr = x.compile("/movements/user");
			locationExpr = x.compile("location[1]");
			quitExpr = x.compile("/quit/user");
		} catch (XPathExpressionException e2) {
			e2.printStackTrace();
			return;
		}
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
		while (!terminate) try {
			HttpGet req = new HttpGet("https://djmacgyver.homelinux.org/bgt/stream");
			HttpEntity entity = getClient().execute(req).getEntity();
			if (!entity.isStreaming()) return;
			
			this.users.reset();
			
			InputStream is = entity.getContent();
			byte[] buf = new byte[4096];
			int read = 0;
			while (!terminate) do {
				read = is.read(buf);
				if (terminate) break;
				//System.out.println("read " + read + " bytes!");
				in.setByteStream(new ByteArrayInputStream(buf, 0, read));
				Document dom = builder.parse(in);
				
				// get location updates
				NodeList users = (NodeList) userExpr.evaluate(dom, XPathConstants.NODESET);
				for (int i = 0; i < users.getLength(); i++) {
					Node location = (Node) locationExpr.evaluate(users.item(i), XPathConstants.NODE);
					int lat = 0, lon = 0;
					for (int k = 0; k < location.getChildNodes().getLength(); k++) {
						Node coord = location.getChildNodes().item(k);
						int value = (int) (Float.parseFloat(coord.getTextContent()) * 1E6);
						if (coord.getNodeName().equals("lat")) lat = value;
						if (coord.getNodeName().equals("lon")) lon = value;
					}
					GeoPoint point = new GeoPoint(lat, lon);
					int userId = Integer.parseInt(users.item(i).getAttributes().getNamedItem("id").getNodeValue());
					this.users.updateUser(userId, point);
				}
				
				// get removals
				users = (NodeList) quitExpr.evaluate(dom, XPathConstants.NODESET);
				for (int i = 0; i < users.getLength(); i++) {
					int userId = Integer.parseInt(users.item(i).getAttributes().getNamedItem("id").getNodeValue());
					this.users.removeUser(userId);
				}
			} while (read >= 0);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
			terminate();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			terminate();
		}
		getClient().getConnectionManager().shutdown();
		System.out.println("MapClient Thread ended");
	}
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
}

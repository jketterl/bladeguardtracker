package net.djmacgyver.bgt.downstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.http.HttpClient;
import net.djmacgyver.bgt.map.RouteOverlay;
import net.djmacgyver.bgt.map.UserOverlay;
import net.djmacgyver.bgt.map.UserOverlayItem;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.android.maps.GeoPoint;

public class HttpStreamingConnection extends Thread {
	private UserOverlay users;
	private RouteOverlay route;
	private HttpClient client;
	private Context context;
	private boolean terminate = false;
	private Handler length;
	private XPath xPath;
	private XPathExpression pointExpression;
	private XPathExpression userExpression;
	private XPathExpression locationExpression;
	private XPathExpression quitExpression;
	private XPathExpression mapExpression;
	private XPathExpression statsExpression;
	
	public HttpStreamingConnection(UserOverlay users, RouteOverlay route, Context context, Handler h) {
		this.users = users;
		this.context = context;
		this.route = route;
		this.length = h;
		
		try {
			userExpression = getXPath().compile("/updates/movements/user");
			locationExpression = getXPath().compile("location[1]");
			quitExpression = getXPath().compile("/updates/quit/user");
			mapExpression = getXPath().compile("/updates/map[1]");
			pointExpression = getXPath().compile("gpx:gpx/gpx:rte/gpx:rtept");
			statsExpression = getXPath().compile("/updates/stats[1]");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			terminate();
		}
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
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
		while (!terminate) try {
			HttpGet req = new HttpGet(context.getResources().getString(R.string.base_url) + "stream");
			HttpEntity entity = getClient().execute(req).getEntity();
			if (!entity.isStreaming()) return;
			
			this.users.reset();
			
			InputStream is = entity.getContent();
			byte[] buf = new byte[4096];
			int read = 0;
			while (!terminate) do {
				String xml = "";
				do {
					read = is.read(buf);
					xml = xml.concat(new String(buf, 0, read));
				} while (read == 4096);

				if (terminate) break;
				in.setCharacterStream(new StringReader(xml));
				Document dom = builder.parse(in);
				
				parseData(dom);
			} while (read >= 0);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
			terminate();
		}
		getClient().getConnectionManager().shutdown();
	}
	
	protected XPath getXPath() {
		if (xPath == null) {
			XPathFactory xfactory = XPathFactory.newInstance();
			XPath x = xfactory.newXPath();
			x.setNamespaceContext(new NamespaceContext() {
				@Override
				public Iterator<String> getPrefixes(String namespaceURI) {
					return null;
				}
				
				@Override
				public String getPrefix(String namespaceURI) {
					return null;
				}
				
				@Override
				public String getNamespaceURI(String prefix) {
					if (prefix.equals("gpx")) return "http://www.topografix.com/GPX/1/1";
					return null;
				}
			});
		}
		return xPath;
	}
	
	protected void parseData(Document dom) {
		try {
			parseUserUpdates(dom);
			parseUserRemovals(dom);
			parseMapData(dom);
			parseStatisticsUpdates(dom);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			terminate();
		}
	}

	private void parseStatisticsUpdates(Document dom) throws XPathExpressionException {
		Node stats = (Node) statsExpression.evaluate(dom, XPathConstants.NODE);
		if (stats != null) {
			String l = stats.getChildNodes().item(0).getTextContent();
			Message msg = new Message();
			try {
				float le = Float.parseFloat(l);
				DecimalFormat df = new DecimalFormat("0.###");
				msg.obj = "Zuglänge: " + df.format(le) + " km";
			} catch (NumberFormatException e) {
				msg.obj = "Zuglänge derzeit unbekannt";
			}
			this.length.sendMessage(msg);
		}
	}

	private void parseMapData(Document dom) throws XPathExpressionException {
		Node map = (Node) mapExpression.evaluate(dom, XPathConstants.NODE);
		if (map != null) {
			NodeList points = (NodeList) pointExpression.evaluate(map, XPathConstants.NODESET);
			GeoPoint[] geoPoints = new GeoPoint[points.getLength()];
			for (int i = 0; i < points.getLength(); i++) {
				GeoPoint gPoint = new GeoPoint(
						(int) (Float.parseFloat(points.item(i).getAttributes().getNamedItem("lat").getNodeValue()) * 1E6),
						(int) (Float.parseFloat(points.item(i).getAttributes().getNamedItem("lon").getNodeValue()) * 1E6)
				);
				geoPoints[i] = gPoint;
			}
			
			route.setPoints(geoPoints);
		}
	}

	private void parseUserRemovals(Document dom) throws XPathExpressionException {
		// get removals
		NodeList users = (NodeList) quitExpression.evaluate(dom, XPathConstants.NODESET);
		for (int i = 0; i < users.getLength(); i++) {
			int userId = Integer.parseInt(users.item(i).getAttributes().getNamedItem("id").getNodeValue());
			this.users.removeUser(userId);
		}
	}

	private void parseUserUpdates(Document dom) throws XPathExpressionException {
		// get location updates
		NodeList users = (NodeList) userExpression.evaluate(dom, XPathConstants.NODESET);
		for (int i = 0; i < users.getLength(); i++) {
			Node location = (Node) locationExpression.evaluate(users.item(i), XPathConstants.NODE);
			int lat = 0, lon = 0;
			for (int k = 0; k < location.getChildNodes().getLength(); k++) {
				Node coord = location.getChildNodes().item(k);
				int value = (int) (Float.parseFloat(coord.getTextContent()) * 1E6);
				if (coord.getNodeName().equals("lat")) lat = value;
				if (coord.getNodeName().equals("lon")) lon = value;
			}
			GeoPoint point = new GeoPoint(lat, lon);
			Node user = users.item(i);
			int userId = Integer.parseInt(user.getAttributes().getNamedItem("id").getNodeValue());
			UserOverlayItem o = this.users.getUser(userId);
			if (o != null) {
				o.setPoint(point);
			} else {
				String userName = user.getAttributes().getNamedItem("name").getNodeValue();
				String team = user.getAttributes().getNamedItem("team").getNodeValue();
				this.users.addUser(new UserOverlayItem(point, userId, userName, team));
			}
		}
	}
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
}

package net.djmacgyver.bgt.downstream;

import java.text.DecimalFormat;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.maps.GeoPoint;

import net.djmacgyver.bgt.map.RouteOverlay;
import net.djmacgyver.bgt.map.UserOverlay;
import net.djmacgyver.bgt.map.UserOverlayItem;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class MapStreamingConnection extends HttpStreamingConnection {
	private UserOverlay users;
	private RouteOverlay route;
	private Handler length;
	private XPathExpression pointExpression;
	private XPathExpression userExpression;
	private XPathExpression locationExpression;
	private XPathExpression quitExpression;
	private XPathExpression mapExpression;
	private XPathExpression statsExpression;
	private Handler handler;

	public MapStreamingConnection(UserOverlay users, RouteOverlay route, Context context, Handler h) {
		super(context);
		this.users = users;
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
	
	@Override
	protected Handler getHandler() {
		if (handler == null) {
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (!(msg.obj instanceof Document)) return;
					parseData((Document) msg.obj);
				}
			};
		}
		return handler;
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
	
	@Override
	protected void onReconnect() {
		this.users.reset();
	}

	private void parseData(Document dom) {
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
}

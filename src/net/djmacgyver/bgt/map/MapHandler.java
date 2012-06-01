package net.djmacgyver.bgt.map;

import java.text.DecimalFormat;
import java.util.Iterator;

import net.djmacgyver.bgt.activity.Map;
import net.djmacgyver.bgt.downstream.HttpStreamingConnection;
import net.djmacgyver.bgt.socket.HttpSocketListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Handler;
import android.os.Message;

import com.google.android.maps.GeoPoint;

public class MapHandler extends Handler {
	private Map map;
	
	public MapHandler(Map map) {
		this.map = map;
	}
	
	@Override
	public void handleMessage(Message msg) {
		JSONObject data = (JSONObject) msg.obj;
		System.out.println("got update: " + data);
		parseUserUpdates(data);
		parseUserRemovals(data);
		parseMapData(data);
		parseStatisticsUpdates(data);
	}

	private void parseStatisticsUpdates(JSONObject data) {
		if (!data.has("stats")) return;
		System.out.println("testing for stats...");
		JSONObject stats;
		try {
			System.out.println(data.get("stats"));
			stats = data.getJSONArray("stats").getJSONObject(0);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		System.out.println("got stats: " + stats);
		
		String text;
		double length = -1;
		text = "n/a";
		if (stats.has("bladeNightLength")) {
			try {
				length = stats.getDouble("bladeNightLength");
				DecimalFormat df = new DecimalFormat("0.#");
				text = df.format(length) + " km";
			} catch (NumberFormatException e) {} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		map.getLengthTextView().setText(text);
		
		/*
		Node speedNode = (Node) speedExpression.evaluate(stats, XPathConstants.NODE);
		double speed = -1;
		text = "n/a";
		if (speedNode != null) {
			try {
				speed = Double.parseDouble(speedNode.getTextContent());
				DecimalFormat df = new DecimalFormat("0.#");
				text = df.format(speed * 3.6) + " km/h";
			} catch (NumberFormatException e) {}
		}
		map.getSpeedTextView().setText(text);
		
		text = "n/a";
		if (length > 0 && speed > 0) {
			double cycleTime = (length * 1000 / speed) / 60;
			DecimalFormat df = new DecimalFormat("0");
			text = df.format(cycleTime) + " min";
		}
		map.getCycleTimeTextView().setText(text);
		*/
	}

	private void parseMapData(JSONObject data) {
		/*
		Node map = (Node) mapExpression.evaluate(data, XPathConstants.NODE);
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
			
			this.map.getRoute().setPoints(geoPoints);
		}
		*/
	}

	private void parseUserRemovals(JSONObject data) {
		/*
		// get removals
		NodeList users = (NodeList) quitExpression.evaluate(data, XPathConstants.NODESET);
		for (int i = 0; i < users.getLength(); i++) {
			int userId = Integer.parseInt(users.item(i).getAttributes().getNamedItem("id").getNodeValue());
			map.getUserOverlay().removeUser(userId);
		}
		*/
	}

	private void parseUserUpdates(JSONObject data) {
		/*
		// get location updates
		NodeList users = (NodeList) userExpression.evaluate(data, XPathConstants.NODESET);
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
			UserOverlayItem o = map.getUserOverlay().getUser(userId);
			if (o != null) {
				o.setPoint(point);
			} else {
				String userName = user.getAttributes().getNamedItem("name").getNodeValue();
				String team = user.getAttributes().getNamedItem("team").getNodeValue();
				map.getUserOverlay().addUser(new UserOverlayItem(point, userId, userName, team));
			}
		}
		*/
	}
}
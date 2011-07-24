package net.djmacgyver.bgt;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class MapDownloaderThread extends Thread {
	private Context context;
	private RouteOverlay overlay;
	private Document map;
	private HttpClient client;
	private MapView mapView;

	private Document getMap() throws ClientProtocolException, IOException {
		if (map == null) {
			HttpUriRequest req = new HttpGet(Config.baseUrl + "map");
			HttpEntity e = getClient().execute(req).getEntity();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder builder;
			try {
				builder = dbf.newDocumentBuilder();
				//String xml = convertStreamToString();
				map = builder.parse(e.getContent());
				e.consumeContent();
			} catch (IllegalStateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			getClient().getConnectionManager().shutdown();
			client = null;
		}
		return map;
	}
	
	private HttpClient getClient() {
		if (client == null) {
			client = new HttpClient(context);
		}
		return client;
	}

	public MapDownloaderThread(Context context, RouteOverlay overlay, MapView mapView) {
		this.context = context;
		this.overlay = overlay;
		this.mapView = mapView;
	}
	
	@Override
	public void run() {
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
		XPathExpression pointExpr;
		
		try {
			pointExpr = x.compile("/gpx:gpx/gpx:rte/gpx:rtept");
			
			NodeList points = (NodeList) pointExpr.evaluate(getMap(), XPathConstants.NODESET);
			GeoPoint[] geoPoints = new GeoPoint[points.getLength()];
			for (int i = 0; i < points.getLength(); i++) {
				GeoPoint gPoint = new GeoPoint(
						(int) (Float.parseFloat(points.item(i).getAttributes().getNamedItem("lat").getNodeValue()) * 1E6),
						(int) (Float.parseFloat(points.item(i).getAttributes().getNamedItem("lon").getNodeValue()) * 1E6)
				);
				geoPoints[i] = gPoint;
			}
			
			overlay.setPoints(geoPoints);

			// zoom in to the route
			int minLat = 360000000, maxLat = 0, minLon = 360000000, maxLon = 0;
			for (int i = 0; i < geoPoints.length; i++) {
				minLat = Math.min(minLat, geoPoints[i].getLatitudeE6());
				maxLat = Math.max(maxLat, geoPoints[i].getLatitudeE6());
				minLon = Math.min(minLon, geoPoints[i].getLongitudeE6());
				maxLon = Math.max(maxLon, geoPoints[i].getLongitudeE6());
			}
			mapView.getController().zoomToSpan(maxLat - minLat, maxLon - minLon);
			mapView.getController().setCenter(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

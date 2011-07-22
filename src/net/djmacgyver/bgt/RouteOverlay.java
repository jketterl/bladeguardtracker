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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RouteOverlay extends Overlay {
	private Document map;
	private HttpClient client;
	private Context context;
	
	public RouteOverlay(Context context) {
		this.context = context;
		try {
			this.getMap();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	private String convertStreamToString(InputStream is) throws Exception {
		  BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		  StringBuilder sb = new StringBuilder();
		  String line = null;
		  while ((line = reader.readLine()) != null) {
			  sb.append(line + "\n");
		  }
		  is.close();
		  return sb.toString();
	}
	*/

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

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
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
		Point previousPoint = null;
		
		Paint p = new Paint();
		p.setAntiAlias(true);

		try {
			pointExpr = x.compile("/gpx:gpx/gpx:rte/gpx:rtept");
			
			NodeList points = (NodeList) pointExpr.evaluate(getMap(), XPathConstants.NODESET);
			System.out.println(points.getLength());
			for (int i = 0; i < points.getLength(); i++) {
				Point point = new Point();
				GeoPoint gPoint = new GeoPoint(
						(int) (Float.parseFloat(points.item(i).getAttributes().getNamedItem("lat").getNodeValue()) * 1E6),
						(int) (Float.parseFloat(points.item(i).getAttributes().getNamedItem("lon").getNodeValue()) * 1E6)
				);
				mapView.getProjection().toPixels(gPoint, point);
				if (previousPoint != null) {
					canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, p);
				} else System.out.println("skipped one point");
				previousPoint = point;
			}
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

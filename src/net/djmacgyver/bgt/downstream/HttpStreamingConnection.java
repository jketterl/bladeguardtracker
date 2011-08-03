package net.djmacgyver.bgt.downstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;


abstract public class HttpStreamingConnection extends Thread {
	private HttpClient client;
	private Context context;
	private boolean terminate = false;
	private XPath xPath;
	
	public HttpStreamingConnection(Context context) {
		this.context = context;
	}
	
	private HttpClient getClient()
	{
		if (client == null) {
			client = new HttpClient(context);
		}
		return client;
	}
	
	protected void onReconnect() {
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
			
			onReconnect();
			
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
				try {
					Document dom = builder.parse(in);
					parseData(dom);
				} catch (SAXException e) {
					e.printStackTrace();
					terminate();
				}
			} while (read >= 0);
		} catch (IOException e) {
			//Error communicating with the server
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e1) {}
		}
		getClient().getConnectionManager().shutdown();
	}
	
	protected XPath getXPath() {
		if (xPath == null) {
			XPathFactory xfactory = XPathFactory.newInstance();
			xPath = xfactory.newXPath();
			xPath.setNamespaceContext(new NamespaceContext() {
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
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
	
	abstract protected void parseData(Document dom);
}

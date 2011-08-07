package net.djmacgyver.bgt.downstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;


public class HttpStreamingConnection extends Thread {
	private HttpClient client;
	private Context context;
	private boolean terminate = false;
	private String url;
	private Handler handler;
	
	public static final int CONNECT = 0;
	public static final int DISCONNECT = 1;
	public static final int TERMINATE = 2;
	public static final int BEFORECONNECT = 3;
	
	public HttpStreamingConnection(Context context, String url, Handler handler) {
		this.context = context;
		this.url = url;
		this.handler = handler;
	}
	
	protected Handler getHandler() {
		return this.handler;
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
			sendMessage(BEFORECONNECT);
			
			HttpGet req = new HttpGet(url);
			HttpEntity entity = getClient().execute(req).getEntity();
			if (!entity.isStreaming()) return;
			
			sendMessage(CONNECT);
			
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
					sendMessage(dom);
				} catch (SAXException e) {
					e.printStackTrace();
					terminate();
				}
			} while (read >= 0);

			sendMessage(DISCONNECT);
		} catch (IOException e) {
			sendMessage(DISCONNECT);

			//Error communicating with the server
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e1) {}
		}
		getClient().getConnectionManager().shutdown();
		sendMessage(TERMINATE);
	}
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
	
	private void sendMessage(Object o) {
		Message msg = new Message();
		msg.obj = o;
		getHandler().sendMessage(msg);
	}
}

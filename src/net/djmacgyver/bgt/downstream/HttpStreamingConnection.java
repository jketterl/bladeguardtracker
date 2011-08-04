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
		Message msg;
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
		while (!terminate) try {
			HttpGet req = new HttpGet(url);
			HttpEntity entity = getClient().execute(req).getEntity();
			if (!entity.isStreaming()) return;
			
			msg = new Message();
			msg.obj = CONNECT;
			this.getHandler().sendMessage(msg);
			
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

					msg = new Message();
					msg.obj = dom;
					getHandler().sendMessage(msg);
				} catch (SAXException e) {
					e.printStackTrace();
					terminate();
				}
			} while (read >= 0);

			msg = new Message();
			msg.obj = DISCONNECT;
			this.getHandler().sendMessage(msg);
		} catch (IOException e) {
			msg = new Message();
			msg.obj = DISCONNECT;
			this.getHandler().sendMessage(msg);

			//Error communicating with the server
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e1) {}
		}
		getClient().getConnectionManager().shutdown();
		msg = new Message();
		msg.obj = TERMINATE;
		this.getHandler().sendMessage(msg);
	}
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
}

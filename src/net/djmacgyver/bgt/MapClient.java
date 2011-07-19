package net.djmacgyver.bgt;

import java.io.IOException;
import java.io.InputStream;

import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;

public class MapClient extends Thread {
	private Map map;
	private HttpClient client;
	private Context context;
	
	public MapClient(Map map, Context context) {
		this.map = map;
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
		HttpGet req = new HttpGet("http://jketterl-nb.tech/bgt/query.php");
		try {
			HttpResponse res = getClient().execute(req);
			if (!res.getEntity().isStreaming()) return;
			
			InputStream i = res.getEntity().getContent();
			byte[] buf = new byte[4096];
			int read = 0;
			do {
				read = i.read(buf);
				System.out.println("read " + read + " bytes!");
				System.out.println(new String(buf, 0, read));
			} while (read >= 0);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

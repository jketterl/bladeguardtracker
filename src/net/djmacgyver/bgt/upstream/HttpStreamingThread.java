package net.djmacgyver.bgt.upstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import net.djmacgyver.bgt.Config;
import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;

import android.content.Context;

public class HttpStreamingThread extends Thread {
	private Context context;
	private HttpClient client;
	private OutputStream os;
	private boolean terminate = false;
	private Vector<byte[]> queue = new Vector<byte[]>();
	private int userId;
	
	public HttpStreamingThread() {
		Random r = new Random();
		this.userId = r.nextInt(100);
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	private HttpClient getClient() {
		if (client == null) {
			client = new HttpClient(this.context);
		}
		return client;
	}
	
	@Override
	public void run() {
		HttpPost req = new HttpPost(Config.baseUrl + "log?uid=" + userId);
		req.setEntity(new HttpEntity() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				os = outstream;
				Iterator<byte[]> i = queue.iterator();
				while (i.hasNext()) {
					os.write(i.next());
					os.flush();
				}
				System.out.println("queue done");
				while (!terminate) try {
					Thread.sleep(30000);
					os.write("keepalive".getBytes());
					os.flush();
				} catch (InterruptedException e) {}
				os.write("quit".getBytes());
				os.close();
			}
			
			@Override
			public boolean isStreaming() {
				return true;
			}
			
			@Override
			public boolean isRepeatable() {
				return false;
			}
			
			@Override
			public boolean isChunked() {
				return true;
			}
			
			@Override
			public Header getContentType() {
				return new BasicHeader("content-type", "text/xml");
			}
			
			@Override
			public long getContentLength() {
				return -1;
			}
			
			@Override
			public Header getContentEncoding() {
				return new BasicHeader("content-encoding", "utf-8");
			}
			
			@Override
			public InputStream getContent() throws IOException, IllegalStateException {
				System.out.println("getContent()");
				return null;
			}
			
			@Override
			public void consumeContent() throws IOException {
			}
		});
		try {
			getClient().execute(req);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendData(byte[] data) {
		if (os == null) {
			queue.add(data);
			return;
		}
		try {
			os.write(data);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
}

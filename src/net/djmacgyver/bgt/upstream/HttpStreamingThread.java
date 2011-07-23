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
	private class StreamingHttpEntity implements HttpEntity {
		private boolean terminateEntity = false;
		private OutputStream os;
		
		@Override
		public void writeTo(OutputStream outstream) throws IOException {
			os = outstream;
			Iterator<byte[]> i = queue.iterator();
			while (i.hasNext()) sendData(i.next());
			queue = new Vector<byte[]>();
			while (!terminate && !terminateEntity) try {
				Thread.sleep(50000);
				sendData("keepalive".getBytes());
			} catch (InterruptedException e) {}
			sendData("quit".getBytes());
			os.close();
			os = null;
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
			// dummy implementation
			return null;
		}
		
		@Override
		public void consumeContent() throws IOException {
			os.close();
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
				this.terminateEntity = true;
			}
			interrupt();
		}
	}
	
	private Context context;
	private HttpClient client;
	private boolean terminate = false;
	private Vector<byte[]> queue = new Vector<byte[]>();
	private int userId;
	private StreamingHttpEntity entity;
	
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
	
	private StreamingHttpEntity getEntity() {
		if (entity == null) {
			entity = new StreamingHttpEntity();
		}
		return entity;
	}
	
	@Override
	public void run() {
		while (!terminate) try {
			HttpPost req = new HttpPost(Config.baseUrl + "log?uid=" + userId);
			req.setEntity(getEntity());
			getClient().execute(req).getEntity().consumeContent();
			entity = null;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getClient().getConnectionManager().shutdown();
	}
	
	public void sendData(byte[] data) {
		getEntity().sendData(data);
	}
	
	public void terminate() {
		terminate = true;
		interrupt();
	}
}

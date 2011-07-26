package net.djmacgyver.bgt.upstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

class StreamingHttpEntity implements HttpEntity {
	private boolean terminate = false;
	private OutputStream os;
	private Thread thread;
	Vector<byte[]> queue = new Vector<byte[]>();
	
	public StreamingHttpEntity(Thread t) {
		this.thread = t;
	}
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		Iterator<byte[]> i = queue.iterator();
		while (i.hasNext()) sendData(i.next(), outstream);
		os = outstream;
		queue = new Vector<byte[]>();
		while (!terminate) try {
			Thread.sleep(50000);
			sendData("keepalive");
		} catch (InterruptedException e) {}
		sendData("quit");
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
	
	public void sendData(String data) {
		sendData(data.getBytes());
	}
	
	public void sendData(byte[] data, OutputStream os) {
		try {
			os.write(data);
			os.flush();
		} catch (IOException e) {
			terminate = true;
		}
		thread.interrupt();
	}
	
	public void sendData(byte[] data) {
		if (os == null) {
			queue.add(data);
			return;
		}
		sendData(data, os);
	}
	
	public void terminate() {
		terminate = true;
		thread.interrupt();
	}
}
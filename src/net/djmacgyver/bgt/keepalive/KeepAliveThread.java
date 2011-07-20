package net.djmacgyver.bgt.keepalive;

public class KeepAliveThread extends Thread {
	private KeepAliveTarget target;
	private int interval;
	private boolean terminate = false;
	
	public KeepAliveThread(KeepAliveTarget target, int interval) {
		this.target = target;
		this.interval = interval;
	}
	
	@Override
	public void run() {
		while (!terminate) {
			target.keepAlive(this);
			try {
				sleep(interval * 1000);
			} catch (InterruptedException e) {}
		}
	}
	
	public void terminate()
	{
		this.terminate = true;
		interrupt();
	}
}

package net.djmacgyver.bgt;


public class MapRefresher extends Thread {
	private Map map;
	private int interval;
	private boolean terminate = false;
	
	public MapRefresher(Map map, int interval) {
		this.map = map;
		this.interval = interval;
	}
	
	@Override
	public void run() {
		while (!terminate) {
			this.map.refresh();
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

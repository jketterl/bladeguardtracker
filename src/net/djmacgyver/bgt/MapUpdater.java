package net.djmacgyver.bgt;


public class MapUpdater extends Thread {
	private Map map;
	private int interval;
	
	public MapUpdater(Map map, int interval) {
		this.map = map;
		this.interval = interval;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				sleep(interval * 1000);
			} catch (InterruptedException e) {}
			this.map.refresh();
		}
	}
}

package net.djmacgyver.bgt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RouteOverlay extends Overlay {
	private GeoPoint[] points;
	private MapDownloaderThread downloader;
	
	public RouteOverlay(Context context) {
		downloader = new MapDownloaderThread(context, this);
		downloader.start();
	}
	
	public void setPoints(GeoPoint[] points) {
		this.points = points;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (points == null) return;

		Paint p = new Paint();
		p.setAntiAlias(true);
		Point previousPoint = null;
		
		for (int i = 0; i < points.length; i++) {
			Point point = new Point();
			mapView.getProjection().toPixels(points[i], point);
			if (previousPoint != null) {
				canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, p);
			}
			previousPoint = point;
		}
	}
}

package net.djmacgyver.bgt.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RouteOverlay extends Overlay {
	private GeoPoint[] points;
	private Paint paint;
	private MapView view;
	
	public RouteOverlay(MapView view) {
		this.view = view;
	}
	
	public synchronized void setPoints(GeoPoint[] points) {
		this.points = points;
		// zoom in to the route
		if (points.length == 0) return;
		int minLat = 90000000, maxLat = -90000000, minLon = 180000000, maxLon = -180000000;
		for (int i = 0; i < points.length; i++) {
			minLat = Math.min(minLat, points[i].getLatitudeE6());
			maxLat = Math.max(maxLat, points[i].getLatitudeE6());
			minLon = Math.min(minLon, points[i].getLongitudeE6());
			maxLon = Math.max(maxLon, points[i].getLongitudeE6());
		}
		view.getController().zoomToSpan(maxLat - minLat, maxLon - minLon);
		view.getController().setCenter(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
	}
	
	public Paint getPaint() {
		if (paint == null) {
			paint = new Paint();
		}
		return paint;
	}
	
	@Override
	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (points == null) return;

		Point previousPoint = null;
		
		for (int i = 0; i < points.length; i++) {
			Point point = new Point();
			mapView.getProjection().toPixels(points[i], point);
			if (previousPoint != null) {
				canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, getPaint());
			}
			previousPoint = point;
		}
	}
}

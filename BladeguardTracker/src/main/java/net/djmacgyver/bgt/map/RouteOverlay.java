package net.djmacgyver.bgt.map;

import net.djmacgyver.bgt.R;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class RouteOverlay {
	private GeoPoint[] points;
	private Paint paint, trackPaint;
	private GoogleMap map;
	private int from = -1, to = -1;
	
	public RouteOverlay(GoogleMap map) {
        this.map = map;
	}

	public synchronized void setPoints(GeoPoint[] points) {
		this.points = points;
		// zoom in to the route
		if (points.length == 0) return;
		int minLat = 90000000, maxLat = -90000000, minLon = 180000000, maxLon = -180000000;
        PolylineOptions routeOptions = new PolylineOptions();

        for (GeoPoint point : points) {
            minLat = Math.min(minLat, point.getLatitudeE6());
            maxLat = Math.max(maxLat, point.getLatitudeE6());
            minLon = Math.min(minLon, point.getLongitudeE6());
            maxLon = Math.max(maxLon, point.getLongitudeE6());
            LatLng p = new LatLng(point.getLatitudeE6() / 1e6, point.getLongitudeE6() / 1e6);
            routeOptions.add(p);
        }

        Polyline route = map.addPolyline(routeOptions);
        /*
		map.getController().zoomToSpan(maxLat - minLat, maxLon - minLon);
		map.getController().setCenter(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
		*/
	}

    /*
	private Paint getRegularPaint() {
		if (paint == null) {
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLUE);
			paint.setAlpha(64);
			paint.setStrokeWidth(map.getResources().getDimensionPixelSize(R.dimen.mapStrokeWidth));
		}
		return paint;
	}
	
	private Paint getTrackPaint() {
		if (trackPaint == null) {
			trackPaint = new Paint();
			trackPaint.setAntiAlias(true);
			trackPaint.setColor(Color.rgb(255, 192, 0));
			//trackPaint.setAlpha(192);
			trackPaint.setStrokeWidth(map.getResources().getDimension(R.dimen.trackStrokeWidth));
		}
		return trackPaint;
	}
	
	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		if (points == null) return;

		Point previousPoint = null;
		int length = points.length;
		int i = from;
		if (from >= 0 && to >= 0 && from < length && to < length) {
			// if the current part of the map is occupied, draw it twice:
			// once in highlighted, once in regular
			while (i != to) {
				Point point = new Point();
				mapView.getProjection().toPixels(points[i], point);
				
				if (previousPoint != null) {
					canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, getTrackPaint());
				}
				previousPoint = point;
				
				i++;
				if (i >= length) i = 0;
			}
		}
		
		previousPoint = null;
		for (i = 0; i < points.length; i++) {
			Point point = new Point();
			mapView.getProjection().toPixels(points[i], point);
			if (previousPoint != null) {
				canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, getRegularPaint());
			}
			previousPoint = point;
		}

	}
	*/
	
	public void setBetween(int from, int to) {
		this.from = from;
		this.to = to;
	}
}

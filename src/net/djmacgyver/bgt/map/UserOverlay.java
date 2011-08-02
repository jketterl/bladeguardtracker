package net.djmacgyver.bgt.map;

import java.util.HashMap;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.activity.Map;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class UserOverlay extends ItemizedOverlay<OverlayItem> {
	private HashMap<Integer, OverlayItem> mOverlays = new HashMap<Integer, OverlayItem>();
	private Map map;
	private RelativeLayout bubble;

	public UserOverlay(Drawable defaultMarker, Map map) {
		super(boundCenter(defaultMarker));
		populate();
		this.map = map;
	}

	public synchronized void updateUser(int userId, GeoPoint point) {
		OverlayItem i = mOverlays.get(userId);
		if (i != null)
			mOverlays.remove(i);
		mOverlays.put(userId, new OverlayItem(point, Integer.toString(userId), "Bladeguard"));
		setLastFocusedIndex(-1);
		populate();
	}

	public synchronized void removeUser(int userId) {
		mOverlays.remove(userId);
		setLastFocusedIndex(-1);
		populate();
	}

	public synchronized void reset() {
		mOverlays.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return (OverlayItem) mOverlays.values().toArray()[i];
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
	}
	
	private RelativeLayout getBubble() {
		if (bubble == null) {
			LayoutInflater inflater = map.getLayoutInflater();
			bubble = (RelativeLayout) inflater.inflate(R.layout.bubble, map.getMap(), false);
			
			ImageView bubbleClose = (ImageView) bubble.findViewById(R.id.balloon_overlay_close);
			bubbleClose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bubble.setVisibility(View.GONE);
				}
			});
		}
		return bubble;
	}

	private void displayBubble(OverlayItem item) {
		// Hide the bubble if it's already showing for another result
		map.getMap().removeView(getBubble());
		bubble.setVisibility(View.GONE);

		// Set some view content
		TextView username = (TextView) getBubble().findViewById(R.id.username);
		username.setText(item.getTitle());
		
		TextView comment = (TextView) getBubble().findViewById(R.id.comment);
		comment.setText(item.getSnippet());

		// This is the important bit - set up a LayoutParams object for
		// positioning of the bubble.
		// This will keep the bubble floating over the GeoPoint
		// result.getPoint() as you move the MapView around,
		// but you can also keep the view in the same place on the map using a
		// different LayoutParams constructor
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				item.getPoint(), MapView.LayoutParams.BOTTOM_CENTER);

		getBubble().setLayoutParams(params);

		map.getMap().addView(bubble);
		// Measure the bubble so it can be placed on the map
		map.getMap().measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
							 MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		// Runnable to fade the bubble in when we've finished animatingTo our
		// OverlayItem (below)
		Runnable r = new Runnable() {
			public void run() {
				/*
				Animation fadeIn = AnimationUtils.loadAnimation(map,
						R.anim.fadein);
				*/
				bubble.setVisibility(View.VISIBLE);
				//bubble.startAnimation(fadeIn);
			}
		};

		// This projection and offset finds us a new GeoPoint slightly below the
		// actual OverlayItem,
		// which means the bubble will end up being centered nicely when we tap
		// on an Item.
		Projection projection = map.getMap().getProjection();
		Point p = new Point();

		projection.toPixels(item.getPoint(), p);
		p.offset(0, -(bubble.getMeasuredHeight() / 2));
		GeoPoint target = projection.fromPixels(p.x, p.y);

		// Move the MapView to our point, and then call the Runnable that fades
		// in the bubble.
		map.getMap().getController().animateTo(target, r);
	}

	@Override
	protected boolean onTap(int index) {
		displayBubble(getItem(index));
		return super.onTap(index);
	}
}

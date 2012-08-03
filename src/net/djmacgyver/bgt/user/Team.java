package net.djmacgyver.bgt.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.map.HSVManipulationMatrix;
import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

public class Team {
	private static Team[] teams = new Team[7];
	private static Team anonymousTeam;
	private static float[][] teamColors = {
		{-30,  .8f},
		{0,    1},
		{95,   1},
		{165,  1},
		{-106, 1},
		{22,   1},
		{-171, .8f}
	};
	
	private String name;
	private Drawable pin;
	
	protected Team(String name, Drawable d) {
		this.name = name;
		this.pin = d;
	}
	
	public static Team getTeam(String name, Context context) {
		Pattern p = Pattern.compile("([0-9]+)");
		Matcher m = p.matcher(name);
		if (!m.find()) return getAnonymousTeam(context);
		int teamId = Integer.valueOf(m.group(0));
		if (teamId > teams.length) return getAnonymousTeam(context);
		if (teams[teamId -1] != null) return teams[teamId];
		
		Drawable d = context.getResources().getDrawable(R.drawable.pin_common).mutate();
		float[] colors = teamColors[teamId - 1];
    	d.setColorFilter(new ColorMatrixColorFilter(new HSVManipulationMatrix(colors[0], colors[1])));
    	d.setBounds(d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
    	
		Team team = new Team(name, d);
		teams[teamId - 1] = team;
		return team;
	}
	
	public static Team getAnonymousTeam(Context context)
	{
		if (anonymousTeam == null) {
			Drawable d = context.getResources().getDrawable(R.drawable.pin).mutate();
	    	d.setBounds(d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / -2, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
	    	d.setAlpha(127);
			anonymousTeam = new Team("anonymous", d);
		}
		return anonymousTeam;
	}

	public String getName() {
		return name;
	}

	public Drawable getPin() {
		return pin;
	}
}

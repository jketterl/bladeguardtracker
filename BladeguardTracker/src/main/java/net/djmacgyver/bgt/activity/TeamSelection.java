package net.djmacgyver.bgt.activity;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.team.TeamList;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class TeamSelection extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.fullscreenlist);

        setListAdapter(new TeamList(this, true));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent result = new Intent();
		result.putExtra("teamId", id);
		setResult(Activity.RESULT_OK, result);
		finish();
	}
}

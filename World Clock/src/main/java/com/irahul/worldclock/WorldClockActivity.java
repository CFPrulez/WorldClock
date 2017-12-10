/*
 * Copyright (C) 2012 Rahul Agarwal
 *
 * This file is part of the World Clock
 * World Clock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * World Clock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with World Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.irahul.worldclock;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Main world clock activity
 * 
 * @author rahul
 * 
 */
public class WorldClockActivity extends AppCompatActivity {
	private static final String TAG = WorldClockActivity.class.getName();	
	//
	//Intent extras map keys
	//IN - sent to edit dialog
	//OUT - received in onActivityResult
	//
	public static final String INTENT_TZ_DISPLAYNAME_IN = "INTENT_TZ_DISPLAYNAME_IN";
	public static final String INTENT_TZ_ID_IN = "INTENT_TZ_ID_IN";
	public static final String INTENT_TZ_DISPLAYNAME_OUT = "INTENT_TZ_DISPLAYNAME_OUT";
	public static final String INTENT_TZ_ID_OUT = "INTENT_TZ_ID_OUT";
	
	//Request codes for intent 
	private static final int REQ_CODE_ADD_ZONE = 0;
	private static final int REQ_CODE_EDIT_ZONE = 1;
		
	private WorldClockData data;
	private TimeZoneListAdapter adapter;
	private Toolbar toolbar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.worldclock_main);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.app_name);

		setSupportActionBar(toolbar);

		//init data
		data = new WorldClockData(getApplicationContext());						
		int listSize = refreshListView();
		
		// register to get context event to edit/delete
		ListView mainListView = (ListView)findViewById(R.id.main_list_view);
		registerForContextMenu(mainListView);
		
		FloatingActionButton mainAddButton = findViewById(R.id.main_button_add);
		mainAddButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				invokeAddZoneActivity();
			}			
		});
		
		//if no data exists then prompt to add
		if(listSize==0){
			invokeAddZoneActivity();
		}
	}

	/**
	 * Context menu to allow edit/delete of timezone
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_timezone_edit, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		
		ListView mainListView = (ListView)findViewById(R.id.main_list_view);		
		WorldClockTimeZone selectedTimeZone = (WorldClockTimeZone) mainListView.getAdapter().getItem(info.position);

		switch (item.getItemId()) {
			case R.id.menu_up:
				changePositionUp(selectedTimeZone);
				refreshListView();
				data.updateFile();
				return true;
			case R.id.menu_down:
				changePositionDown(selectedTimeZone);
				refreshListView();
				data.updateFile();
				return true;
			case R.id.menu_edit:
				// edit
				Intent editIntent = new Intent(Intent.ACTION_EDIT);
				editIntent.putExtra(INTENT_TZ_ID_IN, selectedTimeZone.getId());
				editIntent.putExtra(INTENT_TZ_DISPLAYNAME_IN, selectedTimeZone.getDisplayName());
				editIntent.setComponent(new ComponentName(this, TimeZoneEdit.class));
				startActivityForResult(editIntent, REQ_CODE_EDIT_ZONE);
				return true;
			case R.id.menu_delete:
				// delete
				int position = selectedTimeZone.getPosition();
				data.deleteZone(selectedTimeZone);
				changePositionsAfterWorldClockTimeZoneRemoval(position);
				refreshListView();
				data.updateFile();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intentReceived) {
		Log.d(TAG, "activity requestcode="+requestCode+" resultCode="+resultCode+ "data="+intentReceived);
		//process response from add/edit activity
		if(resultCode==RESULT_OK){
			switch(requestCode){
			case REQ_CODE_ADD_ZONE:
				Log.d(TAG, "Add zone id"+intentReceived.getStringExtra(INTENT_TZ_ID_OUT)+ " name="+intentReceived.getStringExtra(INTENT_TZ_DISPLAYNAME_OUT));
				data.addZone(new WorldClockTimeZone(TimeZone.getTimeZone(intentReceived.getStringExtra(INTENT_TZ_ID_OUT)),intentReceived.getStringExtra(INTENT_TZ_DISPLAYNAME_OUT)));
				break;
			case REQ_CODE_EDIT_ZONE:
				Log.d(TAG, "EDIT - Remove zone id"+intentReceived.getStringExtra(INTENT_TZ_ID_IN)+ " name="+intentReceived.getStringExtra(INTENT_TZ_DISPLAYNAME_IN));
				Log.d(TAG, "EDIT - Add zone id"+intentReceived.getStringExtra(INTENT_TZ_ID_OUT)+ " name="+intentReceived.getStringExtra(INTENT_TZ_DISPLAYNAME_OUT));
				data.deleteZone(new WorldClockTimeZone(TimeZone.getTimeZone(intentReceived.getStringExtra(INTENT_TZ_ID_IN))));
				data.addZone(new WorldClockTimeZone(TimeZone.getTimeZone(intentReceived.getStringExtra(INTENT_TZ_ID_OUT)),intentReceived.getStringExtra(INTENT_TZ_DISPLAYNAME_OUT)));				
				break;
			default:
				throw new WorldClockException("Unsupported request code!");
			}
		}
		
		//refresh data 
		refreshListView();
		data.updateFile();
		super.onActivityResult(requestCode, resultCode, intentReceived);
	}

	/**
	 * Present options menu to add timezones
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	/**
	 * Handle menu item selects
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			// add a new zone
			invokeAddZoneActivity();
			return true;
		case R.id.menu_about:
			Intent i = new Intent(WorldClockActivity.this, AboutActivity.class);
			startActivity(i);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void invokeAddZoneActivity() {
		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setComponent(new ComponentName(this, TimeZoneEdit.class));
		startActivityForResult(intent, REQ_CODE_ADD_ZONE);
	}

	private void changePositionUp(WorldClockTimeZone selectedTimeZone) {
		int position = selectedTimeZone.getPosition();
		if(position == 0)
			return;

		for(WorldClockTimeZone w : data.getSavedTimeZones()) {
			if(w.getPosition() == position - 1) {
				w.setPosition(position);
				break;
			}
		}

		selectedTimeZone.setPosition(position - 1);
	}

	private void changePositionDown(WorldClockTimeZone selectedTimeZone) {
		int position = selectedTimeZone.getPosition();

		Set<WorldClockTimeZone> worldClockTimeZoneSet = data.getSavedTimeZones();
		if(position == worldClockTimeZoneSet.size() - 1)
			return;

		for(WorldClockTimeZone w : worldClockTimeZoneSet) {
			if(w.getPosition() == position + 1) {
				w.setPosition(position);
				break;
			}
		}

		selectedTimeZone.setPosition(position + 1);
	}

	private void changePositionsAfterWorldClockTimeZoneRemoval(int deletedPosition) {
		for(WorldClockTimeZone w : data.getSavedTimeZones()) {
			int position = w.getPosition();
			if(position > deletedPosition)
				w.setPosition(position - 1);
		}
	}

	private WorldClockTimeZone[] getSortedWorldClockTimeZoneArray(WorldClockTimeZone[] values) {
		WorldClockTimeZone[] sorted = new WorldClockTimeZone[values.length];
		HashSet<WorldClockTimeZone> unclear_id = new HashSet<>();

		// get sorted array, if already sorted in O(n)
		for(WorldClockTimeZone w : values) {
			int position = w.getPosition();
			if(position != -1 && position < values.length && sorted[position] == null) {
					sorted[position] = w;
					continue;
				}
			unclear_id.add(w);
		}

		// insert WCTZs without id
		int i = -1;
		for(WorldClockTimeZone w : unclear_id) {
			while (i < values.length) {
				i++;
				if (sorted[i] == null) {
					sorted[i] = w;
					w.setPosition(i);
					break;
				}
			}
		}

		return sorted;
	}

	/**
	 * Refresh list of timezones
	 * @return number of items in list after refresh
	 */
	private int refreshListView() {
		WorldClockTimeZone[] values = data.getSavedTimeZones().toArray(
				new WorldClockTimeZone[] {});

		values = getSortedWorldClockTimeZoneArray(values);

		Log.d(TAG, "Loaded data size for refresh:" + values.length);

		adapter = new TimeZoneListAdapter(this, values);
		
		ListView mainListView = (ListView)findViewById(R.id.main_list_view);
		mainListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();		
		return values.length;
	}
}
/*
 * Copyright (C) 2011 Google Inc.
 * Copyright (C) 2013 ENTERTAILION LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.entertailion.android.slideshow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.entertailion.android.slideshow.images.ImageAdapter;
import com.entertailion.android.slideshow.images.ImageManager;
import com.entertailion.android.slideshow.utils.Analytics;
import com.entertailion.android.slideshow.utils.Utils;

/**
 * The main Activity which displays the grid of photos fetched from the
 * internet. Launches photos in full-screen using ViewImageActivity.
 * 
 * @author leon_nicholls
 *
 */
public class ImageGridActivity extends Activity {
	private static final String LOG_TAG = "ImageGrid";
	public static final String PREFERENCES_NAME = "preferences";
	public static final String FIRST_INSTALL = "first_install";
	public static final String LAST_SITE_TITLE = "last_site_title";
	public static final String LAST_SITE_URL = "last_site_url";

	// Identifiers for menu items
	protected static final int MENU_SETTINGS = Menu.FIRST + 1;
	protected static final int MENU_ABOUT = MENU_SETTINGS + 1;
	protected static final int MENU_SELECT_SITE = MENU_ABOUT + 1;
	protected static final int MENU_SELECT_BOOKMARK = MENU_SELECT_SITE + 1;

	private final static float COVER_ALPHA_VALUE = 0.8f;

	private ImageManager mImageManager;

	private static Context mContext;

	private ProgressBar progressBar;

	private GridView gridView;

	private ImageAdapter imageAdapter;

	private ImageView menuImageView, coverImageView;

	private TextView titleTextView;

	private Animation fadeOut, moveUpSlow;

	private String nextSite = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		moveUpSlow = AnimationUtils.loadAnimation(this, R.anim.move_up_slow);

		initGridView();

		mImageManager = ImageManager.getInstance(mContext);

		SharedPreferences settings = getSharedPreferences(ImageGridActivity.PREFERENCES_NAME, Activity.MODE_PRIVATE);
		String lastSiteTitle = settings.getString(LAST_SITE_TITLE, getString(R.string.default_site_title));
		String lastSiteUrl = settings.getString(LAST_SITE_URL, getString(R.string.default_site_url));
		// load previous site; start downloading the image data
		try {
			mImageManager.load(lastSiteUrl);
			titleTextView.setText(Html.fromHtml(lastSiteTitle));
		} catch (Exception e) {
			Log.e(LOG_TAG, "onCreate", e);
		}

		// Set the context for Google Analytics
		Analytics.createAnalytics(this);
		Utils.logDeviceInfo(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Start Google Analytics for this activity
		Analytics.startAnalytics(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Stop Google Analytics for this activity
		Analytics.stopAnalytics(this);
	}

	@Override
	protected void onDestroy() {
		super.onStop();

		// clean the file cache when root activity exit
		// the resulting total cache size will be less than 3M
		if (isTaskRoot()) {
			AQUtility.cleanCacheAsync(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		showCover(false);
		imageAdapter.notifyDataSetChanged();

		// For first time install show the introduction dialog with some user
		// instructions
		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
		boolean firstInstall = settings.getBoolean(FIRST_INSTALL, true);
		if (firstInstall) {
			try {
				Dialogs.displayIntroduction(this);

				// persist not to show introduction again
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(FIRST_INSTALL, false);
				editor.commit();
			} catch (Exception e) {
				Log.d(LOG_TAG, "first install", e);
			}
		} else {
			Dialogs.displayRating(this);
		}

		// display menu hint
		fadeOut.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				menuImageView.setVisibility(View.GONE);
				showCover(false);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		});
		menuImageView.startAnimation(fadeOut);
		// display site title
		moveUpSlow.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				titleTextView.setVisibility(View.GONE);
				showCover(false);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		});
		titleTextView.startAnimation(moveUpSlow);

		Analytics.logEvent(Analytics.IMAGE_GRID);
	}

	@Override
	protected void onPause() {
		super.onPause();
		showCover(true);
	}

	private void initGridView() {
		setContentView(R.layout.image_grid);
		gridView = (GridView) findViewById(R.id.gridview);

		menuImageView = (ImageView) findViewById(R.id.menu);
		coverImageView = (ImageView) findViewById(R.id.cover);
		titleTextView = (TextView) findViewById(R.id.title);
		Typeface typeface = ((SlideshowApplication) getApplicationContext()).getAlternateTypeface(this);
		titleTextView.setTypeface(typeface);

		imageAdapter = new ImageAdapter(mContext);
		gridView.setAdapter(imageAdapter);
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		progressBar.setVisibility(View.VISIBLE);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				BitmapAjaxCallback.cancel();
				AjaxCallback.cancel();
				// load the full-screen photo activity
				final Intent i = new Intent(ImageGridActivity.this, ViewImageActivity.class);
				i.putExtra(ImageManager.SLIDESHOW_ITEM_POSITION, position);
				startActivity(i);
			}
		});
		gridView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			public void onChildViewAdded(View parent, View child) {
				progressBar.setVisibility(View.INVISIBLE);
				((ViewGroup) parent).getChildAt(0).setSelected(true);
			}

			public void onChildViewRemoved(View parent, View child) {
				progressBar.setVisibility(View.INVISIBLE);
			}
		});
		imageAdapter.registerDataSetObserver(new DataSetObserver() {
			public void onChanged() {
			}

			public void onInvalidated() {
				// check if something went wrong; no photos
				if (mImageManager.isError()) {
					progressBar.setVisibility(View.INVISIBLE);
					Toast statusToast = Toast.makeText(ImageGridActivity.this, getString(R.string.no_photos), Toast.LENGTH_SHORT);
					statusToast.show();
				}
			}
		});
		gridView.requestFocus();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		titleTextView.setVisibility(View.GONE);
		switch (e.getKeyCode()) {
		case KeyEvent.KEYCODE_MEDIA_PLAY: {
			BitmapAjaxCallback.cancel();
			AjaxCallback.cancel();
			final Intent i = new Intent(ImageGridActivity.this, ViewImageActivity.class);
			i.putExtra(ImageManager.SLIDESHOW_ITEM_POSITION, gridView.getSelectedItemPosition());
			i.putExtra(ImageManager.SLIDESHOW_STATE, "play");
			startActivity(i);
			return true;
		}
		case KeyEvent.KEYCODE_MEDIA_NEXT: {
			// Go to the last photo
			int count = gridView.getAdapter().getCount();
			gridView.setSelection(count - 1);
			return true;
		}
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
			// Go to the first photo
			gridView.setSelection(0);
			return true;
		}
		}
		return super.dispatchKeyEvent(e);
	};

	/**
	 * Display the cover layer to darken the screen for dialogs.
	 * 
	 * @param isVisible
	 */
	public void showCover(boolean isVisible) {
		if (isVisible) {
			coverImageView.setAlpha(COVER_ALPHA_VALUE);
			coverImageView.setVisibility(View.VISIBLE);
		} else {
			coverImageView.setVisibility(View.GONE);
		}
	}

	/**
	 * Create the menu. Invoked by the user by pressing the menu key.
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SELECT_BOOKMARK, 0, R.string.menu_select_bookmark).setIcon(R.drawable.ic_menu_bookmark).setAlphabeticShortcut('B');
		menu.add(0, MENU_SELECT_SITE, 0, R.string.menu_select_site).setIcon(R.drawable.ic_menu_bookmark).setAlphabeticShortcut('W');
		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setIcon(R.drawable.ic_menu_settings).setAlphabeticShortcut('S');
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_clipboard).setAlphabeticShortcut('A');

		return true;
	}

	/**
	 * Handle the menu selections.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		BitmapAjaxCallback.cancel();
		AjaxCallback.cancel();
		if (handleMenuOption(item.getItemId())) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public boolean handleMenuOption(int id) {
		showCover(true);
		switch (id) {
		case MENU_ABOUT:
			Dialogs.displayAbout(this);
			return true;
		case MENU_SELECT_BOOKMARK:
			Dialogs.displayBookmarks(this);
			return true;
		case MENU_SELECT_SITE:
			Dialogs.displaySites(this);
			return true;
		case MENU_SETTINGS:
			// launch the preferences activity
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			Analytics.logEvent(Analytics.SETTINGS);
			return true;
		}
		showCover(false);
		return false;
	}

	public void setSite(String title, String url) {
		Log.d(LOG_TAG, "setSite: title=" + title + ", url=" + url);
		nextSite = url;

		// persist site selection
		try {
			SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(LAST_SITE_TITLE, title);
			editor.putString(LAST_SITE_URL, url);
			editor.commit();
		} catch (Exception e) {
			Log.d(LOG_TAG, "setSite", e);
		}

		initGridView();

		Log.d(LOG_TAG, "setSite=" + nextSite);
		try {
			mImageManager.load(nextSite);
			titleTextView.setText(Html.fromHtml(title));
		} catch (Exception e) {
			Log.e(LOG_TAG, "setSite", e);
		}
	}

	public static ImageGridActivity getInstance() {
		return (ImageGridActivity) mContext;
	}
}

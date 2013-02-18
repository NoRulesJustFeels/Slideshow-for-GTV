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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.entertailion.android.slideshow.images.GalleryImageAdapter;
import com.entertailion.android.slideshow.images.ImageItem;
import com.entertailion.android.slideshow.images.ImageManager;
import com.entertailion.android.slideshow.utils.Analytics;
import com.entertailion.android.slideshow.utils.Utils;
import com.entertailion.android.slideshow.widget.EcoGallery;

/**
 * Activity which displays a full-screen photo and a carousel to browse through
 * all photos. It also enables the user to view a slideshow of all photos.
 * 
 * @author leon_nicholls
 * 
 */
public class ViewImageActivity extends Activity implements Animation.AnimationListener {
	private static final String LOG_TAG = "ViewImage";

	public static final String FIRST_DOWN = "first_down";
	public static final String FIRST_SLIDESHOW = "first_slideshow";
	public static final String FIRST_SITE = "first_site";

	public static final int ANIMATION_DELAY = 2000;
	public static final String IMAGE_DELAY = "5";

	private static final float GALLERY_UNSELECTECTED_ALPHA = 0.9f;
	private static final int GALLERY_ANIMATION_DURATION = 200;

	/**
	 * A toast that shows status messages. We have to keep it in order to be
	 * able to substitute existing messages instead of queuing them.
	 */
	private Toast statusToast;

	private ImageView image1;

	private ImageView image2;

	private ImageView foregroundImageView;

	private ImageView selectorImageView;

	private boolean playing = false;

	private int visibleItemIndex;

	private Context context;

	private ImageManager imageManager;

	private TextView titleTextView;

	private TextView footerTextView;

	private EcoGallery gallery;

	private GalleryImageAdapter adapter;

	private String title;

	private String footer;

	private ProgressBar progressBar;

	private AlphaAnimation animation1;

	private AlphaAnimation animation2;

	private Animation galleryUpAnimation, galleryDownAnimation;

	private Animation selectorUpAnimation, selectorDownAnimation;

	private AQuery aq;

	private boolean finished = false;

	private String state;

	private int fullSize = 0;

	private boolean galleryVisible = false;

	private int delay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		super.onCreate(savedInstanceState);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String delayPreference = preferences.getString(PreferencesActivity.GENERAL_PHOTO_DELAY, IMAGE_DELAY);
		delay = Integer.parseInt(delayPreference) * 1000; // milliseconds

		final Resources resources = context.getResources();
		fullSize = (int) resources.getDimensionPixelSize(R.dimen.full_size);

		aq = new AQuery(context);

		setContentView(R.layout.view_image);
		progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
		final Intent i = getIntent();
		visibleItemIndex = i.getIntExtra(ImageManager.SLIDESHOW_ITEM_POSITION, 0);
		state = i.getStringExtra(ImageManager.SLIDESHOW_STATE);

		image1 = foregroundImageView = (ImageView) findViewById(R.id.image_1);
		image2 = (ImageView) findViewById(R.id.image_2);
		selectorImageView = (ImageView) findViewById(R.id.selector);

		titleTextView = (TextView) findViewById(R.id.title);
		Typeface typeface = ((SlideshowApplication) getApplicationContext()).getAlternateTypeface(this);
		titleTextView.setTypeface(typeface);
		footerTextView = (TextView) findViewById(R.id.footer);
		imageManager = ImageManager.getInstance(ViewImageActivity.this);

		statusToast = Toast.makeText(ViewImageActivity.this, "", Toast.LENGTH_LONG);

		if (imageManager.size() > 0) {
			final ImageItem item = imageManager.get(visibleItemIndex);
			titleTextView.setText(Html.fromHtml(item.getTitle()));
			footerTextView.setText(Html.fromHtml(getString(R.string.author) + " " + item.getOwner()));
			setImage(image1, item);
			image1.setVisibility(View.VISIBLE);
			foregroundImageView = image1;
			if (imageManager.size() > 1) {
				final ImageItem item2 = imageManager.get(visibleItemIndex + 1);
				if (item2 != null) {
					title = item2.getTitle();
					footer = getString(R.string.author) + " " + item2.getOwner();
					setImage(image2, item2);
					image2.setVisibility(View.INVISIBLE);
				}
			}
		}

		galleryUpAnimation = AnimationUtils.loadAnimation(this, R.anim.gallery_up);
		galleryUpAnimation.setFillAfter(true);
		galleryDownAnimation = AnimationUtils.loadAnimation(this, R.anim.gallery_down);
		galleryDownAnimation.setFillAfter(true);
		galleryDownAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				galleryVisible = false;
				// toggle between images and gallery to get key events
				image1.setFocusable(true);
				image2.setFocusable(true);
				gallery.setFocusable(false);
				foregroundImageView.requestFocus();
				footerTextView.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		});
		galleryUpAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				galleryVisible = true;
				// toggle between images and gallery to get key events
				image1.setFocusable(false);
				image2.setFocusable(false);
				gallery.setFocusable(true);
				gallery.requestFocus();
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		});

		// slideshow animations
		animation1 = new AlphaAnimation(1.0f, 0.0f);
		animation1.setStartOffset(delay);
		animation1.setDuration(ANIMATION_DELAY);
		animation1.setFillAfter(true);
		animation1.setInterpolator(new LinearInterpolator());
		animation2 = new AlphaAnimation(0.0f, 1.0f);
		animation2.setStartOffset(delay);
		animation2.setDuration(ANIMATION_DELAY);
		animation2.setFillAfter(true);
		animation2.setAnimationListener(this);
		animation2.setInterpolator(new LinearInterpolator());

		selectorUpAnimation = AnimationUtils.loadAnimation(this, R.anim.gallery_up);
		selectorUpAnimation.setFillAfter(true);
		selectorDownAnimation = AnimationUtils.loadAnimation(this, R.anim.gallery_down);
		selectorDownAnimation.setFillAfter(true);

		adapter = new GalleryImageAdapter(this);
		gallery = (EcoGallery) findViewById(R.id.gallery);
		gallery.setUnselectedAlpha(GALLERY_UNSELECTECTED_ALPHA);
		gallery.setGravity(Gravity.CENTER_HORIZONTAL);
		gallery.setAnimationDuration(GALLERY_ANIMATION_DURATION);
		gallery.setHorizontalFadingEdgeEnabled(true);
		gallery.setEmptyView(createEmptyView());
		gallery.setDrawingCacheEnabled(true);
		gallery.setFocusableInTouchMode(false);
		gallery.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
						// Go to the last item
						int count = gallery.getAdapter().getCount();
						gallery.setSelection(count - 1);
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
						// Go to the first item
						gallery.setSelection(0);
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						// select current position
						visibleItemIndex = gallery.getSelectedItemPosition();
						final ImageItem imageItem = imageManager.get(visibleItemIndex);
						setCurrentImageItem(imageItem);
						return true;
					}
				}
				return false;
			}

		});
		gallery.setVisibility(View.INVISIBLE);

		// toggle between images and gallery to get key events
		image1.setFocusable(true);
		image2.setFocusable(true);
		gallery.setFocusable(false);
		foregroundImageView.requestFocus();

		// Set the context for Google Analytics
		Analytics.createAnalytics(this);
	}

	/**
	 * Create a view for an empty gallery
	 * 
	 * @return default empty view
	 */
	private View createEmptyView() {
		ImageView iconView = new ImageView(this);
		iconView.setScaleType(ScaleType.FIT_XY);
		iconView.setBackgroundColor(getResources().getColor(R.color.transparent));
		iconView.setImageResource(R.drawable.gallery_item_over);
		return iconView;
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

	protected void onDestroy() {
		super.onDestroy();

		BitmapAjaxCallback.cancel();
		AjaxCallback.cancel();
	}

	@Override
	protected void onPause() {
		Log.d(LOG_TAG, "onPause: " + state);
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(LOG_TAG, "onResume: " + state);
		super.onResume();

		if (imageManager.size() == 0) {
			finish();
		} else {
			if (state == null) {
				// show instructions for first slideshow
				SharedPreferences settings = getSharedPreferences(ImageGridActivity.PREFERENCES_NAME, Activity.MODE_PRIVATE);
				boolean firstSlideshow = settings.getBoolean(FIRST_SLIDESHOW, true);
				boolean firstSite = settings.getBoolean(FIRST_SITE, true);
				if (firstSlideshow) {
					try {
						showStatusToast(R.string.slideshow_play_hint);

						// persist not to show slideshow instructions again
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(FIRST_SLIDESHOW, false);
						editor.commit();
					} catch (Exception e) {
						Log.e(LOG_TAG, "first slideshow", e);
					}
				} else if (firstSite) {
					try {
						showStatusToast(R.string.slideshow_site_hint);

						// persist not to show site instructions again
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(FIRST_SITE, false);
						editor.commit();
					} catch (Exception e) {
						Log.e(LOG_TAG, "first site", e);
					}
				}
			} else {
				// don't show titles with slideshow
				titleTextView.setVisibility(View.INVISIBLE);
				footerTextView.setVisibility(View.INVISIBLE);
			}

			Analytics.logEvent(Analytics.VIEW_IMAGE);
		}
	}

	/**
	 * Display new photo in current image.
	 * 
	 * @param imageItem
	 */
	private void setCurrentImageItem(ImageItem imageItem) {
		if (imageItem != null) {
			progressBar.setVisibility(View.VISIBLE);
			if (foregroundImageView == image1) {
				setImage(image1, imageItem);
			} else {
				setImage(image2, imageItem);
			}
			titleTextView.setText(Html.fromHtml(imageItem.getTitle()));
			footerTextView.setText(Html.fromHtml(getString(R.string.author) + " " + imageItem.getOwner()));
		}
	}

	private void setImage(ImageView view, final ImageItem item) {
		view.setContentDescription(item.getTitle());
		aq.id(view).image(item.getImageUrl(), true, true, fullSize, 0, new BitmapAjaxCallback() {

			@Override
			public void callback(String url, ImageView view, Bitmap bitmap, AjaxStatus status) {
				view.setTag(Boolean.TRUE);
				view.setImageBitmap(bitmap);
				progressBar.setVisibility(View.INVISIBLE);

				if (playing) {
					// wait for photo download to complete before next
					// animation
					doAnimation();
				} else if (state != null) {
					// handle the start of a slideshow from the grid activity
					// check if both images loaded with photos
					if (image1.getTag() != null && image2.getTag() != null) {
						startSlideShow();
						state = null;
					}
				}

			}

		});
	}

	/**
	 * A helper method that shows a quick toast with a status message,
	 * overwriting the previous message if it's not yet hidden.
	 * 
	 * @param messageId
	 *            ID of the message to show.
	 */
	private void showStatusToast(int messageId) {
		statusToast.setText(messageId);
		statusToast.show();
	}

	/**
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER: {
			if (!galleryVisible) {
				stopSlideShow(false);
				showStatusToast(R.string.loading_photo_site);

				// load photo web page in browser
				final ImageItem item = imageManager.get(visibleItemIndex);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(item.getPageUrl()));
				startActivity(intent);
				Analytics.logEvent(Analytics.PHOTO_WEB_SITE);
				return true;
			}
		}
		}
		return super.dispatchKeyEvent(e);
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN: {
			if (!galleryVisible) {
				stopSlideShow(true);
				footerTextView.setVisibility(View.INVISIBLE);
				// check if manager is busy; affects gallery animation
				// performance
				if (imageManager.isRunning()) {
					gallery.setAnimationDuration(0);
				} else {
					gallery.setAnimationDuration(GALLERY_ANIMATION_DURATION);
				}
				gallery.setAdapter(adapter);
				gallery.setVisibility(View.VISIBLE);
				gallery.setSelection(visibleItemIndex);
				gallery.startAnimation(galleryUpAnimation);
				selectorImageView.startAnimation(selectorUpAnimation);

				// show instructions for first down
				SharedPreferences settings = getSharedPreferences(ImageGridActivity.PREFERENCES_NAME, Activity.MODE_PRIVATE);
				boolean firstDown = settings.getBoolean(FIRST_DOWN, true);
				if (firstDown) {
					try {
						showStatusToast(R.string.hide_thumbnails_hint);

						// persist not to show down instructions again
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(FIRST_DOWN, false);
						editor.commit();
					} catch (Exception e) {
						Log.e(LOG_TAG, "first down", e);
					}
				}
				return true;
			}
			break;
		}
		case KeyEvent.KEYCODE_DPAD_UP: {
			if (galleryVisible) {
				gallery.startAnimation(galleryDownAnimation);
				selectorImageView.startAnimation(selectorDownAnimation);
				return true;
			}
			break;
		}
		case KeyEvent.KEYCODE_MEDIA_PLAY: {
			startSlideShow();
			return true;
		}
		case KeyEvent.KEYCODE_MEDIA_STOP:
		case KeyEvent.KEYCODE_MEDIA_PAUSE: {
			stopSlideShow(true);
			return true;
		}
		case KeyEvent.KEYCODE_MEDIA_NEXT: {
			stopSlideShow(true);
			// go to last photo
			int previousIndex = visibleItemIndex;
			visibleItemIndex = imageManager.size() - 1;
			if (previousIndex != visibleItemIndex) {
				final ImageItem imageItem = imageManager.get(visibleItemIndex);
				setCurrentImageItem(imageItem);
			}
			return true;
		}
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
			stopSlideShow(true);
			// got to first photo
			int previousIndex = visibleItemIndex;
			visibleItemIndex = 0;
			if (previousIndex != visibleItemIndex) {
				final ImageItem imageItem = imageManager.get(visibleItemIndex);
				setCurrentImageItem(imageItem);
			}
			return true;
		}
		case KeyEvent.KEYCODE_MEDIA_REWIND:
		case KeyEvent.KEYCODE_DPAD_LEFT: {
			if (!galleryVisible) {
				stopSlideShow(true);
				// go to previous photo
				int previousIndex = visibleItemIndex;
				visibleItemIndex--;
				if (visibleItemIndex < 0) {
					visibleItemIndex = 0;
				}
				if (previousIndex != visibleItemIndex) {
					final ImageItem imageItem = imageManager.get(visibleItemIndex);
					setCurrentImageItem(imageItem);
				}
				return true;
			}
			break;
		}
		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
		case KeyEvent.KEYCODE_DPAD_RIGHT: {
			if (!galleryVisible) {
				stopSlideShow(true);
				// go to next photo
				int previousIndex = visibleItemIndex;
				visibleItemIndex++;
				if (visibleItemIndex > imageManager.size() - 1) {
					visibleItemIndex = imageManager.size() - 1;
				}
				if (previousIndex != visibleItemIndex) {
					final ImageItem imageItem = imageManager.get(visibleItemIndex);
					setCurrentImageItem(imageItem);
				}
				return true;
			}
			break;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void stopSlideShow(boolean showFeedback) {
		if (playing) {
			playing = false;
			titleTextView.setVisibility(View.VISIBLE);
			footerTextView.setVisibility(View.VISIBLE);
			if (showFeedback) {
				showStatusToast(R.string.slideshow_paused);
			}
		}
	}

	private void startSlideShow() {
		if (imageManager.size() > 1) {
			if (!playing) {
				playing = true;
				if (galleryVisible) {
					gallery.startAnimation(galleryDownAnimation);
					selectorImageView.startAnimation(selectorDownAnimation);
				}
				titleTextView.setVisibility(View.INVISIBLE);
				footerTextView.setVisibility(View.INVISIBLE);
				showStatusToast(R.string.slideshow_started);
				// image1 has first photo, image2 has second photo already
				visibleItemIndex++;
				doAnimation();
				Analytics.logEvent(Analytics.SLIDESHOW);
			}
		}
	}

	private void doAnimation() {
		if (foregroundImageView == image1) {
			if (playing) {
				image1.startAnimation(animation1);
				image2.startAnimation(animation2);
			}
			foregroundImageView = image2;
		} else {
			if (playing) {
				image2.startAnimation(animation1);
				image1.startAnimation(animation2);
			}
			foregroundImageView = image1;
		}

		// disable screensaver
		Utils.wakeUp(this);
	}

	public void onAnimationEnd(Animation animation) {
		visibleItemIndex++;
		// loop the index
		if (visibleItemIndex > imageManager.size() - 1) {
			visibleItemIndex = 0;
		}
		final ImageItem item = imageManager.get(visibleItemIndex);
		title = item.getTitle();
		footer = getString(R.string.author) + " " + item.getOwner();
		if (foregroundImageView == image1) {
			setImage(image2, item);
		} else {
			setImage(image1, item);
		}
	}

	public void onAnimationRepeat(Animation animation) {
	}

	public void onAnimationStart(Animation animation) {
		// keep the title in sync with photo
		titleTextView.setText(Html.fromHtml(title));
		footerTextView.setText(Html.fromHtml(footer));
	}

	/**
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus && !finished) {
			stopSlideShow(false);
		}
	}

	private void doFinish() {
		synchronized (this) {
			if (!finished) {
				finished = true;
				new Thread(new Runnable() {

					public void run() {
						ViewImageActivity.this.finish();
					}

				}).start();
			}
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

		menu.add(0, ImageGridActivity.MENU_SELECT_BOOKMARK, 0, R.string.menu_select_bookmark).setIcon(R.drawable.ic_menu_bookmark).setAlphabeticShortcut('B');
		menu.add(0, ImageGridActivity.MENU_SELECT_SITE, 0, R.string.menu_select_site).setIcon(R.drawable.ic_menu_bookmark).setAlphabeticShortcut('W');
		menu.add(0, ImageGridActivity.MENU_SETTINGS, 0, R.string.menu_settings).setIcon(R.drawable.ic_menu_settings).setAlphabeticShortcut('S');
		menu.add(0, ImageGridActivity.MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_clipboard).setAlphabeticShortcut('A');

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		BitmapAjaxCallback.cancel();
		AjaxCallback.cancel();
		// reuse the menu logic from ImageGrid activity
		if (ImageGridActivity.getInstance().handleMenuOption(item.getItemId())) {
			setVisible(false);
			doFinish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	

	/**
	 * Set the menu item states just before it is shown to the user.
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		stopSlideShow(false);
		return true;
	}
}

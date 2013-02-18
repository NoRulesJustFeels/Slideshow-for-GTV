/*
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Browser;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.entertailion.android.slideshow.bookmark.BookmarkAdapter;
import com.entertailion.android.slideshow.bookmark.BookmarkInfo;
import com.entertailion.android.slideshow.sites.SiteAdapter;
import com.entertailion.android.slideshow.sites.SiteInfo;
import com.entertailion.android.slideshow.utils.Analytics;
import com.entertailion.android.slideshow.utils.Utils;

/**
 * Utility class to display various dialogs for the main activity
 * 
 * @author leon_nicholls
 * 
 */
public class Dialogs {

	private static final String LOG_TAG = "Dialogs";

	// Ratings dialog configuration
	public static final String DATE_FIRST_LAUNCHED = "date_first_launched";
	public static final String DONT_SHOW_RATING_AGAIN = "dont_show_rating_again";
	private final static int DAYS_UNTIL_PROMPT = 5;

	/**
	 * Display introduction to the user for first time launch
	 * 
	 * @param context
	 */
	public static void displayIntroduction(final ImageGridActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.introduction);

		Typeface lightTypeface = ((SlideshowApplication) context.getApplicationContext()).getLightTypeface(context);

		TextView titleTextView = (TextView) dialog.findViewById(R.id.intro_title);
		titleTextView.setTypeface(lightTypeface);
		TextView textView1 = (TextView) dialog.findViewById(R.id.intro_text1);
		textView1.setTypeface(lightTypeface);
		TextView textView2 = (TextView) dialog.findViewById(R.id.intro_text2);
		textView2.setTypeface(lightTypeface);

		((Button) dialog.findViewById(R.id.intro_button)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_INTRODUCTION);
	}

	/**
	 * Display about dialog to user when invoked from menu option.
	 * 
	 * @param context
	 */
	public static void displayAbout(final ImageGridActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.about);

		Typeface lightTypeface = ((SlideshowApplication) context.getApplicationContext()).getLightTypeface(context);

		TextView aboutTextView = (TextView) dialog.findViewById(R.id.about_text1);
		aboutTextView.setTypeface(lightTypeface);
		aboutTextView.setText(context.getString(R.string.about_version_title, Utils.getVersion(context)));
		aboutTextView.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				context.showCover(false);
				dialog.dismiss();
				Intent intent = new Intent(context, EasterEggActivity.class);
				context.startActivity(intent);
				Analytics.logEvent(Analytics.EASTER_EGG);
				return true;
			}

		});
		TextView copyrightTextView = (TextView) dialog.findViewById(R.id.copyright_text);
		copyrightTextView.setTypeface(lightTypeface);
		TextView feedbackTextView = (TextView) dialog.findViewById(R.id.feedback_text);
		feedbackTextView.setTypeface(lightTypeface);

		((Button) dialog.findViewById(R.id.button_web)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_web_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_WEB_SITE);
				context.showCover(false);
				dialog.dismiss();
			}

		});

		((Button) dialog.findViewById(R.id.button_privacy_policy)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_privacy_policy_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_PRIVACY_POLICY);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		((Button) dialog.findViewById(R.id.button_more_apps)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(context.getString(R.string.about_button_more_apps_url)));
				context.startActivity(intent);
				Analytics.logEvent(Analytics.ABOUT_MORE_APPS);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_ABOUT);
	}

	/**
	 * Prompt the user to rate the app.
	 * 
	 * @param context
	 */
	public static void displayRating(final ImageGridActivity context) {
		SharedPreferences prefs = context.getSharedPreferences(ImageGridActivity.PREFERENCES_NAME, Activity.MODE_PRIVATE);

		if (prefs.getBoolean(DONT_SHOW_RATING_AGAIN, false)) {
			return;
		}

		final SharedPreferences.Editor editor = prefs.edit();

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong(DATE_FIRST_LAUNCHED, 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong(DATE_FIRST_LAUNCHED, date_firstLaunch);
		}

		// Wait at least n days before opening
		if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
			final Dialog dialog = new Dialog(context);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.confirmation);

			TextView confirmationTextView = (TextView) dialog.findViewById(R.id.confirmationText);
			confirmationTextView.setText(context.getString(R.string.rating_message));
			Button buttonYes = (Button) dialog.findViewById(R.id.button1);
			buttonYes.setText(context.getString(R.string.dialog_yes));
			buttonYes.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.entertailion.android.slideshow"));
					context.startActivity(intent);
					if (editor != null) {
						editor.putBoolean(DONT_SHOW_RATING_AGAIN, true);
						editor.commit();
					}
					Analytics.logEvent(Analytics.RATING_YES);
					context.showCover(false);
					dialog.dismiss();
				}

			});
			Button buttonNo = (Button) dialog.findViewById(R.id.button2);
			buttonNo.setText(context.getString(R.string.dialog_no));
			buttonNo.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (editor != null) {
						editor.putBoolean(DONT_SHOW_RATING_AGAIN, true);
						editor.commit();
					}
					Analytics.logEvent(Analytics.RATING_NO);
					context.showCover(false);
					dialog.dismiss();
				}

			});
			dialog.setOnDismissListener(new OnDismissListener() {

				public void onDismiss(DialogInterface dialog) {
					context.showCover(false);
				}

			});
			context.showCover(true);
			dialog.show();
		}

		editor.commit();
	}

	/**
	 * Display the list of sites.
	 * 
	 * @param context
	 */
	public static void displaySites(final ImageGridActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.sites_list);

		ListView listView = (ListView) dialog.findViewById(R.id.list);
		final ArrayList<SiteInfo> sites = Utils.getSites(context);
		Collections.sort(sites, new Comparator<SiteInfo>() {

			public int compare(SiteInfo lhs, SiteInfo rhs) {
				return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
			}

		});
		listView.setAdapter(new SiteAdapter(context, sites));
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SiteInfo siteInfo = (SiteInfo) parent.getAdapter().getItem(position);
				context.setSite(siteInfo.getTitle(), siteInfo.getUrl());
				Analytics.logEvent(Analytics.SELECT_SITE);
				context.showCover(false);
				dialog.dismiss();
			}

		});
		listView.setDrawingCacheEnabled(true);
		listView.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (v instanceof AbsListView) {
					AbsListView absListView = (AbsListView) v;
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
							// Jump to first item that starts with the typed
							// letter
							char letter = Utils.keyCodeToLetter(keyCode);
							int count = absListView.getAdapter().getCount();
							for (int i = 0; i < count; i++) {
								SiteInfo siteInfo = (SiteInfo) absListView.getAdapter().getItem(i);
								if (siteInfo.getTitle().toUpperCase().charAt(0) == letter) {
									absListView.setSelection(i);
									break;
								}
							}
							return true;
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
							// Go to the last item
							int count = absListView.getAdapter().getCount();
							absListView.setSelection(count - 1);
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
							// Go to the first item
							absListView.setSelection(0);
						}
					}
				}
				return false;
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_SITES);
	}

	/**
	 * Display the list of browser bookmarks. Allow user to load bookmarked web
	 * site.
	 * 
	 * @param context
	 */
	public static void displayBookmarks(final ImageGridActivity context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.bookmarks_list);

		ListView listView = (ListView) dialog.findViewById(R.id.list);
		final ArrayList<BookmarkInfo> bookmarks = loadBookmarks(context);
		Collections.sort(bookmarks, new Comparator<BookmarkInfo>() {

			public int compare(BookmarkInfo lhs, BookmarkInfo rhs) {
				return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
			}

		});
		listView.setAdapter(new BookmarkAdapter(context, bookmarks));
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BookmarkInfo bookmark = (BookmarkInfo) parent.getAdapter().getItem(position);
				if (bookmark.getUrl().toLowerCase().contains("flickr.com") || bookmark.getUrl().toLowerCase().contains("tumblr.com") || bookmark.getUrl().toLowerCase().contains("pinterest.com")) {
					context.setSite(bookmark.getTitle(), bookmark.getUrl());
					context.showCover(false);
					dialog.dismiss();
					Analytics.logEvent(Analytics.INVOKE_BOOKMARK);
				} else {
					displayAlert(context, context.getString(R.string.dialog_sites_supported));
					return;
				}
			}

		});
		listView.setDrawingCacheEnabled(true);
		listView.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (v instanceof AbsListView) {
					AbsListView absListView = (AbsListView) v;
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
							// Jump to first item that starts with the typed
							// letter
							char letter = Utils.keyCodeToLetter(keyCode);
							int count = absListView.getAdapter().getCount();
							for (int i = 0; i < count; i++) {
								BookmarkInfo itemInfo = (BookmarkInfo) absListView.getAdapter().getItem(i);
								if (itemInfo.getTitle().toUpperCase().charAt(0) == letter) {
									absListView.setSelection(i);
									break;
								}
							}
							return true;
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
							// Go to the last item
							int count = absListView.getAdapter().getCount();
							absListView.setSelection(count - 1);
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
							// Go to the first item
							absListView.setSelection(0);
						}
					}
				}
				return false;
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
		Analytics.logEvent(Analytics.DIALOG_BOOKMARKS);
	}

	/**
	 * Utility method to load the list of browser bookmarks.
	 */
	private static ArrayList<BookmarkInfo> loadBookmarks(ImageGridActivity context) {
		ArrayList<BookmarkInfo> bookmarks = new ArrayList<BookmarkInfo>();

		Cursor cursor = context.managedQuery(Browser.BOOKMARKS_URI, Browser.HISTORY_PROJECTION, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.moveToFirst() && cursor.getCount() > 0) {
				while (cursor.isAfterLast() == false) {
					String bookmarkIndex = cursor.getString(Browser.HISTORY_PROJECTION_BOOKMARK_INDEX);
					if (bookmarkIndex != null && bookmarkIndex.equals("1")) {
						BookmarkInfo bookmark = new BookmarkInfo();
						String title = cursor.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX);
						bookmark.setTitle(title);
						String url = cursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
						bookmark.setUrl(url);
						bookmarks.add(bookmark);
					}

					cursor.moveToNext();
				}
			}
		}
		return bookmarks;
	}

	/**
	 * Utility method to display an alert dialog. Use instead of AlertDialog to
	 * get the right styling.
	 * 
	 * @param context
	 * @param message
	 */
	public static void displayAlert(final ImageGridActivity context, String message) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.alert);

		final TextView alertTextView = (TextView) dialog.findViewById(R.id.alertText);
		alertTextView.setText(message);
		Button alertButton = (Button) dialog.findViewById(R.id.alertButton);
		alertButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				context.showCover(false);
				dialog.dismiss();
			}

		});
		dialog.setOnDismissListener(new OnDismissListener() {

			public void onDismiss(DialogInterface dialog) {
				context.showCover(false);
			}

		});
		context.showCover(true);
		dialog.show();
	}

}

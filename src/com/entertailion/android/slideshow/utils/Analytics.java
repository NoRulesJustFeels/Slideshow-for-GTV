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
package com.entertailion.android.slideshow.utils;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.entertailion.android.slideshow.R;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Utility class to manage Google analytics
 * 
 * @see https://developers.google.com/analytics/devguides/collection/android/v2/
 * @author leon_nicholls
 * 
 */
public class Analytics {
	private static final String LOG_TAG = "Analytics";

	public static final String ANALYTICS = "Analytics";
	public static final String IMAGE_GRID = "activity.image_grid";
	public static final String VIEW_IMAGE = "activity.view_image";
	public static final String SLIDESHOW = "slideshow";
	public static final String DIALOG_SITES = "dialog.sites";
	public static final String SELECT_SITE = "sites.select";
	public static final String PHOTO_WEB_SITE = "photo.web_site";
	
	public static final String LOADER_FIVE_HUNDRED_PX = "loader.five_hundred_px";
	public static final String LOADER_FLICKR = "loader.flickr";
	public static final String LOADER_HUBBLE = "loader.hubble";
	public static final String LOADER_PANORAMIO = "loader.panoramio";
	public static final String LOADER_PHOTO_NET = "loader.photo_net";
	public static final String LOADER_PINTEREST = "loader.pinterest";
	public static final String LOADER_TUMBLR = "loader.tumblr";
	public static final String LOADER_WHITE_HOUSE = "loader.white_house";
	public static final String LOADER_WIKIMEDIA = "loader.wikimedia";
	public static final String LOADER_RSS = "loader.rss";
	
	public static final String PHOTO_DELAY_TWO = "photo_delay.2";
	public static final String PHOTO_DELAY_THREE = "photo_delay.3";
	public static final String PHOTO_DELAY_FOUR = "photo_delay.4";
	public static final String PHOTO_DELAY_FIVE = "photo_delay.5";
	public static final String PHOTO_DELAY_SIX = "photo_delay.6";
	public static final String PHOTO_DELAY_SEVEN = "photo_delay.7";
	public static final String PHOTO_DELAY_EIGHT = "photo_delay.8";
	public static final String PHOTO_DELAY_NINE = "photo_delay.9";
	public static final String PHOTO_DELAY_TEN = "photo_delay.10";
	
	public static final String ABOUT_PRIVACY_POLICY = "about.privacy_policy";
	public static final String ABOUT_WEB_SITE = "about.website";
	public static final String ABOUT_MORE_APPS = "about.more_apps";
	public static final String DIALOG_INTRODUCTION = "dialog.introduction";
	public static final String DIALOG_ABOUT = "dialog.about";
	public static final String DIALOG_BOOKMARKS = "dialog.bookmarks";
	public static final String INVOKE_BOOKMARK = "invoke.bookmark";
	public static final String SETTINGS = "settings";
	public static final String RATING_YES = "rating.yes";
	public static final String RATING_NO = "rating.no";
	public static final String RATING_LATER = "rating.later";
	
	public static final String EASTER_EGG = "easter.egg";
	
	private static Context context;

	public static void createAnalytics(Context context) {
		try {
			Analytics.context = context;
			EasyTracker.getInstance().setContext(context);
		} catch (Exception e) {
			Log.e(LOG_TAG, "createAnalytics", e);
		}
	}

	public static void startAnalytics(final Activity activity) {
		try {
			if (activity != null && activity.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getInstance().activityStart(activity);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "startAnalytics", e);
		}
	}

	public static void stopAnalytics(Activity activity) {
		try {
			if (activity != null && activity.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getInstance().activityStop(activity);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "stopAnalytics", e);
		}
	}

	public static void logEvent(String event) {
		try {
			if (context != null && context.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getTracker().trackEvent(ANALYTICS, event, event, 1L);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "logEvent", e);
		}
	}

	public static void logEvent(String event, Map<String, String> parameters) {
		try {
			if (context != null && context.getResources().getInteger(R.integer.development) == 0) {
				EasyTracker.getTracker().trackEvent(ANALYTICS, event, event, 1L);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "logEvent", e);
		}
	}
}

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

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;

public class SlideshowApplication extends Application {

	private static final String LOG_TAG = "SlideshowApplication";

	private Typeface lightTypeface = null;
	private Typeface thinTypeface = null;
	private Typeface mediumTypeface = null;
	private Typeface italicTypeface = null;
	private Typeface alternateTypeface = null;

	@Override
	public void onCreate() {
		// https://github.com/androidquery/androidquery
		if (getResources().getInteger(R.integer.development) == 1) {
			AQUtility.setDebug(true);
		}

		AQUtility.cleanCacheAsync(this, 10000000, 5000000);

		// set the max number of concurrent network connections, default is 4
		AjaxCallback.setNetworkLimit(20);

		// set the max number of icons (image width <= 50) to be cached in
		// memory, default is 20
		BitmapAjaxCallback.setIconCacheLimit(20);

		// set the max number of images (image width > 50) to be cached in
		// memory, default is 20
		BitmapAjaxCallback.setCacheLimit(40);

		// set the max size of an image to be cached in memory, default is 1600
		// pixels (ie. 400x400)
		BitmapAjaxCallback.setPixelLimit(600 * 600);

		// set the max size of the memory cache, default is 1M pixels (4MB)
		BitmapAjaxCallback.setMaxPixelLimit(4000000);
		super.onCreate();
	}

	@Override
	public void onLowMemory() {
		// clear all memory cached images when system is in low memory
		// note that you can configure the max image cache count, see
		// CONFIGURATION
		BitmapAjaxCallback.clearCache();
	}

	/**
	 * Get the light typeface
	 * 
	 * @param context
	 * @return
	 */
	public Typeface getLightTypeface(Context context) {
		if (lightTypeface == null) {
			lightTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		}
		return lightTypeface;
	}

	/**
	 * Get the thin typeface
	 * 
	 * @param context
	 * @return
	 */
	public Typeface getThinTypeface(Context context) {
		if (thinTypeface == null) {
			thinTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
		}
		return thinTypeface;
	}

	/**
	 * Get the medium typeface
	 * 
	 * @param context
	 * @return
	 */
	public Typeface getMediumTypeface(Context context) {
		if (mediumTypeface == null) {
			mediumTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
		}
		return mediumTypeface;
	}

	/**
	 * Get the italic typeface
	 * 
	 * @param context
	 * @return
	 */
	public Typeface getItalicTypeface(Context context) {
		if (italicTypeface == null) {
			italicTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
		}
		return italicTypeface;
	}
	
	/**
	 * Get the alternate typeface
	 * 
	 * @param context
	 * @return
	 */
	public Typeface getAlternateTypeface(Context context) {
		if (alternateTypeface == null) {
			alternateTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Rosario-Bold.ttf");
		}
		return alternateTypeface;
	}

}
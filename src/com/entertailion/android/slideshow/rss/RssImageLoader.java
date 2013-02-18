/*
 * Copyright (C) 2013 ENTERTAILION LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.entertailion.android.slideshow.rss;

import android.util.Log;

import com.entertailion.android.slideshow.images.ImageLoader;
import com.entertailion.android.slideshow.images.ImageManager;

public class RssImageLoader extends ImageLoader {
	private static final String LOG_TAG = "RssImageLoader";

	public RssImageLoader(ImageManager sInstance, String query) {
		super(sInstance, query);

	}

	@Override
	public void doRun() {
		try {
			super.doRss();
		} catch (Exception e) {
			Log.e(LOG_TAG, "run", e);
		}
	}

}
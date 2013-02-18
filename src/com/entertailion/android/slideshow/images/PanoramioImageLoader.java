/*
 * Copyright (C) 2011 Google Inc.
 * Copyright (C) 2013 ENTERTAILION, LLC.
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
package com.entertailion.android.slideshow.images;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.entertailion.android.slideshow.R;
import com.entertailion.android.slideshow.utils.Utils;

public class PanoramioImageLoader extends ImageLoader {
	private static final String LOG_TAG = "PanoramioImageLoader";

	/**
	 * Base URL for Panoramio's web API
	 * http://www.panoramio.com/api/widget/api.html
	 */
	private static final String THUMBNAIL_URL = "http://www.panoramio.com/map/get_panoramas.php?" + "order=date_desc" + "&set=recent" + "&from=0" + "&to=500"
			+ "&size=original";

	double mMinLong;

	double mMaxLong;

	double mMinLat;

	double mMaxLat;

	public PanoramioImageLoader(ImageManager sInstance, String query) {
		super(sInstance, query);
		Context context = sInstance.getContext();
		try {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Location location = locationManager.getLastKnownLocation("static");
			mMinLong = mMaxLong = location.getLongitude();
			mMinLat = mMaxLat = location.getLatitude();
		} catch (Exception e) {
			Log.e(LOG_TAG, "LocationManager error", e);
		}
	}

	@Override
	public void doRun() throws Exception {
		String str = Utils.getCachedData(imageManager.getContext(), THUMBNAIL_URL, true);
		Log.d(LOG_TAG, "json=" + str);
		final JSONObject json = new JSONObject(str);
		doSleep();
		parse(json);
	}

	private void parse(JSONObject json) throws Exception {
		final JSONArray array = json.getJSONArray("photos");
		final int count = array.length();
		for (int i = 0; i < count; i++) {
			final JSONObject obj = array.getJSONObject(i);
			final long id = obj.getLong("photo_id");
			String title = obj.getString("photo_title");
			final String owner = obj.getString("owner_name");
			String original = obj.getString("photo_file_url");
			final String ownerUrl = obj.getString("owner_url");
			final String photoUrl = obj.getString("photo_url");
			final double latitude = obj.getDouble("latitude");
			final double longitude = obj.getDouble("longitude");
			final double width = obj.getDouble("width");
			final double height = obj.getDouble("height");
			if (title == null) {
				title = imageManager.getContext().getString(R.string.untitled);
			}
			if ((width >= 1024 || height >= 1024) && (width <= 2048 || height <= 2048)) {
				doSleep();
				String thumbnail = original.replace("/original/", "/medium/");
				Log.d(LOG_TAG, original);
				final PanoramioImageItem imageItem = new PanoramioImageItem(imageManager.getContext(), id, thumbnail, original, title, owner, ownerUrl, photoUrl,
						latitude + "," + longitude);
				addItem(imageItem);
			}
		}
	}
}
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

package com.entertailion.android.slideshow.images;

import android.content.Context;

/**
 * Holds one item returned from the Panoramio server. This includes the bitmap
 * along with other meta info.
 */
public class PanoramioImageItem extends ImageItem {
	private static final String LOG_TAG = "PanoramioImageItem";
	private long id;

	public PanoramioImageItem(Context context, long id, String thumbUrl, String imageUrl, String title, String owner, String ownerUrl, String pageUrl,
			String location) {
		super(context, thumbUrl, imageUrl, title, owner, ownerUrl, pageUrl, location);
		this.id = id;
	}

	public long getId() {
		return id;
	}

}

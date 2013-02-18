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
package com.entertailion.android.slideshow.images;

import android.content.Context;

public class ImageItem {
	private static final String LOG_TAG = "ImageItem";

	protected String title;

	protected String owner;
	
	protected String thumbUrl;

	protected String imageUrl;
	
	protected String ownerUrl;

	protected String pageUrl;

	protected Context context;

	protected String location;

	public ImageItem(Context context, String thumbUrl, String imageUrl, String title, String owner, String ownerUrl, String pageUrl, String location) {
		this.title = title;
		this.owner = owner;
		this.thumbUrl = thumbUrl;
		this.imageUrl = imageUrl;
		this.ownerUrl = ownerUrl;
		this.pageUrl = pageUrl;
		this.context = context;
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public String getTitle() {
		return title;
	}

	public String getOwner() {
		return owner;
	}
	
	public String getThumbUrl() {
		return thumbUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getOwnerUrl() {
		return ownerUrl;
	}

	public String getPageUrl() {
		return pageUrl;
	}

}

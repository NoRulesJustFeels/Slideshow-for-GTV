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
package com.entertailion.android.slideshow.bookmark;

import java.text.Collator;
import java.util.Comparator;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Data structure for browser bookmarks.
 * @see BookmarkAdapter
 * 
 * @author leon_nicholls
 * 
 */
public class BookmarkInfo {
	private int position;
	private String title;
	private Intent intent;
	private Drawable drawable;
	private String url;

	public BookmarkInfo() {

	}

	public BookmarkInfo(int position, String title, Intent intent) {
		this.position = position;
		this.title = title;
		this.intent = intent;
	}

	public String getTitle() {
		return title;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	public void renderIcon(ImageView imageView) {
		if (drawable != null) {
			imageView.setImageDrawable(drawable);
			return;
		}
		imageView.setImageResource(android.R.drawable.ic_input_get);
	}

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<BookmarkInfo> ALPHA_COMPARATOR = new Comparator<BookmarkInfo>() {
		private final Collator collator = Collator.getInstance();

		public int compare(BookmarkInfo object1, BookmarkInfo object2) {
			return collator.compare(object1.getTitle(), object2.getTitle());
		}
	};

	@Override
	public String toString() {
		return "Item [title=" + getTitle() + ", intent=" + getIntent() + ", position=" + position + "]";
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}

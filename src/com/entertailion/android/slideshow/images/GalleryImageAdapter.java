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
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.androidquery.AQuery;
import com.entertailion.android.slideshow.R;
import com.entertailion.android.slideshow.widget.EcoGallery;

/**
 * This is the Adapter for thumbnail gallery widget of photos in ViewImage
 * Activity.
 */
public class GalleryImageAdapter extends BaseAdapter {
	private static final String LOG_TAG = "GalleryImageAdapter";

	/**
	 * Data structure to cache references for performance.
	 * 
	 */
	private static class ViewHolder {
		public AQuery aq;
	}

	/**
	 * Maintains the state of our data.
	 */
	private final ImageManager imageManager;

	private final Context context;

	private final ImageDataSetObserver imageDataSetObserver;

	private final int thumbnailSize;

	private final EcoGallery.LayoutParams layoutParams;

	/**
	 * Used by the {@link ImageManager} to report changes in the list back to
	 * this adapter.
	 */
	private class ImageDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
	}

	public GalleryImageAdapter(Context context) {
		this.context = context;
		imageManager = ImageManager.getInstance(context);
		imageDataSetObserver = new ImageDataSetObserver();
		imageManager.addObserver(imageDataSetObserver);
		final Resources resources = context.getResources();
		thumbnailSize = (int) resources.getDimensionPixelSize(R.dimen.thumbnail_size);
		layoutParams = new EcoGallery.LayoutParams(thumbnailSize / 2, thumbnailSize / 2);
	}

	/**
	 * Returns the number of images to display
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return imageManager.size();
	}

	/**
	 * Returns the image at a specified position
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		return imageManager.get(position);
	}

	/**
	 * Returns the id of an image at a specified position
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Returns a view to display the image at a specified position
	 * 
	 * @param position
	 *            The position to display
	 * @param convertView
	 *            An existing view that we can reuse. May be null.
	 * @param parent
	 *            The parent view that will eventually hold the view we return.
	 * @return A view to display the image at a specified position
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView iconView = (ImageView) convertView;
		if (iconView == null) {
			iconView = new ImageView(context);
			iconView.setScaleType(ScaleType.CENTER_CROP);
			// Fix for Gallery setUnselectedAlpha bug:
			// 1. Disable hardware acceleration on activity
			// 2. Use style="android:galleryItemBackground" in xml
			// 3. Then explicitly set the background color for each view:
			iconView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
			iconView.setLayoutParams(layoutParams);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.aq = new AQuery(parent);
			iconView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) iconView.getTag();
		final ImageItem imageItem = imageManager.get(position);

		if (imageItem != null) {
			iconView.setBackgroundResource(R.drawable.camera_rectangle);
			iconView.setContentDescription(imageItem.getTitle());
			// https://code.google.com/p/android-query/wiki/ImageLoading
			holder.aq.id(iconView).image(imageItem.getThumbUrl(), true, true, thumbnailSize, R.drawable.warning);
		} else {
			iconView.setBackgroundResource(R.drawable.warning);
		}

		return iconView;
	}
}

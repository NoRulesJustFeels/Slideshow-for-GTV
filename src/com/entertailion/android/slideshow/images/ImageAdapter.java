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
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.entertailion.android.slideshow.R;

/**
 * Adapter used to bind data for the GridView in ImageGrid Activity. This
 * Adapter is updated by the ImageManager.
 */
public class ImageAdapter extends BaseAdapter {
	private static final String LOG_TAG = "ImageAdapter";

	/**
	 * Data structure to cache references for performance.
	 * 
	 */
	private static class ViewHolder {
		public ImageView imageView;
		public AQuery aq;
	}

	/**
	 * Maintains the state of our data
	 */
	private final ImageManager imageManager;

	private final Context context;

	private final MyDataSetObserver dataSetObserver;

	private final LayoutInflater layoutInflator;

	private final int thumbnailSize;

	/**
	 * Used by the {@link ImageManager} to report changes in the list back to
	 * this adapter.
	 */
	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
	}

	public ImageAdapter(Context c) {
		imageManager = ImageManager.getInstance(c);
		context = c;
		dataSetObserver = new MyDataSetObserver();
		imageManager.addObserver(dataSetObserver);

		layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Resources resources = context.getResources();
		thumbnailSize = (int) resources.getDimensionPixelSize(R.dimen.thumbnail_size);
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
		View view = convertView;
		if (view == null) {
			view = layoutInflator.inflate(R.layout.image_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) view.findViewById(R.id.image);
			viewHolder.aq = new AQuery(view);
			view.setTag(viewHolder);
		}

		final ViewHolder holder = (ViewHolder) view.getTag();
		final ImageItem imageItem = imageManager.get(position);

		if (imageItem!=null) {
			holder.imageView.setBackgroundResource(R.drawable.camera_rectangle);
			holder.imageView.setContentDescription(imageItem.getTitle());
			// https://code.google.com/p/android-query/wiki/ImageLoading
			///AQuery aq = new AQuery(view);
			Bitmap placeholder = holder.aq.getCachedImage(R.drawable.camera);
			if (holder.aq.shouldDelay(position, view, parent, imageItem.getThumbUrl())) {
				holder.aq.id(holder.imageView).image(placeholder);
			} else {
				holder.aq.id(holder.imageView).image(imageItem.getThumbUrl(), true, true, thumbnailSize, R.drawable.warning, new BitmapAjaxCallback() {
	
					@Override
					public void callback(String url, ImageView view,
							Bitmap bitmap, AjaxStatus status) {
	
						view.setImageBitmap(bitmap);
					}
	
				});
			}
		} else {
			holder.imageView.setBackgroundResource(R.drawable.warning);
		}

		return view;
	}
}

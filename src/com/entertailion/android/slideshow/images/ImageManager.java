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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;

/**
 * This class is responsible for downloading and parsing the search results for
 * a particular area. All of the work is done on a separate thread, and progress
 * is reported back through the DataSetObserver set in
 * {@link #addObserver(DataSetObserver). State is held in memory by in memory
 * maintained by a single instance of the ImageManager class. *
 */
public class ImageManager {
	private static final String LOG_TAG = "ImageManager";

	/**
	 * Holds the single instance of a ImageManager that is shared by the
	 * process.
	 */
	private static ImageManager imageManager;

	/**
	 * Holds the images and related data that have been downloaded
	 */
	private final ArrayList<ImageItem> images = new ArrayList<ImageItem>();

	private int currentPosition;

	/**
	 * Observers interested in changes to the current search results
	 */
	private final ArrayList<WeakReference<DataSetObserver>> dataSetObservers = new ArrayList<WeakReference<DataSetObserver>>();

	/**
	 * True if we are in the process of loading
	 */
	protected boolean loading;

	protected boolean error;

	protected Context context;

	protected volatile String query;

	/**
	 * Key for an Intent extra. The value is an item to display
	 */
	public static final String SLIDESHOW_ITEM_POSITION = "position";
	public static final String SLIDESHOW_STATE = "state";

	public static ImageManager getInstance(Context c) {
		if (imageManager == null) {
			imageManager = new ImageManager(c);
		}
		return imageManager;
	}

	private ImageManager(Context c) {
		context = c;
	}

	/**
	 * @return True if we are still loading content
	 */
	public boolean isLoading() {
		return loading;
	}

	/**
	 * Clear all downloaded content
	 */
	public void clear() {
		images.clear();
		notifyInvalidateObservers();
	}

	public ImageItem getNext() {
		if (currentPosition + 1 <= images.size() - 1) {
			currentPosition = currentPosition + 1;
			return images.get(currentPosition);
		}
		return null;
	}

	public ImageItem getPrevious() {
		if (currentPosition - 1 >= 0) {
			currentPosition = currentPosition - 1;
			return images.get(currentPosition);
		}
		return null;
	}

	/**
	 * Add an item to and notify observers of the change.
	 * 
	 * @param item
	 *            The item to add
	 */
	public void add(ImageItem item) {
		if (loading) {
			images.add(item);
			notifyObservers();
		}
	}

	/**
	 * @return The number of items displayed so far
	 */
	public int size() {
		return images.size();
	}

	/**
	 * Gets the item at the specified position
	 */
	public ImageItem get(int position) {
		currentPosition = position;
		if (images.size() > position) {
			return images.get(position);
		}
		return null;
	}

	/**
	 * Adds an observer to be notified when the set of items held by this
	 * ImageManager changes.
	 */
	public void addObserver(DataSetObserver observer) {
		final WeakReference<DataSetObserver> obs = new WeakReference<DataSetObserver>(
				observer);
		dataSetObservers.add(obs);
	}

	ImageLoader mPrevThread = null;

	/**
	 * Load a new set of search results for the specified area.
	 * 
	 * @param minLong
	 *            The minimum longitude for the search area
	 * @param maxLong
	 *            The maximum longitude for the search area
	 * @param minLat
	 *            The minimum latitude for the search area
	 * @param maxLat
	 *            The minimum latitude for the search area
	 * @throws JSONException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public void load(String query) throws IOException, URISyntaxException,
			JSONException {
		this.query = query;
		loading = false;
		if (mPrevThread != null && !mPrevThread.isInterrupted()) {
			mPrevThread.interrupt();
			while (mPrevThread.isAlive()) {
				Log.d(LOG_TAG, "waiting for previous thread...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			Log.d(LOG_TAG, "thread dead");
			clear();
			BitmapAjaxCallback.cancel();
			AjaxCallback.cancel();
			mPrevThread = null;
		} else {
			clear();
		}
		loading = true;
		error = false;

		mPrevThread = ImageLoaderFactory.getImageLoader(imageManager, query);
		mPrevThread.setPriority(Thread.MIN_PRIORITY);
		mPrevThread.start();
	}

	/**
	 * Called when something changes in our data set. Cleans up any weak
	 * references that are no longer valid along the way.
	 */
	public void notifyObservers() {
		Log.d(LOG_TAG, "notifyObservers: " + loading);
		if (loading) {
			final ArrayList<WeakReference<DataSetObserver>> observers = dataSetObservers;
			final int count = observers.size();
			for (int i = count - 1; i >= 0; i--) {
				final WeakReference<DataSetObserver> weak = observers.get(i);
				final DataSetObserver obs = weak.get();
				if (obs != null) {
					obs.onChanged();
				} else {
					observers.remove(i);
				}
			}
		}
	}

	/**
	 * Called when something changes in our data set. Cleans up any weak
	 * references that are no longer valid along the way.
	 */
	public void notifyInvalidateObservers() {
		Log.d(LOG_TAG, "notifyInvalidateObservers: " + loading);
		if (loading) {
			final ArrayList<WeakReference<DataSetObserver>> observers = dataSetObservers;
			final int count = observers.size();
			for (int i = count - 1; i >= 0; i--) {
				final WeakReference<DataSetObserver> weak = observers.get(i);
				final DataSetObserver obs = weak.get();
				if (obs != null) {
					obs.onInvalidated();
				} else {
					observers.remove(i);
				}
			}
		}
	}

	public Context getContext() {
		return context;
	}

	public void setLoading(boolean state) {
		loading = state;
	}

	public String getQuery() {
		return query;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean state) {
		error = state;
	}
	
	public boolean isRunning() {
		if (mPrevThread!=null) {
			return mPrevThread.isRunning();
		}
		return false;
	}
}

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.entertailion.android.slideshow.rss.RssFeed;
import com.entertailion.android.slideshow.rss.RssHandler;
import com.entertailion.android.slideshow.rss.RssImageItem;
import com.entertailion.android.slideshow.rss.RssItem;
import com.entertailion.android.slideshow.utils.Utils;

/**
 * Utility class to asynchronously load the web site photo data in a thread.
 * @see FlickrImageLoader, TumblrImageLoader, PinterestImageLoader
 * 
 * @author leon_nicholls
 *
 */
public abstract class ImageLoader extends Thread {
	private static final String LOG_TAG = "ImageLoader";
	
	private static final int SLEEP_DELAY = 100;

	/**
	 * Used to post results back to the UI thread
	 */
	protected static Handler handler = new Handler();

	protected ImageManager imageManager;
	protected String query;
	protected boolean error;
	private boolean running;
	private int sleepDelay = SLEEP_DELAY;

	public ImageLoader(ImageManager imageManager, String query) {
		this.imageManager = imageManager;
		this.query = query;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler.post(new Runnable() {
			public void run() {
				imageManager.setError(false);
				imageManager.setLoading(true);
			}
		});
		error = false;
		running = true;
		try {
			doSleep();
			doRun();
			if (imageManager.size() == 0) {
				doRss();
			}
			doSleep();
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, "run", e);
		} catch (Exception e) {
			error = true;
			Log.e(LOG_TAG, "run", e);
		}
		running = false;
		handler.post(new Runnable() {
			public void run() {
				Log.d(LOG_TAG, "run: error="+error+", size="+imageManager.size()+", loading="+imageManager.isLoading());
				if (imageManager.isLoading()) {
					if (error) {
						imageManager.setError(true);
						imageManager.notifyInvalidateObservers();
						imageManager.setLoading(false);
					} else {
						imageManager.setLoading(false);
						imageManager.notifyObservers();
					}
				}
			}
		});
	}
	
	protected void addItem(final ImageItem imageItem) {
		if (query.equals(imageManager.getQuery())) {
			handler.post(new Runnable() {
				public void run() {
					imageManager.add(imageItem);
				}
			});
		}
	}

	protected void doRss() throws Exception {
		// By default use the site RSS feed data
		String rss = Utils.getRssFeed(query, imageManager.getContext(), true);
		RssHandler rh = new RssHandler();
		RssFeed rssFeed = rh.getFeed(rss);
		Uri uri = Uri.parse(query);
		if (rssFeed.getTitle() == null) {
			rssFeed.setTitle(uri.getHost());
		}
		for (RssItem item : rssFeed.getItems()) {
			Log.d(LOG_TAG, item.getDescription());
			if (item.getDescription() != null) {
				Document doc = Jsoup.parseBodyFragment(item.getDescription());
				Element img = doc.select("img").first();
				if (img != null) {
					String src = img.attr("src");
					if (src != null) {
						Log.d(LOG_TAG, src);
						doSleep();
						final RssImageItem imageItem = new RssImageItem(imageManager.getContext(), src, src, item.getTitle(), uri.getHost(), item.getLink(),
								"http://" + uri.getHost(), query);
						addItem(imageItem);
					}
				}
			}
		}
	}

	public abstract void doRun() throws Exception;
	
	protected void doSleep() throws Exception {
		Thread.sleep(sleepDelay++);
	}
	
	public boolean isRunning() {
		return running;
	}

}
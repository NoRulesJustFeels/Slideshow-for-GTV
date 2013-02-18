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
package com.entertailion.android.slideshow.images;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.util.Log;

import com.entertailion.android.slideshow.utils.Utils;

public class TumblrImageLoader extends ImageLoader {
	private static final String LOG_TAG = "TumblrImageLoader";

	private static final int MAX_PAGES = 10;

	public TumblrImageLoader(ImageManager sInstance, String query) {
		super(sInstance, query);

	}

	@Override
	public void doRun() throws Exception {
		Uri uri = Uri.parse(query);
		int page = 1;
		do {
			String url = "http://" + uri.getHost() + "/page/" + page;
			String html = Utils.getCachedData(imageManager.getContext(), url, true);
			boolean found = false;
			// scrape HTML for images
			Document doc = Jsoup.parse(html);
			// http://jsoup.org/cookbook/extracting-data/selector-syntax
			Elements titles = doc.select("title");
			String title = null;
			if (titles != null && titles.size() > 0) {
				title = titles.first().text();
			}
			Elements images = doc.select(".post img");
			if (images == null || images.size() == 0) {
				images = doc.select("article img");
			}
			if (images == null || images.size() == 0) {
				images = doc.select("item img");
			}
			if (images == null || images.size() == 0) {
				images = doc.select(".posts img");
			}
			for (Element image : images) {
				String src = image.attr("src");
				Log.d(LOG_TAG, uri.getHost() + ": " + src);
				String imageUrl = null;
				if (src.contains("_1280.")) {
					imageUrl = src;
				} else if (src.contains("_250.")) {
					// check if larger resolution available
					String largerImage = src.replace("_250.", "_1280.");
					if (Utils.checkUrl(largerImage)) {
						imageUrl = largerImage;
					}
				} else if (src.contains("_500.")) {
					// check if larger resolution available
					String largerImage = src.replace("_500.", "_1280.");
					if (Utils.checkUrl(largerImage)) {
						imageUrl = largerImage;
					} else {
						// accept 500
						imageUrl = src;
					}
				} else if (src.contains("_400.")) {
					// check if larger resolution available
					String largerImage = src.replace("_400.", "_1280.");
					if (Utils.checkUrl(largerImage)) {
						imageUrl = largerImage;
					}
				}
				if (imageUrl != null) {
					String pageUrl = url;
					Element parent = image.parent();
					if (parent.nodeName().equals("a")) {
						String href = parent.attr("href");
						if (href != null) {
							pageUrl = href;
						}
					}
					doSleep();
					if (image.attr("alt") != null) {
						title = image.attr("alt");
					}
					final ImageItem imageItem = new ImageItem(imageManager.getContext(), src, imageUrl, title, uri.getHost(), url, pageUrl, url);
					addItem(imageItem);
					found = true;
				}
			}

			if (!found) {
				break;
			}
		} while (++page < MAX_PAGES);
	}

}
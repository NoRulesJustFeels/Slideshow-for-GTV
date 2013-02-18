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

public class FiveHundredPxImageLoader extends ImageLoader {
	private static final String LOG_TAG = "FiveHundredPxImageLoader";

	private static final int MAX_PAGES = 10;

	public FiveHundredPxImageLoader(ImageManager sInstance, String query) {
		super(sInstance, query);

	}

	@Override
	public void doRun() throws Exception {
		// get icon
		Uri uri = Uri.parse(query);
		int page = 1;
		do {
			String url = query + "?page=" + page;
			String html = Utils.getCachedData(imageManager.getContext(), url, true);
			boolean found = false;
			// scrape HTML for images
			Document doc = Jsoup.parse(html);
			Elements titles = doc.select("title");
			String title = null;
			if (titles != null && titles.size() > 0) {
				title = titles.first().text();
			}
			// http://jsoup.org/cookbook/extracting-data/selector-syntax
			Elements divs = doc.select("div.photo");
			for (Element div : divs) {
				Elements images = div.select("img");
				if (images != null && images.size() > 0) {
					Element image = images.first();
					String src = image.attr("src");
					Log.d(LOG_TAG, uri.getHost() + ": " + src);
					String imageUrl = null;
					if (src.contains("/3.jpg")) {
						// check if larger resolution available
						String largerImage = src.replace("/3.jpg", "/4.jpg");
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
								pageUrl = "http://" + uri.getHost() + href;
							}
						}
						// TODO title, info...
						doSleep();
						if (image.attr("alt") != null) {
							title = image.attr("alt");
						}
						final ImageItem imageItem = new ImageItem(imageManager.getContext(), src, imageUrl, title, uri.getHost(), url, pageUrl, url);
						addItem(imageItem);
						found = true;
					}
				}
			}

			if (!found) {
				break;
			}
		} while (++page < MAX_PAGES);

	}

}
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

public class PinterestImageLoader extends ImageLoader {
	private static final String LOG_TAG = "PinterestImageLoader";

	public PinterestImageLoader(ImageManager sInstance, String query) {
		super(sInstance, query);

	}

	@Override
	public void doRun() throws Exception {
		Uri uri = Uri.parse(query);
		String html = Utils.getCachedData(imageManager.getContext(), query, true);
		Document doc = Jsoup.parse(html);
		// http://jsoup.org/cookbook/extracting-data/selector-syntax
		Elements titles = doc.select("title");
		String title = null;
		if (titles != null && titles.size() > 0) {
			title = titles.first().text();
		}
		Elements divs = doc.select("div.pin");
		for (Element div : divs) {
			Elements images = div.select("img");
			if (images != null && images.size() > 0) {
				Element image = images.first();
				String src = image.attr("src");
				String closeup = div.attr("data-closeup-url");
				if (closeup == null) {
					closeup = src;
				}
				if (src != null) {
					Log.d(LOG_TAG, src);
					doSleep();
					if (image.attr("alt") != null) {
						title = image.attr("alt");
					}
					final ImageItem imageItem = new ImageItem(imageManager.getContext(), src, closeup, title, uri.getHost(), query, src, query);
					addItem(imageItem);
				}
			}
		}
	}

}
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

import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.util.Log;

import com.entertailion.android.slideshow.utils.Utils;

public class WikiMediaImageLoader extends ImageLoader {
	private static final String LOG_TAG = "WikiMediaImageLoader";

	public WikiMediaImageLoader(ImageManager sInstance, String query) {
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
		Elements divs = doc.select(".thumb");
		for (Element div : divs) {
			Elements images = div.select("img");
			if (images != null && images.size() > 0) {
				Element image = images.first();
				String alt = image.attr("alt");
				String src = image.attr("src");
				Log.d(LOG_TAG, uri.getHost() + ": " + src);
				// http://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Sint_Micha%C3%ABlkerk_Harlingen.jpg/320px-Sint_Micha%C3%ABlkerk_Harlingen.jpg
				// http://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Sint_Micha%C3%ABlkerk_Harlingen.jpg/1024px-Sint_Micha%C3%ABlkerk_Harlingen.jpg
				// http://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Sint_Micha%C3%ABlkerk_Harlingen.jpg/1280px-Sint_Micha%C3%ABlkerk_Harlingen.jpg
				// http://upload.wikimedia.org/wikipedia/commons/4/40/Sint_Micha%C3%ABlkerk_Harlingen.jpg
				// <img alt="Sint Micha‘lkerk Harlingen.jpg"
				// src="//upload.wikimedia.org/wikipedia/commons/thumb/4/40/Sint_Micha%C3%ABlkerk_Harlingen.jpg/143px-Sint_Micha%C3%ABlkerk_Harlingen.jpg"
				// width="143" height="107">
				String imageFileName = src.substring(src.lastIndexOf('-') + 1);
				StringTokenizer tokenizer = new StringTokenizer(src, "/");
				String path = null;
				if (tokenizer.hasMoreElements()) {
					path = "http://" + (String) tokenizer.nextElement() + "/" + (String) tokenizer.nextElement() + "/" + (String) tokenizer.nextElement() + "/"
							+ (String) tokenizer.nextElement() + "/" + (String) tokenizer.nextElement() + "/" + (String) tokenizer.nextElement() + "/"
							+ imageFileName;
				}
				// check if larger resolution available
				String otherSrc = path + "/320px-" + imageFileName;
				if (Utils.checkUrl(otherSrc)) {
					src = otherSrc;
				}
				String imageUrl = null;
				otherSrc = path + "/1280px-" + imageFileName;
				if (Utils.checkUrl(otherSrc)) {
					imageUrl = otherSrc;
				}
				if (imageUrl == null) {
					otherSrc = path + "/1024px-" + imageFileName;
					if (Utils.checkUrl(otherSrc)) {
						imageUrl = otherSrc;
					}
				}
				if (imageUrl != null) {
					String pageUrl = query;
					Element parent = image.parent();
					if (parent.nodeName().equals("a")) {
						String href = parent.attr("href");
						if (href != null) {
							pageUrl = "http://" + uri.getHost() + href;
						}
					}
					doSleep();
					final ImageItem imageItem = new ImageItem(imageManager.getContext(), src, imageUrl, alt, uri.getHost(), query, pageUrl, query);
					addItem(imageItem);
				}
			}
		}
	}

}
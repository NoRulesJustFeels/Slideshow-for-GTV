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

public class HubbleImageLoader extends ImageLoader {
	private static final String LOG_TAG = "HubbleImageLoader";

	public HubbleImageLoader(ImageManager sInstance, String query) {
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
		Elements anchors = doc.select("a.icon");
		Log.d(LOG_TAG, "anchors=" + anchors.size());
		for (Element anchor : anchors) {
			Elements images = anchor.select("img");
			Log.d(LOG_TAG, "images=" + images.size());
			if (images != null && images.size() > 0) {
				Element image = images.first();
				String src = image.attr("src");
				if (src != null) {
					Log.d(LOG_TAG, src);
					String imageUrl = null;
					if (src.contains("-thumb.jpg")) {
						String originalSrc = src;
						// check if larger resolution available
						// http://imgsrc.hubblesite.org/hu/db/images/hs-2013-06-a-thumb.jpg
						// http://imgsrc.hubblesite.org/hu/db/images/hs-2013-06-a-web.jpg
						// http://imgsrc.hubblesite.org/hu/db/images/hs-2013-06-a-large_web.jpg
						// http://imgsrc.hubblesite.org/hu/db/images/hs-2013-06-a-xlarge_web.jpg
						String thumbnailImage = originalSrc.replace("-thumb.jpg", "-web.jpg");
						if (Utils.checkUrl(thumbnailImage)) {
							src = thumbnailImage;
						}
						String largerImage = originalSrc.replace("-thumb.jpg", "-xlarge_web.jpg");
						if (Utils.checkUrl(largerImage)) {
							imageUrl = largerImage;
						}
						if (imageUrl == null) {
							largerImage = originalSrc.replace("-thumb.jpg", "-large_web.jpg");
							if (Utils.checkUrl(largerImage)) {
								imageUrl = largerImage;
							}
						}
					}
					if (imageUrl != null) {
						doSleep();
						if (image.attr("alt") != null) {
							title = image.attr("alt");
						}
						final ImageItem imageItem = new ImageItem(imageManager.getContext(), src, imageUrl, title, uri.getHost(), query, query, query);
						addItem(imageItem);
					}
				}
			}
		}
	}

}
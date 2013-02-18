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
package com.entertailion.android.slideshow.images;

import android.net.Uri;

import com.entertailion.android.slideshow.rss.RssImageLoader;
import com.entertailion.android.slideshow.utils.Analytics;

public class ImageLoaderFactory {
	private static final String LOG_TAG = "ImageLoaderFactory";

	public static ImageLoader getImageLoader(ImageManager imageManager, String query) {
		Uri uri = Uri.parse(query);
		if (uri.getHost().toLowerCase().contains("tumblr.com")) {
			Analytics.logEvent(Analytics.LOADER_TUMBLR);
			return new TumblrImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("pinterest.com")) {
			Analytics.logEvent(Analytics.LOADER_PINTEREST);
			return new PinterestImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("panoramio.com")) {
			Analytics.logEvent(Analytics.LOADER_PANORAMIO);
			return new PanoramioImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("500px.com")) {
			Analytics.logEvent(Analytics.LOADER_FIVE_HUNDRED_PX);
			return new FiveHundredPxImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("whitehouse.gov")) {
			Analytics.logEvent(Analytics.LOADER_WHITE_HOUSE);
			return new WhiteHouseImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("hubblesite.org")) {
			Analytics.logEvent(Analytics.LOADER_HUBBLE);
			return new HubbleImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("photo.net")) {
			Analytics.logEvent(Analytics.LOADER_PHOTO_NET);
			return new PhotoNetImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("wikimedia.org")) {
			Analytics.logEvent(Analytics.LOADER_WIKIMEDIA);
			return new WikiMediaImageLoader(imageManager, query);
		} else if (uri.getHost().toLowerCase().contains("flickr.com")) {
			Analytics.logEvent(Analytics.LOADER_FLICKR);
			return new FlickrImageLoader(imageManager, query);
		} else {
			Analytics.logEvent(Analytics.LOADER_RSS);
			return new RssImageLoader(imageManager, query);
		}
	}
}

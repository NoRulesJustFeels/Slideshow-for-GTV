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

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.util.Log;

import com.entertailion.android.slideshow.utils.Utils;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.groups.GroupsInterface;
import com.googlecode.flickrjandroid.groups.pools.PoolsInterface;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.googlecode.flickrjandroid.photos.Size;
import com.googlecode.flickrjandroid.urls.UrlsInterface;

/**
 * Image loader for Flickr sites. 
 * Uses the flickrj-android library (https://code.google.com/p/flickrj-android/) for the Flickr API: http://www.flickr.com/services/api
 * 
 * @author leon_nicholls
 *
 */
public class FlickrImageLoader extends ImageLoader {
	private static final String LOG_TAG = "FlickrImageLoader";

	private static final int MAX_PAGES = 10;
	
	// http://www.flickr.com/services/apps/create/noncommercial/?
	private static final String FLICKR_API_KEY = "";

	public FlickrImageLoader(ImageManager sInstance, String query) {
		super(sInstance, query);

	}

	@Override
	public void doRun() throws Exception {
		doFlickrApi();

		if (imageManager.size() == 0) {
			doFlickrScreenScrape();
		}
	}
	
	private void doFlickrApi() throws Exception {
		// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/Flickr.java
		Flickr flickr = new Flickr(FLICKR_API_KEY);
		UrlsInterface urlsInterface = flickr.getUrlsInterface();
		String groupName = null;
		String groupId = null;
		// http://www.flickr.com/services/api/flickr.urls.lookupGroup.html
		// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/urls/UrlsInterface.java
		Log.d(LOG_TAG, query);
		Group group = urlsInterface.lookupGroup(query);
		if (group != null) {
			groupName = group.getName();
			groupId = group.getId();
		}

		if (groupId != null) {
			// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/groups/GroupsInterface.java
			GroupsInterface groupsInterface = flickr.getGroupsInterface();
			// http://www.flickr.com/services/api/flickr.groups.pools.getPhotos.html
			// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/groups/pools/PoolsInterface.java
			PoolsInterface poolsInterface = flickr.getPoolsInterface();
			doSleep();
			// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/SearchResultList.java
			PhotoList photoList = poolsInterface.getPhotos(groupId, null, null, 200, 0);
			PhotosInterface photosInterface = flickr.getPhotosInterface();
			for(Photo photo:photoList) {
				// filter the photos
				if (photo.isPublicFlag() && !photo.isFamilyFlag() && !photo.isFriendFlag()) {
					// http://www.flickr.com/services/api/flickr.photos.getSizes.html
					// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/photos/PhotosInterface.java
					String thumbnailUrl = null;
					String imageUrl = null;
					List<Size> sizes = (List<Size>)photosInterface.getSizes(photo.getId());
					// https://code.google.com/p/flickrj-android/source/browse/flickrj-android/src/main/java/com/googlecode/flickrjandroid/photos/Size.java
					for(int i=sizes.size()-1;i>=0;i--) {
						Size size = sizes.get(i);
						if (thumbnailUrl == null) {
							if (size.getWidth() == 500 || size.getHeight() == 500) {
								thumbnailUrl = size.getSource();
							}
						}
						if (thumbnailUrl == null) {
							if (size.getWidth() == 320 || size.getHeight() == 320) {
								thumbnailUrl = size.getSource();
							}
						}
						if (imageUrl == null) {
							if (size.getWidth() == 2048 || size.getHeight() == 2048) {
								imageUrl = size.getSource();
							}
						}
						if (imageUrl == null) {
							if (size.getWidth() == 1600 || size.getHeight() == 1600) {
								thumbnailUrl = size.getSource();
							}
						}
						if (imageUrl == null) {
							if (size.getWidth() == 1024 || size.getHeight() == 1024) {
								thumbnailUrl = size.getSource();
							}
						}
					}
					
					if (thumbnailUrl!=null && imageUrl!=null) {
						// http://www.flickr.com/services/api/misc.urls.html
						String photoUrl = photo.getUrl();
						String profileUrl = photoUrl;
						String username = groupName;
						String location = null;
						if (photo.getOwner()!=null) {
							photoUrl = "http://www.flickr.com/photos/"+photo.getOwner().getId()+"/"+photo.getId();
							profileUrl = photo.getOwner().getProfileurl();
							username = photo.getOwner().getUsername();
						}
						if (photo.getGeoData()!=null) {
							location = photo.getGeoData().toString();
						}
						Log.d(LOG_TAG, thumbnailUrl+", "+imageUrl);
						doSleep();
						final ImageItem imageItem = new ImageItem(imageManager.getContext(), thumbnailUrl, imageUrl, photo.getTitle(), username, profileUrl, photoUrl, location);
						addItem(imageItem);
					}
				} else {
					Log.w(LOG_TAG, "filtered photo: "+photo);
				}
			}
		}
	}
	
	private void doFlickrScreenScrape() throws Exception {
		Log.d(LOG_TAG, "doFlickrScreenScrape");
		Uri uri = Uri.parse(query);
		int page = 1;
		do {
			String url = uri.getScheme()+"://"+uri.getHost()+uri.getPath();
			if (url.indexOf("/pool/")==-1) {
				url = url + "pool/?view=md";
			}
			Log.d(LOG_TAG, url);
			url = url.replaceAll("/\\?", "/page"+page+"/?");
			url = url.replaceAll("view=sm", "view=md");
			url = url.replaceAll("view=ju", "view=md");
			url = url.replaceAll("view=sq", "view=md");
			Log.d(LOG_TAG, "url="+url);
			String html = Utils.getCachedData(imageManager.getContext(), url, true);
			boolean found = false;
			// scrape HTML for images
			Document doc = Jsoup.parse(html);
			// http://jsoup.org/cookbook/extracting-data/selector-syntax
			Elements titles = doc.select("title");
			String title = null;
			if (titles!=null && titles.size()>0) {
				title = titles.first().text();
			}
			Elements images = doc.select(".thumb img");
			for (Element image : images) {
				String src = image.attr("src");
				Log.d(LOG_TAG, uri.getHost() + ": " + src);
				String imageUrl = null;
				String thumbUrl = null;
				// <img src="http://farm3.staticflickr.com/2621/4146982527_73fce3293b_n.jpg"> //320
				// <img src="http://farm3.staticflickr.com/2621/4146982527_73fce3293b.jpg">   //500
				// <img src="http://farm3.staticflickr.com/2621/4146982527_73fce3293b_m.jpg"> //240
				// <img src="http://farm3.staticflickr.com/2621/4146982527_73fce3293b_b.jpg"> //1024
				// <img src="http://farm3.staticflickr.com/2621/4146982527_73fce3293b_h.jpg"> //1600
				// <img src="http://farm3.staticflickr.com/2621/4146982527_73fce3293b_k.jpg"> //2048
				
				if (src.contains("_m.jpg")) {
					// check if larger resolution available
					String largerImage = src.replace("_m.jpg", ".jpg");
					if (Utils.checkUrl(largerImage)) {
						thumbUrl = largerImage;
					}
					if (thumbUrl==null) {
						largerImage = src.replace("_m.jpg", "_n.jpg");
						if (Utils.checkUrl(largerImage)) {
							thumbUrl = largerImage;
						}
					}
					if (thumbUrl==null) {
						thumbUrl = src;
					}
					largerImage = src.replace("_m.jpg", "_k.jpg");
					if (Utils.checkUrl(largerImage)) {
						imageUrl = largerImage;
					}
					if (imageUrl==null) {
						largerImage = src.replace("_m.jpg", "_h.jpg");
						if (Utils.checkUrl(largerImage)) {
							imageUrl = largerImage;
						}
					}
					if (imageUrl==null) {
						largerImage = src.replace("_m.jpg", "_b.jpg");
						if (Utils.checkUrl(largerImage)) {
							imageUrl = largerImage;
						}
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
					doSleep();
					if (image.attr("alt")!=null) {
						title = image.attr("alt");
					}
					final ImageItem imageItem = new ImageItem(imageManager.getContext(), thumbUrl, imageUrl, title, uri.getHost(), url, pageUrl, url);
					handler.post(new Runnable() {
						public void run() {
							imageManager.add(imageItem);
						}
					});
					found = true;
				}
			}

			if (!found) {
				break;
			}
		} while (++page < MAX_PAGES);
	}

}
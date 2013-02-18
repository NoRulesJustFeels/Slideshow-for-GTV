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
package com.entertailion.android.slideshow.rss;

import java.util.ArrayList;
import java.util.Date;

import android.graphics.Bitmap;

/**
 * Data structure for RSS feed
 * 
 * @author leon_nicholls
 * 
 */
public class RssFeed implements Comparable<RssFeed> {
	private static final String LOG_TAG = "RssFeed";
	private int id;
	private String title;
	private String link;
	private String description;
	private Date date;
	private Date viewDate;
	private ArrayList<RssItem> items;
	private String logo;
	private String image;
	private Bitmap bitmap;
	private int ttl;

	public RssFeed(int id, String title, String link, String description, Date date, Date viewDate, String logo, String image, Bitmap bitmap, int ttl) {
		this();
		this.id = id;
		this.title = title;
		this.link = link;
		this.description = description;
		this.date = date;
		this.viewDate = viewDate;
		this.logo = logo;
		this.image = image;
		this.bitmap = bitmap;
		this.ttl = ttl;
	}

	public RssFeed() {
		this.items = new ArrayList<RssItem>();
		this.date = new Date(0);
		this.viewDate = new Date(0);
		this.ttl = -1;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<RssItem> getItems() {
		return items;
	}

	public void addItem(RssItem item) {
		items.add(item);
	}

	public Date getViewDate() {
		return viewDate;
	}

	public void setViewDate(Date viewDate) {
		this.viewDate = viewDate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public int compareTo(RssFeed other) {
		if (getDate() != null && other.getDate() != null) {
			return getDate().compareTo(other.getDate());
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return title;
	}

}

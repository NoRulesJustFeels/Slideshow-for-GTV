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
package com.entertailion.android.slideshow.sites;

import java.util.ArrayList;

import com.entertailion.android.slideshow.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for featured web sites.
 * @see res/xml/sites.xml
 * 
 * @author leon_nicholls
 * 
 */
public class SiteAdapter extends ArrayAdapter<SiteInfo> {
	private static final String LOG_TAG = "SiteAdapter";
	private Context context;
	private LayoutInflater inflater;

	/**
	 * Data structure for caching reference for performance.
	 * 
	 */
	private static class ViewHolder {
		public TextView textView;
		public ImageView imageView;
	}

	public SiteAdapter(Context context, ArrayList<SiteInfo> bookmarks) {
		super(context, 0, bookmarks);
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			rowView = inflater.inflate(R.layout.list_row, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) rowView.findViewById(R.id.label);
			viewHolder.imageView = (ImageView) rowView.findViewById(R.id.icon);
			viewHolder.imageView.setImageResource(R.drawable.ic_menu_bookmark);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		final SiteInfo info = getItem(position);

		holder.textView.setText(info.getTitle());

		return rowView;
	}
}
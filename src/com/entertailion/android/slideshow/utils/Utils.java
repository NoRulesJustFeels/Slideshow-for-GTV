/*
 * Copyright (C) 2013 ENTERTAILION LLC
 * Copyright (C) 2008 The Android Open Source Project
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
package com.entertailion.android.slideshow.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.http.AndroidHttpClient;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;

import com.entertailion.android.slideshow.R;
import com.entertailion.android.slideshow.sites.SiteInfo;

/**
 * Utility class.
 * 
 * Some code from:
 * https://github.com/AnderWeb/android_packages_apps_Launcher/tree/froyo
 * 
 * @author leon_nicholls
 */
public class Utils {
	private static final String LOG_TAG = "Utils";

	private static final char[] ALPHABET = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z' };

	private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public static final String WEB_SITE_ICON_PREFIX = "web_site_icon_";

	/**
	 * Get the RSS feed for a web site.
	 * 
	 * @param url
	 * @param context
	 * @param refresh
	 * @return
	 */
	public static String getRssFeed(String url, Context context, boolean refresh) {
		String rss = Utils.getCachedData(context, url, refresh);
		if (rss != null) {
			rss = rss.trim();
			if (rss.startsWith(XML_PREFIX)) {
				return rss;
			} else {
				try {
					Document doc = Jsoup.parse(rss);
					Element link = doc.select("link[type=application/rss+xml]").first();
					if (link != null && link.attr("rel").equalsIgnoreCase("alternate")) {
						String href = link.attr("href");
						if (href != null) {
							rss = Utils.getCachedData(context, href, refresh);
							return rss;
						}
					}
				} catch (Exception e) {
					Log.e(LOG_TAG, "Jsoup exception", e);
				}
			}
		}
		return rss;
	}

	/**
	 * Clean a string so it can be used for a file name
	 * 
	 * @param value
	 * @return
	 */
	public static final String clean(String value) {
		return value.replaceAll(":", "_").replaceAll("/", "_").replaceAll("\\\\", "_").replaceAll("\\?", "_").replaceAll("#", "_");
	}

	/**
	 * Escape XML entities
	 * 
	 * @param aText
	 * @return
	 */
	public static final String escapeXML(String aText) {
		if (null == aText) {
			return "";
		}
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\'') {
				result.append("&#039;");
			} else if (character == '&') {
				result.append("&amp;");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Create a bitmap from a image URL.
	 * 
	 * @param src
	 * @return
	 */
	public static final Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			int size = Math.max(myBitmap.getWidth(), myBitmap.getHeight());
			Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			c.drawBitmap(myBitmap, (size - myBitmap.getWidth()) / 2, (size - myBitmap.getHeight()) / 2, paint);
			return b;
		} catch (Exception e) {
			Log.e(LOG_TAG, "Faild to get the image from URL:" + src, e);
			return null;
		}
	}

	/**
	 * Get the Android version of the app.
	 * 
	 * @param context
	 * @return
	 */
	public static final String getVersion(Context context) {
		String versionString = context.getString(R.string.unknown_build);
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionString = info.versionName;
		} catch (Exception e) {
			// do nothing
		}
		return versionString;
	}

	/**
	 * Log device info.
	 * 
	 * @param context
	 */
	public static final void logDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			Log.i(LOG_TAG, "Version=" + pi.versionName);
			Log.i(LOG_TAG, "IP Address=" + Utils.getLocalIpAddress());
			Log.i(LOG_TAG, "android.os.Build.VERSION.RELEASE=" + android.os.Build.VERSION.RELEASE);
			Log.i(LOG_TAG, "android.os.Build.VERSION.INCREMENTAL=" + android.os.Build.VERSION.INCREMENTAL);
			Log.i(LOG_TAG, "android.os.Build.DEVICE=" + android.os.Build.DEVICE);
			Log.i(LOG_TAG, "android.os.Build.MODEL=" + android.os.Build.MODEL);
			Log.i(LOG_TAG, "android.os.Build.PRODUCT=" + android.os.Build.PRODUCT);
			Log.i(LOG_TAG, "android.os.Build.MANUFACTURER=" + android.os.Build.MANUFACTURER);
			Log.i(LOG_TAG, "android.os.Build.BRAND=" + android.os.Build.BRAND);
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "logDeviceInfo", e);
		}
	}

	/**
	 * Get the IP address of the network interface.
	 * 
	 * @return
	 */
	public static final String getLocalIpAddress() {
		InetAddress inetAddress = Utils.getLocalInetAddress();
		if (inetAddress != null) {
			return inetAddress.getHostAddress().toString();
		}
		return null;
	}

	public static final InetAddress getLocalInetAddress() {
		InetAddress selectedInetAddress = null;
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.isUp()) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							if (inetAddress instanceof Inet4Address) { // only
																		// want
																		// ipv4
																		// address
								if (inetAddress.getHostAddress().toString().charAt(0) != '0') {
									if (selectedInetAddress == null) {
										selectedInetAddress = inetAddress;
									} else if (intf.getName().startsWith("eth")) { // prefer
																					// wired
																					// interface
										selectedInetAddress = inetAddress;
									}
								}
							}
						}
					}
				}
			}
			return selectedInetAddress;
		} catch (Throwable e) {
			Log.e(LOG_TAG, "Failed to get the IP address", e);
		}
		return null;
	}

	public static final boolean isUsa() {
		return Locale.getDefault().equals(Locale.US);
	}

	/**
	 * Get string version of HTML for a web page. Cache HTML for future acccess.
	 * 
	 * @param context
	 * @param url
	 * @param refresh
	 * @return
	 */
	public synchronized static final String getCachedData(Context context, String url, boolean refresh) {
		Log.d(LOG_TAG, "getCachedData: " + url);
		String data = null;
		boolean exists = false;
		String cleanUrl = "cache." + clean(url);
		File file = context.getFileStreamPath(cleanUrl);
		if (file != null && file.exists()) {
			exists = true;
		}

		if (!refresh && exists) {
			try {
				FileInputStream fis = context.openFileInput(cleanUrl);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				StringBuffer buffer = new StringBuffer();
				for (String line; (line = br.readLine()) != null;) {
					buffer.append(line);
				}
				fis.close();
				data = buffer.toString();
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error getData: " + url, e);
			}
		} else {
			boolean found = false;
			StringBuilder builder = new StringBuilder();
			try {
				InputStream stream = new HttpRequestHelper().getHttpStream(url);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				stream.close();
				found = true;
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error getData: " + url, e);
			} catch (Exception e) {
				Log.e(LOG_TAG, "stream is NULL");
			}
			data = builder.toString();
			if (!found && exists) {
				try {
					FileInputStream fis = context.openFileInput(cleanUrl);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					StringBuffer buffer = new StringBuffer();
					for (String line; (line = br.readLine()) != null;) {
						buffer.append(line);
					}
					fis.close();
					data = buffer.toString();
				} catch (Exception e) {
					Log.e(LOG_TAG, "Error getData: " + url, e);
				}
			}
			if (data != null && data.trim().length() > 0) {
				try {
					FileOutputStream fos = context.openFileOutput(cleanUrl, Context.MODE_PRIVATE);
					fos.write(data.getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					Log.e(LOG_TAG, "Error getData: " + url, e);
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error getData: " + url, e);
				}
			}
		}
		return data;
	}

	public static final char keyCodeToLetter(int keyCode) {
		return ALPHABET[keyCode - KeyEvent.KEYCODE_A];
	}

	/**
	 * Save a bitmap to a local file.
	 * 
	 * @param context
	 * @param bitmap
	 * @param targetWidth
	 * @param targetHeight
	 * @param fileName
	 * @throws IOException
	 */
	public static final void saveToFile(Context context, Bitmap bitmap, int targetWidth, int targetHeight, String fileName) throws IOException {
		FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		// FileOutputStream fos = new FileOutputStream(fileName);
		if (bitmap.getWidth() == targetWidth && bitmap.getHeight() == targetHeight) {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} else {
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
			scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
		}
		fos.close();
	}

	/**
	 * Determine if there is a high resolution icon available for the web site.
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public static final String getWebSiteIcon(Context context, String url) {
		String icon = null;
		if (url != null) {
			String data = Utils.getCachedData(context, url, true);
			if (data != null) {
				Document doc = Jsoup.parse(data);
				if (doc != null) {
					String href = null;
					Elements metas = doc.select("meta[itemprop=image]");
					if (metas.size() > 0) {
						Element meta = metas.first();
						href = meta.attr("abs:content");
						// weird jsoup bug: abs doesn't always work
						if (href == null || href.trim().length() == 0) {
							href = url + meta.attr("content");
						}
					}
					if (href == null || href.trim().length() == 0) {
						// Find the Microsoft tile icon
						metas = doc.select("meta[name=msapplication-TileImage]");
						if (metas.size() > 0) {
							Element meta = metas.first();
							href = meta.attr("abs:content");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + meta.attr("content");
							}
						}
					}
					if (href == null || href.trim().length() == 0) {
						// Find the Apple touch icon
						Elements links = doc.select("link[rel=apple-touch-icon]");
						if (links.size() > 0) {
							Element link = links.first();
							href = link.attr("abs:href");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + link.attr("href");
							}
						}
					}
					if (href == null || href.trim().length() == 0) {
						// Find the Facebook open graph icon
						metas = doc.select("meta[property=og:image]");
						if (metas.size() > 0) {
							Element link = metas.first();
							href = link.attr("abs:content");
							// weird jsoup bug: abs doesn't always work
							if (href == null || href.trim().length() == 0) {
								href = url + link.attr("content");
							}
						}
					}
					if (href != null && href.trim().length() > 0) {
						try {
							Bitmap bitmap = Utils.getBitmapFromURL(href);
							if (bitmap != null) {
								icon = "web_site_icon_" + Utils.clean(href) + ".png";
								Utils.saveToFile(context, bitmap, bitmap.getWidth(), bitmap.getHeight(), icon);
								bitmap.recycle();
							}
						} catch (Exception e) {
							Log.d(LOG_TAG, "getWebSiteIcon", e);
						}
					}
				}
			}
		}
		return icon;
	}

	public static final String stripBrackets(String n) {
		if (n.endsWith(")")) { // remove ()
			int index = n.lastIndexOf("(");
			if (index != -1) {
				n = n.substring(0, index - 1).trim();
			}
		}
		return n;
	}

	public static final String readAssetFile(Context context, String filename) {
		StringBuffer buffer = new StringBuffer();
		try {
			InputStream is = context.getAssets().open(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line).append('\n');
			}
			is.close();
		} catch (Throwable x) {
		}
		return buffer.toString();
	}

	public static Document getJsoupDocument(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(3000).get();
		} catch (IOException e) {
			Log.e(LOG_TAG, "getJsoupDocument", e);
		}
		return doc;
	}

	/**
	 * Prevent the screensave from activating on Google TV devices.
	 * 
	 * @param context
	 */
	public static void wakeUp(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "tag");
		wl.acquire();
		wl.release();
	}

	/**
	 * Load the list of featured photo web sites from embedded XML file.
	 * 
	 * @see res/xml/sites.xml
	 * 
	 * @param context
	 * @return
	 */
	public static ArrayList<SiteInfo> getSites(Context context) {
		ArrayList<SiteInfo> sites = new ArrayList<SiteInfo>();
		XmlResourceParser parser = null;
		try {
			Resources r = context.getResources();
			int resourceId = r.getIdentifier("sites", "xml", "com.entertailion.android.slideshow");
			if (resourceId != 0) {
				parser = context.getResources().getXml(resourceId); // R.xml.sites
				parser.next();
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						switch (eventType) {
						case XmlPullParser.START_DOCUMENT:
							break;
						case XmlPullParser.START_TAG:
							String tagName = parser.getName();
							if (tagName.equalsIgnoreCase("site")) {
								String name = parser.getAttributeValue(null, "name");
								String url = parser.getAttributeValue(null, "url");
								sites.add(new SiteInfo(name, url));
							}
							break;
						}
					}
					eventType = parser.next();
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "getSites", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
		return sites;
	}

	/**
	 * Check if a URL is valid and points to a resource on the remote web
	 * server. Used to check if certain image sizes are available.
	 * 
	 * @param url
	 * @return
	 */
	public static boolean checkUrl(String url) {
		Log.d(LOG_TAG, "checkUrl: " + url);
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = client.execute(request);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				return true;
			}
		} catch (Exception e) {
			request.abort();
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return false;
	}

}

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
package com.entertailion.android.slideshow;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.entertailion.android.slideshow.utils.Analytics;

/**
 * Handle settings for app. Invoked by the user from the menu.
 * 
 * @author leon_nicholls
 * 
 */
public class PreferencesActivity extends PreferenceActivity {
	private static final String LOG_TAG = "PreferencesActivity";
	public static final String GENERAL_PHOTO_DELAY = "general.photo_delay";

	public static final String PHOTO_DELAY_TWO = "2";
	public static final String PHOTO_DELAY_THREE = "3";
	public static final String PHOTO_DELAY_FOUR = "4";
	public static final String PHOTO_DELAY_FIVE = "5";
	public static final String PHOTO_DELAY_SIX = "6";
	public static final String PHOTO_DELAY_SEVEN = "7";
	public static final String PHOTO_DELAY_EIGHT = "8";
	public static final String PHOTO_DELAY_NINE = "9";
	public static final String PHOTO_DELAY_TEN = "10";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// General
		Preference preference = (Preference) findPreference(GENERAL_PHOTO_DELAY);
		String rows = preference.getSharedPreferences().getString(GENERAL_PHOTO_DELAY, PHOTO_DELAY_FIVE);
		if (rows.equals(PHOTO_DELAY_TWO)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_two));
		} else if (rows.equals(PHOTO_DELAY_THREE)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_three));
		} else if (rows.equals(PHOTO_DELAY_FOUR)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_four));
		} else if (rows.equals(PHOTO_DELAY_FIVE)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_five));
		} else if (rows.equals(PHOTO_DELAY_SIX)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_six));
		} else if (rows.equals(PHOTO_DELAY_SEVEN)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_seven));
		} else if (rows.equals(PHOTO_DELAY_EIGHT)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_eight));
		} else if (rows.equals(PHOTO_DELAY_NINE)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_nine));
		} else if (rows.equals(PHOTO_DELAY_TEN)) {
			preference.setSummary(getString(R.string.preferences_general_photo_delay_ten));
		}
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.equals(PHOTO_DELAY_TWO)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_two));
					Analytics.logEvent(Analytics.PHOTO_DELAY_TWO);
				} else if (newValue.equals(PHOTO_DELAY_THREE)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_three));
					Analytics.logEvent(Analytics.PHOTO_DELAY_THREE);
				} else if (newValue.equals(PHOTO_DELAY_FOUR)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_four));
					Analytics.logEvent(Analytics.PHOTO_DELAY_FOUR);
				} else if (newValue.equals(PHOTO_DELAY_FIVE)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_five));
					Analytics.logEvent(Analytics.PHOTO_DELAY_FIVE);
				} else if (newValue.equals(PHOTO_DELAY_SIX)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_six));
					Analytics.logEvent(Analytics.PHOTO_DELAY_SIX);
				} else if (newValue.equals(PHOTO_DELAY_SEVEN)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_seven));
					Analytics.logEvent(Analytics.PHOTO_DELAY_SEVEN);
				} else if (newValue.equals(PHOTO_DELAY_EIGHT)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_eight));
					Analytics.logEvent(Analytics.PHOTO_DELAY_EIGHT);
				} else if (newValue.equals(PHOTO_DELAY_NINE)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_nine));
					Analytics.logEvent(Analytics.PHOTO_DELAY_NINE);
				} else if (newValue.equals(PHOTO_DELAY_TEN)) {
					preference.setSummary(getString(R.string.preferences_general_photo_delay_ten));
					Analytics.logEvent(Analytics.PHOTO_DELAY_TEN);
				}
				return true;
			}

		});

		Analytics.createAnalytics(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Start Google Analytics for this activity
		Analytics.startAnalytics(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Stop Google Analytics for this activity
		Analytics.stopAnalytics(this);
	}
}
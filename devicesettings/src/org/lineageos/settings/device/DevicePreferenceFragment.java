/*
 * Copyright (C) 2020 The LineageOS Project
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

package org.lineageos.settings.device;

import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.widget.Toast;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

public class DevicePreferenceFragment extends PreferenceFragment {
    private static final String OVERLAY_NO_FILL_PACKAGE = "org.lineageos.overlay.notch.nofill";

    private static final String KEY_MIN_REFRESH_RATE = "pref_min_refresh_rate";
    private static final String KEY_PILL_STYLE_NOTCH = "pref_pill_style_notch";

    private IOverlayManager mOverlayService;

    private ListPreference mPrefMinRefreshRate;
    private SwitchPreference mPrefPillStyleNotch;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        mOverlayService = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.device_prefs);
        mPrefMinRefreshRate = (ListPreference) findPreference(KEY_MIN_REFRESH_RATE);
        mPrefMinRefreshRate.setOnPreferenceChangeListener(PrefListener);
        mPrefPillStyleNotch = (SwitchPreference) findPreference(KEY_PILL_STYLE_NOTCH);
        mPrefPillStyleNotch.setOnPreferenceChangeListener(PrefListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateValuesAndSummaries();
    }

    private void updateValuesAndSummaries() {
        final float refreshRate = Settings.System.getFloat(getContext().getContentResolver(),
            Settings.System.MIN_REFRESH_RATE, 120.0f);
        mPrefMinRefreshRate.setValue(((int) refreshRate) + " Hz");
        mPrefMinRefreshRate.setSummary(mPrefMinRefreshRate.getValue());

        try {
            mPrefPillStyleNotch.setChecked(
                !mOverlayService.getOverlayInfo(OVERLAY_NO_FILL_PACKAGE, 0).isEnabled());
        } catch (RemoteException e) {
            // We can do nothing
        }
    }

    private Preference.OnPreferenceChangeListener PrefListener =
        new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                final String key = preference.getKey();

                if (KEY_MIN_REFRESH_RATE.equals(key)) {
                    Settings.System.putFloat(getContext().getContentResolver(),
                        Settings.System.MIN_REFRESH_RATE,
                        (float) Integer.parseInt((String) value));
                } else if (KEY_PILL_STYLE_NOTCH.equals(key)) {
                    try {
                        mOverlayService.setEnabled(
                            OVERLAY_NO_FILL_PACKAGE, !(boolean) value, 0);
                    } catch (RemoteException e) {
                        // We can do nothing
                    }
                    Toast.makeText(getContext(),
                        R.string.msg_device_need_restart, Toast.LENGTH_SHORT).show();
                }

                updateValuesAndSummaries();
                return true;
            }
        };
}
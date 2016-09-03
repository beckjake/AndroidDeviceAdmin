package io.ebeck.jacob.deviceadmin;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;



/**
 * A {@link PreferenceActivity} that presents a set of application settings. It's just a list of
 * settings.
 */
public class DeviceAdminSettings extends PreferenceActivity {
    private static final String TAG = "DeviceAdminSettings";

    // we need to keep an instance of the device policy manager, of course.
    DevicePolicyManager policyManager;
    // and the name of our receiver
    ComponentName receiverName;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get an instance of the policy manager from getSystemService.
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // and build our receiver's name
        receiverName = new ComponentName(this, DeviceAdminSettingsReceiver.class);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
//                || PreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener {
        // it's nice to have a handle to the outer class
        private DeviceAdminSettings settingsActivity;
        // The preference fragment also needs the policy manager and name
        private DevicePolicyManager policyManager;
        private ComponentName receiverName;

        // we need to know if we're active for some stuff
        private boolean isActiveAdmin;

        // we want a listener on our button
        private EditTextPreference maxFailuresBeforeErase;
        private CheckBoxPreference deviceAdminEnabled;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Log.d(TAG, "In onActivityCreated");
            settingsActivity = (DeviceAdminSettings) getActivity();
            policyManager = settingsActivity.policyManager;
            receiverName = settingsActivity.receiverName;
            isActiveAdmin = policyManager.isAdminActive(receiverName);
            updateSummary();
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "In onResume");
            isActiveAdmin = policyManager.isAdminActive(receiverName);
            updateSummary();
        }

        /**
         * updates isActiveAdmin, sets up summaries
         */
        private void updateSummary() {
            Log.d(TAG, String.format("Active admin=%b", isActiveAdmin));
            Integer current = isActiveAdmin ? getMaxFailuresBeforeErase() : 0;
            maxFailuresBeforeErase.setDefaultValue(current);
            maxFailuresBeforeErase.setSummary(String.format(
                    "%s, currently %s (set 0 for unlimited)",
                    getResources().getString(R.string.pref_summary_erase_after_attempts),
                    current == 0 ? "unlimited" : String.valueOf(current)
            ));
            deviceAdminEnabled.setChecked(isActiveAdmin);
            maxFailuresBeforeErase.setEnabled(isActiveAdmin);
        }

        private Integer getMaxFailuresBeforeErase() {
            Integer i = policyManager.getMaximumFailedPasswordsForWipe(receiverName);
            Log.d(TAG, String.format("Current max failed passwords for wipe=%d", i));
            return i;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "In onCreate");
            addPreferencesFromResource(R.xml.pref_general);
            maxFailuresBeforeErase = (EditTextPreference) findPreference("erase_after_attempts");
            maxFailuresBeforeErase.setOnPreferenceChangeListener(this);
            deviceAdminEnabled = (CheckBoxPreference) findPreference("device_admin_enabled");
            deviceAdminEnabled.setOnPreferenceChangeListener(this);
            setHasOptionsMenu(true);
        }

        /**
         * Settings don't apply until the onPreferenceChange method is exited, it's called *before*
         * anything is set and persisted. Then the IDE started whining about an NPE and fine
         * whatever I'll make it a method. This doesn't seem to always work, either, so I also fudge
         * it some places. \_()_/
         */
        protected void updateSummaryNext() {
            try {
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        updateSummary();
                    }
                });
            } catch (NullPointerException e) {
                Log.w(TAG, String.format("Got NPE in updateSummaryNext(): %s", e));
            }
        }

        private boolean onDeviceAdminEnabledChange(Object newValue) {
            boolean b = (boolean) newValue;
            if (b != isActiveAdmin) {
                if (b) {
                    Intent plsadd = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    plsadd.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, receiverName);
                    plsadd.putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            R.string.device_admin_add_description
                    );
                    startActivity(plsadd);
                    // don't change the checkbox. When the admin intent brings us up we'll do
                    // our onResume thing and update then.
                    return false;
                } else {
                    policyManager.removeActiveAdmin(receiverName);
                    // isActiveAdmin() will still say true at this point.
                    isActiveAdmin = false;
                }
            }
            return true;
        }

        private void onMaxFailuresBeforeEraseChange(Object newValue) {
            int i = 0;
            try {
                i = Integer.parseInt((String)newValue);
            } catch (NumberFormatException e) {
                Toast.makeText(settingsActivity, "Error - invalid entry", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, String.format("Setting maximum failed passwords for erase to %d", i));
            policyManager.setMaximumFailedPasswordsForWipe(receiverName, i);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == deviceAdminEnabled) {
                if (!onDeviceAdminEnabledChange(newValue)) {
                    return false;
                }
            } else if (preference == maxFailuresBeforeErase) {
                onMaxFailuresBeforeEraseChange(newValue);
            }
            updateSummaryNext();
            return true;
        }
    }

    /**
     * Here's the device admin settings receiver.
     * It's required to implement this, but not clear if we need to implement anything.
     */
    public static class DeviceAdminSettingsReceiver extends DeviceAdminReceiver {

    }

}

package devprodroid.orahudserver;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Settings Screen and fragment manager class
 */
public class SettingsActivity extends PreferenceActivity   {


    public static final String KEY_PREF_DEBUG = "pref_debug";
    public static final String KEY_PREF_COMPAT = "pref_compat";

    public static final String KEY_PREF_OUTDOOR_MODE = "pref_outdoor";
    public static final String KEY_PREF_MAGNETO_MODE = "pref_magneto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


}
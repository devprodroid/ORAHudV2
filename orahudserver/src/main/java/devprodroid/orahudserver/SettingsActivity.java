package devprodroid.orahudserver;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by robert on 17.08.15.
 */
public class SettingsActivity extends PreferenceActivity   {


    public static final String KEY_PREF_DEBUG = "pref_debug";
    public static final String KEY_PREF_OUTDOOR_MODE = "pref_outdoor";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


}
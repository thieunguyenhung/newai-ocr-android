package vn.newai.ocr;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import vn.newai.ocr.utility.LocalStorage;

public class SettingActivity extends AppCompatActivity {
    private Toolbar settingToolbar; //custom toolbar

    private static String userEmail, langOCR;//user email and language preferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        /*-Get user setting from Shared Preference*/
        userEmail = LocalStorage.getFromLocal(this, LocalStorage.KEY_USER_EMAIL);
        langOCR = LocalStorage.getFromLocal(this, LocalStorage.KEY_OCR_LANG);

        addControls();
        addEvents();
        getFragmentManager().beginTransaction().replace(R.id.settingFragmentContainer, new SettingFragment()).commit();
    }

    private void addControls() {
        /*Toolbar*/
        settingToolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        settingToolbar.setNavigationIcon(R.drawable.ic_action_nav_back);
        setSupportActionBar(settingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.activity_setting));
        }

        /*-CoordinatorLayout container*/
        CoordinatorLayout coordinatorLayoutContainer = (CoordinatorLayout) findViewById(R.id.settingCoordinatorLayout);
        if (userEmail.isEmpty() || langOCR.isEmpty()) {
            Snackbar.make(coordinatorLayoutContainer, getString(R.string.guide_user_email), Snackbar.LENGTH_LONG).show();
        }
    }

    private void addEvents() {
        settingToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.finish();
            }
        });
    }

    public static class SettingFragment extends PreferenceFragment {
        private EditTextPreference settingTxtEmail; /*-Preference text email*/
        private ListPreference settingListLang; /*-Preference list languages*/
        private ArrayList<String> listLangLables, listLangValues; /*-List language labels and values*/

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            this.setupListLang();
            this.addControls();
            this.addEvents();
        }

        private void setupListLang() {
            /*-Initialize list language labels*/
            this.listLangLables = new ArrayList<>();
            listLangLables.add(getString(R.string.lang_vie));
            listLangLables.add(getString(R.string.lang_eng));

            /*-Initialize list language values*/
            this.listLangValues = new ArrayList<>();
            this.listLangValues.add(getString(R.string.lang_value_vie));
            this.listLangValues.add(getString(R.string.lang_value_eng));
        }

        private void addControls() {
            /*-Preference text email*/
            settingTxtEmail = (EditTextPreference) getPreferenceScreen().findPreference("settingTxtEmail");
            if (null != userEmail && !userEmail.isEmpty()) {
                settingTxtEmail.setText(userEmail);
                settingTxtEmail.setSummary(userEmail);
            } else {
                settingTxtEmail.setSummary(getString(R.string.preference_email_summary));
            }

            /*-Preference list languages*/
            settingListLang = (ListPreference) getPreferenceScreen().findPreference("settingListLang");
            settingListLang.setEntries(this.listLangLables.toArray(new CharSequence[this.listLangLables.size()]));
            settingListLang.setEntryValues(this.listLangValues.toArray(new CharSequence[this.listLangValues.size()]));
            if (null != langOCR && !langOCR.isEmpty()) {
                settingListLang.setValueIndex(this.listLangValues.indexOf(langOCR));
                settingListLang.setSummary(settingListLang.getEntry().toString());
            } else {
                settingListLang.setValueIndex(0);
                settingListLang.setSummary(getString(R.string.preference_lang_summary));
            }
        }

        private void addEvents() {
            settingTxtEmail.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue instanceof String) {
                        String userEmail = (String) newValue;
                        if (!userEmail.isEmpty()) {
                            //Log.d("userEmail", LocalStorage.getFromLocal(SettingFragment.this.getActivity(), LocalStorage.KEY_USER_EMAIL));
                            LocalStorage.saveToLocal(SettingFragment.this.getActivity(), LocalStorage.KEY_USER_EMAIL, userEmail);
                            settingTxtEmail.setText(userEmail);
                            settingTxtEmail.setSummary(userEmail);
                        }
                    }
                    return false;
                }
            });

            settingListLang.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue instanceof String) {
                        String langOCR = (String) newValue;
                        if (!langOCR.isEmpty()) {
                            //Log.d("lang", langOCR);
                            LocalStorage.saveToLocal(SettingFragment.this.getActivity(), LocalStorage.KEY_OCR_LANG, langOCR);
                            settingListLang.setValueIndex(SettingFragment.this.listLangValues.indexOf(langOCR));
                            settingListLang.setSummary(settingListLang.getEntry().toString());
                        }
                    }
                    return false;
                }
            });
        }
    }
}

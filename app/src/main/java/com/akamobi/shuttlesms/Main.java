package com.akamobi.shuttlesms;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;


public class Main extends PreferenceActivity {
	//private static final String TAG = "Main";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
 
        /*
         * ShuttleSMS Enable?
         */
        CheckBoxPreference toggleShuttle = new CheckBoxPreference(this);
        toggleShuttle.setKey("enable_shuttle");
        toggleShuttle.setTitle("Enable Shuttle SMS");
        toggleShuttle.setSummary("Enable/Disable Shuttle SMS");
        root.addPreference(toggleShuttle);                

        /*
         * Seperator
         */
        PreferenceCategory launchPrefCat = new PreferenceCategory(this);
        launchPrefCat.setTitle("Setting");
        root.addPreference(launchPrefCat);

        /*
         * Credit Enable?
         */

        CheckBoxPreference toggleCredit = new CheckBoxPreference(this);
        toggleCredit.setKey("enable_credit");
        toggleCredit.setTitle("Enable Credit List");
        toggleCredit.setSummary("Enable/Disable Shuttle SMS");
        launchPrefCat.addPreference(toggleCredit);                

        Intent i;
        
        /*
         * Credit List
         */
        PreferenceScreen creditPref = getPreferenceManager().createPreferenceScreen(this);

        i = new Intent(this, CreditList.class);
        creditPref.setIntent(i);
        
        creditPref.setTitle("Credit List");
        creditPref.setSummary("Black/White List of Number/Keyword");
        launchPrefCat.addPreference(creditPref);
        //TODO: creditPref.setDependency("enable_credit");
        
		/*
		 * Log 
		 */
        PreferenceScreen logPref = getPreferenceManager().createPreferenceScreen(this);     
        i = new Intent(this, LogList.class);
        logPref.setIntent(i);
        
        logPref.setTitle("Log");
        logPref.setSummary("Challenge-Response Log");
        launchPrefCat.addPreference(logPref);

        /*
         * Help 
         */

        PreferenceScreen helpPref = getPreferenceManager().createPreferenceScreen(this);
        
        i = new Intent(this, Help.class);
        helpPref.setIntent(i);

        helpPref.setTitle("Help");
        helpPref.setSummary("FAQ/Support/About");
        launchPrefCat.addPreference(helpPref);

        return root;
    }
}

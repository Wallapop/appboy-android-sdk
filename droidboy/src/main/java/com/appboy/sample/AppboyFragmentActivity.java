package com.appboy.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.appboy.support.AppboyLogger;

import io.branch.referral.Branch;

/*
 * Appboy integration sample
 *
 * To start tracking analytics using the Appboy Android SDK, in all activities, you must call Appboy.openSession()
 * and Appboy.closeSession() in the activity's onStart() and onStop() respectively. You can see that in this
 * activity (inherited by most other activities) and com.appboy.sample.PreferencesActivity.
 */
public class AppboyFragmentActivity extends AppCompatActivity {
  protected static final String TAG = AppboyLogger.getAppboyLogTag(AppboyFragmentActivity.class);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    Branch.getInstance(getApplicationContext()).initSession();
  }

  @Override
  public void onStop() {
    super.onStop();
    Branch.getInstance(getApplicationContext()).closeSession();
  }
}

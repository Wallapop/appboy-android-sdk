package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.appboy.support.AppboyFileUtils;
import com.appboy.ui.AppboyWebViewActivity;
import com.appboy.ui.R;

import java.util.Arrays;
import java.util.List;

/**
 * Action that launches the AppboyWebViewActivity to a particular URL.
 */
public final class WebAction implements IAction {
  private final String mTargetUrl;
  private final Bundle mExtras;

  public WebAction(String targetUrl) {
    this(targetUrl, null);
  }

  public WebAction(String targetUrl, Bundle extras) {
    mTargetUrl = targetUrl;
    mExtras = extras;
  }

  public void execute(Context context) {
    Intent intent = new Intent(context, AppboyWebViewActivity.class);
    if (mExtras != null) {
      intent.putExtras(mExtras);
    }
    intent.putExtra(AppboyWebViewActivity.URL_EXTRA, mTargetUrl);
    context.startActivity(intent);
  }

  public static List<String> getSupportedSchemes(Context context) {
    List<String> remoteSchemes = AppboyFileUtils.REMOTE_SCHEMES;
    String[] moreSupportedSchemes = context.getResources().getStringArray(R.array.supported_schemes);
    remoteSchemes.addAll(Arrays.asList(moreSupportedSchemes));
    return remoteSchemes;
  }

  public static String[] getUnsupportedAuthority(Context context) {
    return context.getResources().getStringArray(R.array.unsupported_authorities);
  }
}

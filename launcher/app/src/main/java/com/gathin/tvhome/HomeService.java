package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * When another home screen (the operator's) comes to the front, bring this launcher back.
 * Needs no adb: the user enables it once under Settings → Accessibility.
 */
public class HomeService extends AccessibilityService {

    private long lastLaunch;
    private Set<String> homePkgs;
    private long homesAt;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null || pkg.toString().equals(getPackageName())) return;

        long now0 = System.currentTimeMillis();
        if (homePkgs == null || now0 - homesAt > 60_000L) {   // refresh the list of home apps at most once a minute
            PackageManager pm = getPackageManager();
            List<ResolveInfo> homes = pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
            homePkgs = new HashSet<>();
            for (ResolveInfo ri : homes) homePkgs.add(ri.activityInfo.packageName);
            homesAt = now0;
        }
        if (!homePkgs.contains(pkg.toString())) return;

        long now = System.currentTimeMillis();
        if (now - lastLaunch < 800) return;   // avoid fighting a launcher that reopens itself
        lastLaunch = now;
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onInterrupt() {
    }
}

package no.kij.goodbyebixby;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by kissy on 16.04.2017.
 */

public class GoodbyeBixbyService extends AccessibilityService {
    // Bixby = 1082
    // V = 50
    // no.kij.goodbyebixby/.GoodbyeBixbyService

    private final String TAG = "GoodbyeBixbyService";
    private final int BIXBY_KEY_CODE = 1082;
    private SharedPreferences sharedPreferences;
    private int LONG_PRESS_INTERVAL;
    private int DOUBLE_CLICK_INTERVAL;
    private boolean longPressTrigger = false;
    private boolean doubleClickTrigger = false;
    private long buttonEventTime;
    private Handler handler = new Handler();
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (longPressTrigger) {
                longPressTrigger = false;
                boolean hapticFeedback = sharedPreferences.getBoolean("haptic_feedback", false);
                String actionName = sharedPreferences.getString("action_long", "None");
                String pkgName = sharedPreferences.getString("package_name_long", null);
                if (!actionName.equals("None")) {
                    if (hapticFeedback) {
                        Vibrator vibe = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibe.vibrate(50);
                    }
                    runAction(actionName, pkgName);
                }
            }
        }
    };

    private Runnable doubleClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (doubleClickTrigger) {
                doubleClickTrigger = false;
                String actionName = sharedPreferences.getString("action_single", "None");
                String pkgName = sharedPreferences.getString("package_name_single", null);
                if (!actionName.equals("None")) {
                    runAction(actionName, pkgName);
                }
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("AccessibilityEvent", event.toString());
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
        Log.i(TAG, "onServiceConnected: Service Connected");
        AccessibilityServiceInfo info = getServiceInfo();
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_key), MODE_PRIVATE);
        Helper.context = getApplicationContext();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        LONG_PRESS_INTERVAL = 200 + (sharedPreferences.getInt("long_press_interval", 3) * 100);
        DOUBLE_CLICK_INTERVAL = 100 + (sharedPreferences.getInt("double_click_interval", 4) * 50);

        if (keyCode == BIXBY_KEY_CODE) {

            if (action == KeyEvent.ACTION_DOWN) {
                longPressTrigger = true;
                if (!doubleClickTrigger) {
                    handler.postDelayed(longPressRunnable, LONG_PRESS_INTERVAL);
                }
            } else if (action == KeyEvent.ACTION_UP) {
                handler.removeCallbacks(longPressRunnable);
                long time = event.getEventTime();
                if (longPressTrigger) {
                    if (doubleClickTrigger && (time - buttonEventTime) < DOUBLE_CLICK_INTERVAL) {
                        doubleClickTrigger = false;
                        handler.removeCallbacks(doubleClickRunnable);
                        String actionName = sharedPreferences.getString("action_double", "None");
                        String pkgName = sharedPreferences.getString("package_name_double", null);
                        if (!actionName.equals("None")) {
                            runAction(actionName, pkgName);
                        }
                    } else {
                        doubleClickTrigger = true;
                        handler.postDelayed(doubleClickRunnable, DOUBLE_CLICK_INTERVAL);
                    }
                }
                buttonEventTime = time;
                longPressTrigger = false;
            }
            return true;
        }
        return false;
    }

    private void runAction(String action, String packageName) {
        if (packageName != null && !(packageName.equals(""))) {
            runPkg(action, packageName);
        } else {
            runSystemAction(action);
        }
    }

    private void runPkg(String action, String packageName) {
        int flag = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            intent.setFlags(flag);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, action + " not found!", Toast.LENGTH_SHORT).show();
        }
    }
    private void runSystemAction(String action) {
        Helper.context = getApplicationContext();
        Log.i(TAG, "runAction: " + action);
        switch (action) {
            case "None":
                Log.i(TAG, "runAction: Not Set!");
                break;
            case "Home":
                performGlobalAction(GLOBAL_ACTION_HOME);
                break;
            case "Back":
                performGlobalAction(GLOBAL_ACTION_BACK);
                break;
            case "Recent Apps":
                performGlobalAction(GLOBAL_ACTION_RECENTS);
                break;
            case "Google Now Cards":
                Helper.googleNow();
                break;
            case "Google Now Voice":
                Helper.googleVoice();
                break;
            case "Notifications":
                performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
                break;
            case "Quick Settings":
                performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
                break;
            case "Split Screen":
                performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
                break;
            case "Power Menu":
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
                break;
            case "Toggle Flash":
                Helper.toggleFlash();
                break;
            case "Camera":
                Helper.camera();
                break;
        }
    }
}

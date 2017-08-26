package no.kij.goodbyebixby;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    private LinearLayout singleClick;
    private LinearLayout doubleClick;
    private LinearLayout longPress;
    private TextView singleClickAssignedTo;
    private TextView doubleClickAssignedTo;
    private TextView longPressAssignedTo;
    private TextView longPressIntervalText;
    private TextView doubleClickIntervalText;
    private SeekBar longPressIntervalSeekBar;
    private SeekBar doubleClickIntervalSeekBar;
    private CheckBox hapticFeedBackCheckBox;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getString(R.string.preference_key), MODE_PRIVATE);
        runUpdates();
        initialize();
        runIntro();
    }

    @Override
    protected void onResume() {
        isServiceRunning();
        super.onResume();
    }

    private void isServiceRunning() {
        if (!sharedPreferences.getBoolean("firstRun", true)) {
            if (!Helper.isAccessibilitySettingsOn(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(this, "Please enable GoodbyeBixby!", Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     * Listens to the activity changers and creates menu and submenus.
     */
    private void startActivityMenuListeners() {
        singleClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateMenu(singleClick, "single", singleClickAssignedTo);
            }
        });

        doubleClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateMenu(doubleClick, "double", doubleClickAssignedTo);
            }
        });

        longPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateMenu(longPress, "long", longPressAssignedTo);
            }
        });

        hapticFeedBackCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("haptic_feedback", isChecked);
                editor.apply();
            }
        });
    }

    private void generateMenu(LinearLayout btn, final String action, final TextView assignedTo) {

        PopupMenu popupMenu = new PopupMenu(MainActivity.this, btn);
        popupMenu.getMenuInflater().inflate(R.menu.app_menu, popupMenu.getMenu());
        Menu menu = popupMenu.getMenu();
        menu.add(0, 100, Menu.NONE, "None");
        SubMenu subMenu = menu.getItem(0).getSubMenu();

        subMenu.add(R.id.system_actions, 101, Menu.NONE, R.string.home);
        subMenu.add(R.id.system_actions, 102, Menu.NONE, R.string.back);
        subMenu.add(R.id.system_actions, 103, Menu.NONE, R.string.recent_app);
        subMenu.add(R.id.system_actions, 104, Menu.NONE, R.string.google_now);
        subMenu.add(R.id.system_actions, 104, Menu.NONE, R.string.google_now_voice);
        subMenu.add(R.id.system_actions, 105, Menu.NONE, R.string.notifications);
        subMenu.add(R.id.system_actions, 106, Menu.NONE, R.string.quick_settings);
        subMenu.add(R.id.system_actions, 107, Menu.NONE, R.string.split_screen);
        subMenu.add(R.id.system_actions, 108, Menu.NONE, R.string.power_menu);
        subMenu.add(R.id.system_actions, 109, Menu.NONE, R.string.flash);
        subMenu.add(R.id.system_actions, 110, Menu.NONE, R.string.camera);

        SubMenu appMenu = menu.getItem(1).getSubMenu();

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> appList = getPackageManager().queryIntentActivities(intent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(getPackageManager()));

        int appId = 800;
        for (ResolveInfo info : appList) {
            appMenu.add(R.id.apps, appId, Menu.NONE, info.loadLabel(getPackageManager()));
            appId++;
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() < 100) {
                    return false;
                } else if (item.getItemId() < 200) {
                    String label = updateAssignedAction(item.getTitle().toString(), null, "system", action);
                    assignedTo.setText(label);
                } else if (item.getItemId() >= 800) {
                    ActivityInfo activityInfo = appList.get(item.getItemId() - 800).activityInfo;
                    String label = updateAssignedAction(item.getTitle().toString(), activityInfo, "application", action);
                    assignedTo.setText(label);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private String updateAssignedAction(String action, ActivityInfo activityInfo, String type, String key) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        String label = "";
        if (activityInfo != null) {
            String packageName = activityInfo.applicationInfo.packageName;
            String name = activityInfo.name;
            label = activityInfo.loadLabel(getPackageManager()).toString();
            edit.putString("action_" + key, action);
            edit.putString("activity_name_" + key, name);
            edit.putString("package_name_" + key, packageName);
            edit.putString("type_" + key, type);
        } else {
            edit.putString("action_" + key, action);
            edit.putString("type_" + key, type);
            edit.putString("package_name_" + key, null);
            edit.putString("activity_name_" + key, null);
            label = action;
        }
        edit.apply();
        return label;
    }

    /**
     * Listens to SeekBar changes and updates them accordingly in the preferences.
     */
    private void startSeekBarChangeListeners() {
        longPressIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            int seekProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "onProgressChanged: " + progress);
                switch (progress) {
                    case 0:
                        this.progress = 200;
                        seekProgress = progress;
                        break;
                    default:
                        this.progress = (200) + (100 * progress);
                        seekProgress = progress;
                        break;
                }
                String temp = this.progress + " ms";
                longPressIntervalText.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putInt("long_press_interval", seekProgress);
                e.apply();
            }
        });

        doubleClickIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            int seekProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "onProgressChanged: " + progress);
                switch (progress) {
                    case 0:
                        this.progress = 100;
                        seekProgress = progress;
                        break;
                    default:
                        this.progress = (100) + (50 * progress);
                        seekProgress = progress;
                        break;
                }
                String temp = this.progress + " ms";
                doubleClickIntervalText.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putInt("double_click_interval", seekProgress);
                e.apply();
            }
        });
    }

    private void initialize() {
        getSupportActionBar().setTitle(Html.fromHtml("Goodbye<b>Bixby</b>", 0));
        singleClick = (LinearLayout) findViewById(R.id.singleClick);
        doubleClick = (LinearLayout) findViewById(R.id.doubleClick);
        longPress = (LinearLayout) findViewById(R.id.longPress);
        singleClickAssignedTo = (TextView) findViewById(R.id.singleClickAssignedTo);
        doubleClickAssignedTo = (TextView) findViewById(R.id.doubleClickAssignedTo);
        longPressAssignedTo = (TextView) findViewById(R.id.longPressAssignedTo);
        doubleClickIntervalText = (TextView) findViewById(R.id.doubleClickIntervalMsText);
        longPressIntervalText = (TextView) findViewById(R.id.longPressIntervalMsText);
        longPressIntervalSeekBar = (SeekBar) findViewById(R.id.longPressIntervalSeekBar);
        doubleClickIntervalSeekBar = (SeekBar) findViewById(R.id.doubleClickIntervalSeekBar);
        hapticFeedBackCheckBox = (CheckBox) findViewById(R.id.hapticFeedbackCheckbox);

        singleClickAssignedTo.setText(sharedPreferences.getString("action_single", getString(R.string.none)));
        doubleClickAssignedTo.setText(sharedPreferences.getString("action_double", getString(R.string.none)));
        longPressAssignedTo.setText(sharedPreferences.getString("action_long", getString(R.string.none)));

        // TODO Add SeekBar to customize vibration length
        boolean hapticEnabled = sharedPreferences.getBoolean("haptic_feedback", false);
        hapticFeedBackCheckBox.setChecked(hapticEnabled);

        longPressIntervalSeekBar.setMax(4);
        doubleClickIntervalSeekBar.setMax(5);
        // 300ms
        longPressIntervalSeekBar.setProgress(sharedPreferences.getInt("long_press_interval", 3));
        //100ms
        doubleClickIntervalSeekBar.setProgress(sharedPreferences.getInt("double_click_interval", 4));

        String temp = "N/A ms";
        if (longPressIntervalSeekBar.getProgress() == 0) {
            temp = "200 ms";
        } else {
            temp = (200 + (longPressIntervalSeekBar.getProgress() * 100)) + " ms";
        }
        longPressIntervalText.setText(temp);

        if (doubleClickIntervalSeekBar.getProgress() == 0) {
            temp = "100 ms";
        } else {
            temp = (100 + (doubleClickIntervalSeekBar.getProgress() * 50)) + " ms";
        }
        doubleClickIntervalText.setText(temp);
        startSeekBarChangeListeners();
        startActivityMenuListeners();
    }

    /**
     * Run the intro, but only once!
     */
    private void runIntro() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                boolean isFirstRun = sharedPreferences.getBoolean("firstRun", true);

                if (isFirstRun) {
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);
                }
            }
        });
        t.start();
    }

    private void runUpdates() {
        try {
            int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            if (sharedPreferences.getInt("lastVersion", 0) != version) {
                try {
                    clearPrefs();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("lastVersion", version);
                    editor.apply();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clearPrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}

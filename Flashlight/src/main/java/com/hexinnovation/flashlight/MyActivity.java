package com.hexinnovation.flashlight;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.System;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MyActivity extends Activity {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private BrightnessControl mBrightnessControl;
    private DividerLine mDividerLine;
    private LightSwitch mLightSwitch;
    private GearActionBar mGearBar;
    private SettingsActionBar mSettingsBar;
    private SwitchCompat mLightPowerSwitch, mDarkPowerSwitch;
    private boolean mIsAnimatingToSettings, mSettingsShown;
    private int mCurrentTheme;
    private final static String SCREEN_BRIGHTNESS = "ScreenBrightness";
    private final static String SETTINGS_SHOWN = "SettingsShown";

    private RadioGroup mLightRadios, mDarkRadios;
    private Window mWindow;
    private LayoutParams mWindowAttrs;
    private Toast mLastToast;
    private AlertDialog mDialog;
    private boolean mIsRTL, mPermissionRequested;
    private Runnable mRunOnSettingsShown;
    private TextView mThemeTitle, mNotificationTitle;
    private Integer mActiveTheme;
    private final static Bitmap[] mThemeBitmaps = new Bitmap[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.myactivity);

        mThemeTitle = (TextView)findViewById(R.id.theme_title);
        ((LinearLayout.LayoutParams)mThemeTitle.getLayoutParams()).topMargin = Math.round(getResources().getDimension(R.dimen.padding) - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4f, getResources().getDisplayMetrics()));

        mNotificationTitle = (TextView)findViewById(R.id.notification_title);
        ((LinearLayout.LayoutParams)mNotificationTitle.getLayoutParams()).topMargin = Math.round(getResources().getDimension(R.dimen.padding) - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8f, getResources().getDisplayMetrics()));

        mSettingsBar = (SettingsActionBar)findViewById(R.id.settings_bar);
        mGearBar = (GearActionBar)findViewById(R.id.gear);
        mDividerLine = (DividerLine)findViewById(R.id.divider_line);
        mLightSwitch = (LightSwitch)findViewById(R.id.light_switch);
        mBrightnessControl = (BrightnessControl)findViewById(R.id.brightness_control);

        mIsRTL = findViewById(R.id.rtl) != null;

        mSettingsBar.setListener(new MyActionBar.Listener() {
            @Override
            public void onIconPressed() {
                animateBackFromSettings();
            }
        });

        int padding = getResources().getDimensionPixelOffset(R.dimen.padding);

        FrameLayout parent = (FrameLayout)findViewById(R.id.power_cycle_switch_parent);
        ContextThemeWrapper lightTheme = new ContextThemeWrapper(this, R.style.LightControl);
        ContextThemeWrapper darkTheme = new ContextThemeWrapper(this, R.style.DarkControl);

        mLightPowerSwitch = new SwitchCompat(lightTheme);
        mDarkPowerSwitch = new SwitchCompat(darkTheme);

        mLightPowerSwitch.setTextColor(ContextCompat.getColor(MyActivity.this, R.color.gray_light));
        mDarkPowerSwitch.setTextColor(ContextCompat.getColor(MyActivity.this, R.color.gray_dark));

        ((MyScrollView)findViewById(R.id.bg_2)).setOnScrollChangedListener(new Runnable() {
            @Override
            public void run() {
                mThemeBitmaps[0] = mThemeBitmaps[1] = null;
            }
        });

        CompoundButton.OnCheckedChangeListener switchChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView == mLightPowerSwitch && mCurrentTheme == 0) {
                    mDarkPowerSwitch.setChecked(isChecked);
                } else if (buttonView == mDarkPowerSwitch && mCurrentTheme == 1) {
                    mLightPowerSwitch.setChecked(isChecked);
                }
                mThemeBitmaps[0] = mThemeBitmaps[1] = null;
                Preferences.setCycleLightOnScreenOff(isChecked);
            }
        };

        Typeface roboto = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");

        for (SwitchCompat s : new SwitchCompat[] { mLightPowerSwitch, mDarkPowerSwitch }) {
            s.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.notification_title_text_size));
            s.setOnCheckedChangeListener(switchChangeListener);
            s.setSwitchMinWidth(getResources().getDimensionPixelSize(R.dimen.switch_width));
            s.setPadding(padding, 0, padding - Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F, getResources().getDisplayMetrics())), 0);
            s.setTypeface(roboto);
            s.setChecked(Preferences.getCycleLightOnScreenOff());
            SpannableStringBuilder text = new SpannableStringBuilder(getString(R.string.restart_light_title) + "\n");
            text.setSpan(new ForegroundColorSpan(ActivityCompat.getColor(this, s == mLightPowerSwitch ? R.color.title_light : R.color.title_dark)), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int start = text.length();
            text.append(getString(R.string.restart_light_message));
            text.setSpan(new AbsoluteSizeSpan(Math.round(getResources().getDimension(R.dimen.notification_subtext_size)), false), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(ActivityCompat.getColor(this, s == mLightPowerSwitch ? R.color.subtitle_light : R.color.subtitle_dark)), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setText(text);

            parent.addView(s, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        mLightRadios = (RadioGroup)findViewById(R.id.light_radios);
        mDarkRadios = (RadioGroup)findViewById(R.id.dark_radios);

        final AppCompatRadioButton lightNever = new AppCompatRadioButton(lightTheme);
        final AppCompatRadioButton darkNever = new AppCompatRadioButton(darkTheme);

        for (AppCompatRadioButton r : new AppCompatRadioButton[] { lightNever, darkNever }) {
            r.setText(getResources().getString(R.string.notification_never));
        }

        final AppCompatRadioButton lightAlways = new AppCompatRadioButton(lightTheme);
        final AppCompatRadioButton darkAlways = new AppCompatRadioButton(darkTheme);

        for (AppCompatRadioButton r : new AppCompatRadioButton[] { lightAlways, darkAlways }) {
            r.setText(getResources().getString(R.string.notification_always));
        }

        final AppCompatRadioButton lightSometimes = new AppCompatRadioButton(lightTheme);
        final AppCompatRadioButton darkSometimes = new AppCompatRadioButton(darkTheme);

        for (AppCompatRadioButton r : new AppCompatRadioButton[] { lightSometimes, darkSometimes }) {
            r.setText(getResources().getString(R.string.notification_sometimes));
        }

        mThemeTitle.setTypeface(roboto);
        mNotificationTitle.setTypeface(roboto);

        mLightRadios.setPadding(padding - Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6F, getResources().getDisplayMetrics())), 0, padding, 0);
        mDarkRadios.setPadding(padding - Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6F, getResources().getDisplayMetrics())), 0, padding, 0);

        CompoundButton.OnCheckedChangeListener radioListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    int option;
                    if (buttonView == lightAlways || buttonView == darkAlways) {
                        option = Preferences.NOTIFICATION_ALWAYS;
                        (buttonView == lightAlways ? darkAlways : lightAlways).setChecked(true);
                        mThemeBitmaps[0] = mThemeBitmaps[1] = null;
                    } else if (buttonView == lightNever || buttonView == darkNever) {
                        option = Preferences.NOTIFICATION_NEVER;
                        (buttonView == lightNever ? darkNever : lightNever).setChecked(true);
                        mThemeBitmaps[0] = mThemeBitmaps[1] = null;
                    } else {
                        option = Preferences.NOTIFICATION_SOMETIMES;
                        (buttonView == lightSometimes ? darkSometimes : lightSometimes).setChecked(true);
                        mThemeBitmaps[0] = mThemeBitmaps[1] = null;
                    }

                    if (Preferences.getNotificationSetting() != option) {
                        Preferences.setNotificationSetting(option);
                        FlashlightService.refreshNotification();
                    }
                }
            }
        };

        for (AppCompatRadioButton r : new AppCompatRadioButton[] { lightNever, lightSometimes, lightAlways, darkNever, darkSometimes, darkAlways }) {
            r.setOnCheckedChangeListener(radioListener);
            r.setTypeface(roboto);
            r.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.notification_subtext_size));
        }

        for (RadioButton r : new AppCompatRadioButton[] { lightNever, lightSometimes, lightAlways}) {
            r.setTextColor(ActivityCompat.getColor(this, R.color.subtitle_light));
            mLightRadios.addView(r, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        for (RadioButton r : new AppCompatRadioButton[] { darkNever, darkSometimes, darkAlways }) {
            r.setTextColor(ActivityCompat.getColor(this, R.color.subtitle_dark));
            mDarkRadios.addView(r, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        switch (Preferences.getNotificationSetting()) {
            case Preferences.NOTIFICATION_ALWAYS:
                lightAlways.setChecked(true);
                break;
            case Preferences.NOTIFICATION_NEVER:
                lightNever.setChecked(true);
                break;
            case Preferences.NOTIFICATION_SOMETIMES:
                lightSometimes.setChecked(true);
                break;
        }

        mGearBar.setListener(new MyActionBar.Listener() {
            @Override
            public void onIconPressed() {
                animateToSettings();
            }
        });



        mLightSwitch.setFlashLightListener(mFlashlightListener);
        mBrightnessControl.setListener(new BrightnessControl.Listener() {
            public void onBrightnessChanged(float newValue) {
                if (mWindowAttrs == null) {
                    mWindow = getWindow();
                    mWindowAttrs = mWindow.getAttributes();
                }

                mWindowAttrs.screenBrightness = newValue;
                mWindow.setAttributes(mWindowAttrs);
            }
        });

        ThemeSelector.Listener themeListener = new ThemeSelector.Listener() {
            @Override
            public void onThemeSelected(final int theme, final MotionEvent motionEvent) {
                if (motionEvent == null) {
                    mySetTheme(theme);
                    mActiveTheme = theme;
                } else if (mActiveTheme != null && mActiveTheme != theme) {
                    final int originalTheme = mActiveTheme;
                    mActiveTheme = null;
                    final Handler handler = new Handler();

                    final View rootView = findViewById(R.id.root);
                    final OverlayView overlayView = (OverlayView)findViewById(R.id.overlay);
                    overlayView.clearClipPath();

                    final boolean needToGenerateImages = mThemeBitmaps[theme] == null || mThemeBitmaps[originalTheme] == null;

                    final Runnable performTransition = new Runnable() {
                        @Override
                        public void run() {
                            overlayView.setVisibility(View.VISIBLE);
                            float cX = motionEvent.getRawX();
                            float cY = motionEvent.getRawY();

                            int[] locationOnScreen = new int[2];
                            overlayView.getLocationOnScreen(locationOnScreen);

                            cX -= locationOnScreen[0];
                            cY -= locationOnScreen[1];

                            float dX = Math.max(overlayView.getWidth() - cX, cX);
                            float dY = Math.max(overlayView.getHeight() - cY, cY);

                            final IBezier animation = BezierAnimation.easeIn(0, (float)Math.sqrt(dX*dX+dY*dY), 0, 200);

                            overlayView.setCenter(cX, cY);
                            overlayView.setRadius(0);
                            overlayView.setBitmap(mThemeBitmaps[theme]);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // We're showing the original theme.
                                    // We need to show the overlay of the new theme.
                                    overlayView.setRadius(animation.getCurrentValue());
                                    if (animation.hasEnded()) {
                                        overlayView.setVisibility(View.GONE);
                                        mySetTheme(theme);
                                        mActiveTheme = theme;
                                    } else {
                                        handler.post(this);
                                    }
                                }
                            });
                        }
                    };

//                    Bitmap bmp = mThemeBitmaps[theme];
                    if (needToGenerateImages) {
                        mThemeBitmaps[theme] = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
                        mThemeBitmaps[originalTheme] = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas(mThemeBitmaps[originalTheme]);
                        rootView.draw(canvas);
                        overlayView.setBitmap(mThemeBitmaps[originalTheme]);
                        overlayView.setVisibility(View.VISIBLE);

                        mySetTheme(theme);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                overlayView.setVisibility(View.GONE);
                                Canvas canvas = new Canvas(mThemeBitmaps[theme]);
                                rootView.draw(canvas);
                                overlayView.setVisibility(View.VISIBLE);
                                mySetTheme(originalTheme);
                                performTransition.run();
                            }
                        });
                    } else {
                        performTransition.run();
                    }
                }
            }
        };

        ThemeSelector themeSelector = (ThemeSelector)findViewById(R.id.theme_selector);
        ((LinearLayout.LayoutParams)themeSelector.getLayoutParams()).topMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics())-getResources().getDimension(R.dimen.padding));
        themeSelector.setListener(themeListener);

        themeListener.onThemeSelected(Preferences.getTheme(), null);

        if (savedInstanceState != null) {
            mBrightnessControl.setBrightness(savedInstanceState.getFloat(SCREEN_BRIGHTNESS, BrightnessControl.MIN_BRIGHTNESS));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = getIntent();
            if (intent != null) {
                Set<String> categories = intent.getCategories();
                if (categories != null && categories.contains(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
                    jumpToSettings();
                }
            }
        }
    }
    private void jumpToSettings() {
        final View settingsTab = findViewById(R.id.settings_tab);
        final int height = settingsTab.getHeight();
        if (height == 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    jumpToSettings();
                }
            });
            return;
        }

        final View mainTab = findViewById(R.id.main_tab);
        final FrameLayout.LayoutParams flps = (FrameLayout.LayoutParams)mainTab.getLayoutParams();
        flps.topMargin = flps.height = height;
        mainTab.setLayoutParams(flps);
        mainTab.requestLayout();
        mSettingsShown = true;
        FlashlightService.setActivity(null);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && intent != null) {
            Set<String> categories = intent.getCategories();
            if (categories != null && categories.contains(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)) {
                jumpToSettings();
            }
        }
    }

    private void mySetTheme(int theme) {
        mLightSwitch.setTheme(theme);
        mSettingsBar.setTheme(theme);
        mGearBar.setTheme(theme);
        mDividerLine.setTheme(theme);
        mBrightnessControl.setTheme(theme);

        int bgColor;
        switch (theme) {
            case 0:
                mThemeTitle.setTextColor(ContextCompat.getColor(MyActivity.this, R.color.title_light));
                mNotificationTitle.setTextColor(ContextCompat.getColor(MyActivity.this, R.color.title_light));
                mLightPowerSwitch.setVisibility(View.VISIBLE);
                mDarkPowerSwitch.setVisibility(View.GONE);
                bgColor = ContextCompat.getColor(MyActivity.this, R.color.bg_light);
                mLightRadios.setVisibility(View.VISIBLE);
                mDarkRadios.setVisibility(View.GONE);
                break;
            case 1:
                mThemeTitle.setTextColor(ContextCompat.getColor(MyActivity.this, R.color.title_dark));
                mNotificationTitle.setTextColor(ContextCompat.getColor(MyActivity.this, R.color.title_dark));
                mLightPowerSwitch.setVisibility(View.GONE);
                mDarkPowerSwitch.setVisibility(View.VISIBLE);
                bgColor = ContextCompat.getColor(MyActivity.this, R.color.bg_dark);
                mLightRadios.setVisibility(View.GONE);
                mDarkRadios.setVisibility(View.VISIBLE);
                break;
            default:
                throw new RuntimeException("invalid theme selected.");
        }

        for (int id : new int[] { R.id.settings_tab, R.id.main_tab, R.id.root, R.id.bg_2 }) {
            findViewById(id).setBackgroundColor(bgColor);
        }
        mCurrentTheme = theme;

    }
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    private static int getLightDialogTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return android.R.style.Theme_Material_Light_Dialog_NoActionBar;
        }
        return android.R.style.Theme_Holo_Light_Dialog;
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
        }
        super.onDestroy();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mBrightnessControl.setBrightness(savedInstanceState.getFloat(SCREEN_BRIGHTNESS, BrightnessControl.MIN_BRIGHTNESS));
        if (savedInstanceState.getBoolean(SETTINGS_SHOWN, false)) {
            jumpToSettings();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat(SCREEN_BRIGHTNESS, mBrightnessControl.getBrightness());
        outState.putBoolean(SETTINGS_SHOWN, mSettingsShown);
    }

    

    @Override
    public void onAttachedToWindow() {
        mWindow = getWindow();
        mWindowAttrs = mWindow.getAttributes();
        if (mBrightnessControl != null) {
            if (!mBrightnessControl.hasScreenBrightness() && System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) == System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                mBrightnessControl.setBrightness(System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS, 255) / 255f);
            }
        }
        
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        super.onAttachedToWindow();
    }
    
    
    public void notifyTurnOnFailed() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mLastToast != null) {
                    mLastToast.cancel();
                }

                mLightSwitch.notifyTurnOnFailed();
                mLastToast = Toast.makeText(MyActivity.this, getString(R.string.error_opening_camera), Toast.LENGTH_SHORT);
                mLastToast.show();
            }
        });
    }
    private final LightSwitch.Listener mFlashlightListener = new LightSwitch.Listener() {
        @Override
        public void onLightSwitched(boolean turnedOn) {
            if (turnedOn) {
                FlashlightService.turnOn();
            } else {
                FlashlightService.turnOff();
            }
        }
    };

    private void refreshLightSwitch() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (FlashlightService.isLightOn()) {
                mLightSwitch.turnLightOn();
            } else {
                mLightSwitch.turnLightOff();
            }
        } else {
            mLightSwitch.setNotSupported();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) && !hasCameraPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                if ((mDialog == null || !mDialog.isShowing()) && !mPermissionRequested) {
                    mDialog = new AlertDialog.Builder(this, getLightDialogTheme()).setTitle(R.string.camera_permissions_required_title).setMessage(R.string.camera_permissions_required_message).setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestCameraPermission();
                        }
                    }).setNegativeButton(getResources().getText(R.string.dont_allow), null).show();
                }
            } else {
                requestCameraPermission();
            }
        }

        if (!mSettingsShown) {
            FlashlightService.setActivity(this);
        }

        refreshLightSwitch();

        if (!mBrightnessControl.hasScreenBrightness()) {
            mBrightnessControl.setBrightness(0.5f);
        }

        animateInAllViews();
    }
    private void animateInAllViews() {
        for (IAnimateIn view : new IAnimateIn[] { mLightSwitch, mBrightnessControl, mDividerLine }) {
            view.animateIn(200);
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mPermissionRequested = true;
            }
        });
    }

    @Override
    protected void onPause() {
        FlashlightService.setActivity(null);
        mPermissionRequested = false;

        super.onPause();
    }


    public boolean isRTL() {
        return mIsRTL;
    }


    private void animateBackFromSettings() {
        mSettingsShown = false;
        animateInAllViews();
        FlashlightService.setActivity(this);
        final View mainTab = findViewById(R.id.main_tab);
        final View settingsTab = findViewById(R.id.settings_tab);
        final FrameLayout.LayoutParams flps = (FrameLayout.LayoutParams)mainTab.getLayoutParams();
        final IBezier animation = BezierAnimation.easeOut(flps.topMargin, 0, 0, 300);
        final Handler handler = new Handler();
        final int left = mainTab.getLeft();
        final int right = mainTab.getRight();
        final int height = settingsTab.getHeight();
        flps.height = height;
        final GearActionBar gear = (GearActionBar)findViewById(R.id.gear);
        gear.setSolidForeground(true);
        refreshLightSwitch();
        new Runnable() {
            @Override
            public void run() {
                flps.topMargin = Math.round(animation.getCurrentValue());
                mainTab.layout(left, flps.topMargin, right, flps.topMargin + height);
                if (animation.hasEnded()) {
                    ((MyActionBar)findViewById(R.id.settings_bar)).setSolidForeground(false);
                    mainTab.requestLayout();
                    gear.reverseAnimation();
                } else {
                    handler.post(this);
                }
            }
        }.run();
    }

    private void animateToSettings() {
        mIsAnimatingToSettings = true;
        final View settingsTab = findViewById(R.id.settings_tab);
        final View mainTab = findViewById(R.id.main_tab);
        final int left = mainTab.getLeft();
        final int right = mainTab.getRight();
        final int height = settingsTab.getHeight();
        final FrameLayout.LayoutParams flps = (FrameLayout.LayoutParams)mainTab.getLayoutParams();
        flps.height = height;
        final IBezier animation = BezierAnimation.easeIn(flps.topMargin, height, 0, 300);
        final Handler handler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                flps.topMargin = Math.round(animation.getCurrentValue());
                mainTab.layout(left, flps.topMargin, right, flps.topMargin + height);
                if (animation.hasEnded()) {
                    mainTab.setLayoutParams(flps);
                    mainTab.requestLayout();
                    mIsAnimatingToSettings = false;
                    mSettingsShown = true;
                    FlashlightService.setActivity(null);
                    if (mRunOnSettingsShown != null) {
                        mRunOnSettingsShown.run();
                        mRunOnSettingsShown = null;
                    }
                } else {
                    handler.post(this);
                }
            }
        }.run();
    }

    @Override
    public void onBackPressed() {
        if (mSettingsShown) {
            animateBackFromSettings();
        } else if (mIsAnimatingToSettings) {
            mRunOnSettingsShown = new Runnable() {
                @Override
                public void run() {
                    animateBackFromSettings();
                }
            };
        } else {
            super.onBackPressed();
        }
    }
}


package com.cmge.cge.sdk.api;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cmge.cge.sdk.util.CLog;

import java.util.ArrayList;
import java.util.List;

public class CgeSdkSplashActivity extends Activity {

    private static final String CHANNEL_SPLASH_IMAGE_PREFIX = "cge_sdk_channel_splash_image_";
    private static final String GAME_SPLASH_IMAGE_PREFIX = "cge_sdk_game_splash_image_";

    private static final int DEFAULT_SPLASH_INTERVAL = 3000; // milliseconds

    private Handler mHandler = new Handler();

    private List<Integer> mSplashImagesIdList = new ArrayList<Integer>();

    private ViewGroup mRootView;
    private ImageView mSplashImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // root layout
        mRootView = new RelativeLayout(this);
        mRootView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        setContentView(mRootView);

        // splash image view
        mSplashImageView = new ImageView(this);
        mSplashImageView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        mSplashImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mRootView.addView(mSplashImageView);

        loadChannelSplashImages();
        loadGameSplashImages();

        playSplashImage(0);
    }

    /**
     * 闪屏结束回调
     */
    protected void onSplashEnded() {
        String gameMainActivity = CgeSdk.getInstance().getGameMainActivity();
        if (gameMainActivity == null) {
            throw new RuntimeException("no game main activity");
        }

        Intent intent = new Intent();
        intent.setClassName(this, CgeSdk.getInstance().getGameMainActivity());
        startActivity(intent);
    }

    /**
     * 设置闪屏背景颜色，图片无法覆盖的部分将以改颜色填充
     * 
     * @return 闪屏背景颜色
     */
    protected int getSplashBackgroundColor(int index) {
        return Color.BLACK;
    }

    /**
     * 设置闪屏持续时间
     * 
     * @return 闪屏持续时间，单位为毫秒
     */
    protected int getSplashInterval() {
        return DEFAULT_SPLASH_INTERVAL;
    }

    private void loadChannelSplashImages() {
        loadSplashImages(CHANNEL_SPLASH_IMAGE_PREFIX);
    }

    private void loadGameSplashImages() {
        loadSplashImages(GAME_SPLASH_IMAGE_PREFIX);
    }

    private void loadSplashImages(String prefix) {
        int index = 0;
        int id = 0;
        do {
            final String name = prefix + index;
            id = getResources().getIdentifier(name, "drawable", getPackageName());
            if (id != 0) {
                CLog.d(CLog.TAG_CORE, "found splash image: " + name);
                mSplashImagesIdList.add(id);
            }
            index++;
        } while (id != 0);
    }

    private void playSplashImage(final int index) {
        if (index < 0 || index >= mSplashImagesIdList.size()) {
            onSplashEnded();
            finish();
            return;
        }

        mSplashImageView.setImageResource(mSplashImagesIdList.get(index));

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                playSplashImage(index + 1);
            }
        }, getSplashInterval());
    }
}

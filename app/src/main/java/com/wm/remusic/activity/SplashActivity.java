package com.wm.remusic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wm.remusic.R;
import com.wm.remusic.activity.MainActivity;
import com.wm.remusic.uitl.PermissionHelper;

import net.youmi.android.AdManager;
import net.youmi.android.normal.common.ErrorCode;
import net.youmi.android.normal.spot.SplashViewSettings;
import net.youmi.android.normal.spot.SpotListener;
import net.youmi.android.normal.spot.SpotManager;

/**
 * <p>开屏窗口</p>
 * Edited by Alian Lee on 2016-11-25.
 */
public class SplashActivity extends Activity {
    private Context mContext;
	private PermissionHelper mPermissionHelper;
	private static String TAG = "SplashActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;
		// 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 移除标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

		// 当系统为6.0以上时，需要申请权限
		mPermissionHelper = new PermissionHelper(this);
		mPermissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener() {
			@Override
			public void onAfterApplyAllPermission() {
				Log.i(TAG, "All of requested permissions has been granted, so run app logic.");
				runApp();
			}
		});
		if (Build.VERSION.SDK_INT < 23) {
			// 如果系统版本低于23，直接跑应用的逻辑
			Log.d(TAG, "The api level of system is lower than 23, so run app logic directly.");
			runApp();
		} else {
			// 如果权限全部申请了，那就直接跑应用逻辑
			if (mPermissionHelper.isAllRequestedPermissionGranted()) {
				Log.d(TAG, "All of requested permissions has been granted, so run app logic directly.");
				runApp();
			} else {
				// 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
				Log.i(TAG, "Some of requested permissions hasn't been granted, so apply permissions first.");
				mPermissionHelper.applyPermissions();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mPermissionHelper.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 跑应用的逻辑
	 */
	private void runApp() {
		//初始化SDK
		AdManager.getInstance(mContext).init("9ca9f88752601ff9", "c91f620182e46bad", false, true);
		//设置开屏
		setupSplashAd();
	}

	/**
	 * 设置开屏广告
	 */
	private void setupSplashAd() {
		// 创建开屏容器
		final RelativeLayout splashLayout = (RelativeLayout) findViewById(R.id.rl_splash);
		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ABOVE, R.id.view_divider);

		// 对开屏进行设置
		SplashViewSettings splashViewSettings = new SplashViewSettings();
		//		// 设置是否展示失败自动跳转，默认自动跳转
		//		splashViewSettings.setAutoJumpToTargetWhenShowFailed(false);
		// 设置跳转的窗口类
		splashViewSettings.setTargetClass(MainActivity.class);
		// 设置开屏的容器
		splashViewSettings.setSplashViewContainer(splashLayout);

		// 展示开屏广告
		SpotManager.getInstance(mContext)
				.showSplash(mContext, splashViewSettings, mStopListener);
	}

	static SpotListener mStopListener = new SpotListener() {

		@Override
		public void onShowSuccess() {
			logInfo("开屏展示成功");
		}

		@Override
		public void onShowFailed(int errorCode) {
			logError("开屏展示失败");
			switch (errorCode) {
				case ErrorCode.NON_NETWORK:
					logError("网络异常");
					break;
				case ErrorCode.NON_AD:
					logError("暂无开屏广告");
					break;
				case ErrorCode.RESOURCE_NOT_READY:
					logError("开屏资源还没准备好");
					break;
				case ErrorCode.SHOW_INTERVAL_LIMITED:
					logError("开屏展示间隔限制");
					break;
				case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
					logError("开屏控件处在不可见状态");
					break;
				default:
					logError("errorCode: %d", errorCode);
					break;
			}
		}

		@Override
		public void onSpotClosed() {
			logDebug("开屏被关闭");
		}

		@Override
		public void onSpotClicked(boolean isWebPage) {
			logDebug("开屏被点击");
			logInfo("是否是网页广告？%s", isWebPage ? "是" : "不是");
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 开屏展示界面的 onDestroy() 回调方法中调用
		SpotManager.getInstance(mContext).onDestroy();
	}

	/**
	 * 打印调试级别日志
	 *
	 * @param format
	 * @param args
	 */
	protected static void logDebug(String format, Object... args) {
		logMessage(Log.DEBUG, format, args);
	}

	/**
	 * 打印信息级别日志
	 *
	 * @param format
	 * @param args
	 */
	protected static void logInfo(String format, Object... args) {
		logMessage(Log.INFO, format, args);
	}

	/**
	 * 打印错误级别日志
	 *
	 * @param format
	 * @param args
	 */
	protected static void logError(String format, Object... args) {
		logMessage(Log.ERROR, format, args);
	}

	/**
	 * 展示短时Toast
	 *
	 * @param format
	 * @param args
	 */
	protected void showShortToast(String format, Object... args) {
		showToast(Toast.LENGTH_SHORT, format, args);
	}

	/**
	 * 展示长时Toast
	 *
	 * @param format
	 * @param args
	 */
	protected void showLongToast(String format, Object... args) {
		showToast(Toast.LENGTH_LONG, format, args);
	}

	/**
	 * 打印日志
	 *
	 * @param level
	 * @param format
	 * @param args
	 */
	private static void logMessage(int level, String format, Object... args) {
		String formattedString = String.format(format, args);
		switch (level) {
			case Log.DEBUG:
				Log.d(TAG, formattedString);
				break;
			case Log.INFO:
				Log.i(TAG, formattedString);
				break;
			case Log.ERROR:
				Log.e(TAG, formattedString);
				break;
		}
	}

	/**
	 * 展示Toast
	 *
	 * @param duration
	 * @param format
	 * @param args
	 */
	private void showToast(int duration, String format, Object... args) {
		Toast.makeText(mContext, String.format(format, args), duration).show();
	}
}

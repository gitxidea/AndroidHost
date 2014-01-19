package org.xidea.android.host;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.os.Build;
import android.os.Bundle;

public class HostApplication extends Application {
	private static HostApplication INSTANCE;
	private ArrayList<ApplicationCallback> callbackList = new ArrayList<ApplicationCallback>();

	public interface ApplicationCallback extends ComponentCallbacks {
		public void onCreate(Application app);

		public void onTrimMemory(int level);
	}

    public interface ActivityCallback {
        void onCreated(Activity activity, Bundle savedInstanceState);
        void onStarted(Activity activity);
        void onResumed(Activity activity);
        void onPaused(Activity activity);
        void onStopped(Activity activity);
        void onSaveInstanceState(Activity activity, Bundle outState);
        void onDestroyed(Activity activity);
    }
	public static HostApplication getInstance(){
		return INSTANCE;
	}
	@Override
	public void onCreate() {
		INSTANCE = this;
		super.onCreate();
		HostEnv.init(this);
		//load map example
		HostEnv.requirePluginPackage("com.example.a");
		
		Object[] callbacks = callbackList.toArray();
		for (int i = 0; i < callbacks.length; i++) {
			Object c = callbacks[i];
			if (c instanceof ApplicationCallback) {
				((ApplicationCallback) c).onCreate(this);
			}
		}
	}

	@SuppressLint("NewApi")
	public void registerApplicationCallbacks(ApplicationCallback callback) {
		synchronized (callback) {
			callbackList.add(callback);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			registerComponentCallbacks(callback);
		}
	}

	@SuppressLint("NewApi")
	public void unregisterActivityLifecycleCallbacks(
			ApplicationCallback callback) {
		synchronized (callback) {
			callbackList.remove(callback);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			super.unregisterComponentCallbacks(callback);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onTrimMemory(int level) {
		Object[] callbacks = callbackList.toArray();
		for (int i = 0; i < callbacks.length; i++) {
			Object c = callbacks[i];
			if (c instanceof ApplicationCallback) {
				((ApplicationCallback) c).onTrimMemory(level);
			}
		}
		super.onTrimMemory(level);
	}

}

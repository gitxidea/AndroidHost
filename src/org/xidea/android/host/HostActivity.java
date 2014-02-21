package org.xidea.android.host;

import java.io.Serializable;
import java.util.ArrayList;


import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;

public class HostActivity extends FragmentActivity {

	private static final String PAGE_LAYOUT = "PAGE_LAYOUT";
	private static final String PAGE_PACKAGE = "PAGE_PACKAGE_NAME";
	private static final String PAGE_CLASS = "PAGE_CLASS_NAME";
	private static final String PAGE_ARGUMENTS = "PAGE_ARGUMENTS";
	private PluginPackage pluginPackage;
	private Context baseContext = this;
	private ArrayList<HostApplication.ActivityCallback> callbacks = new ArrayList<HostApplication.ActivityCallback>();

	private static final int STATE_CREATE = 0, STATE_STARTED = 1,
			STATE_RESUMED = 2, STATE_PAUSED = 3, STATE_STOPPED = 4,
			STATE_SAVE = 5, STATE_DESTROYED = 6;


	protected void doCallback(int type, Bundle savedInstanceState) {
		for (HostApplication.ActivityCallback callback : callbacks) {
			switch (type) {
			case STATE_CREATE:
				callback.onCreated(this, savedInstanceState);
				break;
			case STATE_STARTED:
				callback.onStarted(this);
				break;
			case STATE_RESUMED:
				callback.onResumed(this);
				break;
			case STATE_PAUSED:
				callback.onPaused(this);
				break;
			case STATE_STOPPED:
				callback.onStopped(this);
				break;
			case STATE_SAVE:
				callback.onSaveInstanceState(this, savedInstanceState);
				break;
			case STATE_DESTROYED:
				callback.onDestroyed(this);
				break;
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onCreateParepare();
		doCallback(STATE_CREATE, savedInstanceState);
	}

	protected void onStart() {
		super.onStart();
		doCallback(STATE_STARTED, null);
	}

	protected void onResume() {
		super.onResume();
		doCallback(STATE_RESUMED, null);
	}

	protected void onPause() {
		super.onPause();
		doCallback(STATE_PAUSED, null);
	}

	protected void onStop() {
		super.onStop();
		doCallback(STATE_STOPPED, null);
	}

	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		doCallback(STATE_SAVE, null);
	}

	protected void onDestroy() {
		super.onDestroy();
		doCallback(STATE_DESTROYED, null);
	}

	private void onCreateParepare() {
		Intent intent = this.getIntent();
		int layout = intent.getIntExtra(PAGE_LAYOUT, -1);
		if (layout < 0) {
			setContentView(R.layout.host);
		}
		Class<?> type = null;
		Serializable classObject = intent.getSerializableExtra(PAGE_CLASS);
		if (classObject instanceof Class<?>) {
			type = (Class<?>) classObject;
			this.pluginPackage = HostEnv.findClassPackage(type);
		} else {
			String pkg = intent.getStringExtra(PAGE_PACKAGE);
			this.pluginPackage = HostEnv.requirePluginPackage(pkg);
			if (classObject instanceof String) {
				String className = (String) classObject;
				try {
					type = pluginPackage.loadClass(className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			//}else if(classObject == null){
			//	type == null;	
			}
		}
		this.baseContext = pluginPackage.getPluginContext();
		if (layout > 0) {
			setContentView(layout);
		}
		Bundle arguments = intent.getBundleExtra(PAGE_ARGUMENTS);
		try {
			Fragment fragments;
			if(type != null){
				fragments = (Fragment) type.newInstance();
			}else{
				fragments = pluginPackage.getDefaultPlugin().index();
			}
			fragments.setArguments(arguments);
			//fragments.setRetainInstance(true);
			FragmentManager manager = getSupportFragmentManager();
			manager.beginTransaction().add(R.id.body, fragments, "fragment").commit();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	protected void attachBaseContext(Context newBase){
		this.baseContext = newBase;
		super.attachBaseContext(newBase);
	}
	public LayoutInflater getLayoutInflater() {
		return pluginPackage == null?super.getLayoutInflater():pluginPackage.getLayoutInflater();
	}
	public Resources getResources() {
		return baseContext.getResources();
	}
	public ClassLoader getClassLoader(){
		return baseContext.getClassLoader();
	}
	public Context getApplicationContext(){
		return baseContext;
	}
	public AssetManager getAssets(){
		return baseContext.getAssets();
	}
	public Object getSystemService(String name){
		return baseContext.getSystemService(name);
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent data) {
		super.onActivityResult(requestCode, responseCode, data);
	}

	public static Intent create(Class<? extends Fragment> type, Bundle arguments) {
		return doCreate(null, type, arguments);
	}

	public static Intent create(String pluginPackage, Bundle arguments) {
		return doCreate(pluginPackage, null, arguments);
	}

	public static Intent create(String pluginPackage, String className,
			Bundle arguments) {
		return doCreate(pluginPackage, className, arguments);
	}

	private static Intent doCreate(String pluginPackage,
			Serializable className, Bundle arguments) {
		Intent intent = new Intent(HostApplication.getInstance(),
				HostActivity.class);
		if (className != null) {
			intent.putExtra(PAGE_CLASS, className);
		}
		if (pluginPackage != null) {
			intent.putExtra(PAGE_PACKAGE, pluginPackage);
		}
		if (arguments != null) {
			intent.putExtra(PAGE_ARGUMENTS, arguments);
		}
		return intent;
	}
}

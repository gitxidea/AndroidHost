package org.xidea.android.host;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;

public class HostActivity extends FragmentActivity {

	private static final String PAGE_LAYOUT = "PAGE_LAYOUT";
	private static final String PAGE_PACKAGE_NAME = "PAGE_PACKAGE_NAME";
	private static final String PAGE_CLASS_NAME = "PAGE_CLASS_NAME";
	private static final String PAGE_ARGUMENTS = "PAGE_ARGUMENTS";
	private PluginPackage pluginPackage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Intent intent = this.getIntent();
		int layout = intent.getIntExtra(PAGE_LAYOUT, -1);
		if (layout < 0) {
			setContentView(R.layout.host);
		}
		Class<?> type;
		Serializable classObject = intent.getSerializableExtra(PAGE_CLASS_NAME);
		if (classObject instanceof Class<?>) {
			type = (Class<?>) classObject;
			this.pluginPackage = HostEnv.findClassPackage(type);
		} else {
			String pkg = intent.getStringExtra(PAGE_PACKAGE_NAME);
			String className = intent.getStringExtra(PAGE_CLASS_NAME);
			this.pluginPackage = HostEnv.requirePluginPackage(pkg);
			try {
				type = pluginPackage.loadClass(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if(layout>0){
			setContentView(layout);
		}
		Bundle arguments = intent.getBundleExtra(PAGE_ARGUMENTS);
		try {
			Fragment fragments = (Fragment) type.newInstance();
			fragments.setArguments(arguments);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// super.attachBaseContext(this.pluginPackage.getPluginContext());
		super.onCreate(savedInstanceState);
	}

	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
	}

	public LayoutInflater getLayoutInflater() {
		return pluginPackage == null ? super.getLayoutInflater()
				: pluginPackage.getLayoutInflater();
	}

	public Resources getResources() {
		return pluginPackage == null ? super.getResources() : pluginPackage
				.getPluginContext().getResources();
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent data) {
		super.onActivityResult(requestCode, responseCode, data);
	}

	public static Intent create(String pluginPackage, String className,
			Bundle arguments) {
		Intent intent = new Intent(HostApplication.getInstance(),
				HostActivity.class);
		intent.putExtra(PAGE_CLASS_NAME, className);
		intent.putExtra(PAGE_PACKAGE_NAME, pluginPackage);
		if (arguments != null) {
			intent.putExtra(PAGE_ARGUMENTS, arguments);
		}
		return intent;
	}
}

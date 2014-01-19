package org.xidea.android.host;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;

public class HostActivity extends FragmentActivity {

	private static final String STATE_PACKAGE_NAME = "STATE_PACKAGE_NAME";
	private static final String STATE_FRAGMENT_CLASS = "STATE_PACKAGE_NAME";
	private PluginPackage pluginPackage;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String pkg = this.getIntent().getExtras().getString(STATE_PACKAGE_NAME);
		this.pluginPackage = HostEnv.requirePluginPackage(pkg);
		super.attachBaseContext(this.pluginPackage.getPluginContext());
		super.onCreate(savedInstanceState);
	}

	protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }
	public LayoutInflater getLayoutInflater() {
		return pluginPackage.getLayoutInflater();
	}

	public Resources getResources() {
		return pluginPackage.getPluginContext().getResources();
	}
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data) {
		super.onActivityResult(requestCode, responseCode, data);
	}
}

package org.xidea.android.host;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;

public abstract interface PluginLoader {
	public abstract Plugin getPlugin();

	public abstract Context getPluginContext();
	
	public abstract ClassLoader getClassLoader();

	public abstract LayoutInflater getLayoutInflater();

	public abstract List<ClassLoader> getDependences();
}

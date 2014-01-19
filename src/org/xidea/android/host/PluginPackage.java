package org.xidea.android.host;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;

public abstract interface PluginPackage {
	public abstract Plugin getDefaultPlugin();

	public abstract Context getPluginContext();
	
	public abstract ClassLoader getClassLoader();

	public Class<?> loadClass(String className) throws ClassNotFoundException;

	public abstract LayoutInflater getLayoutInflater();

	public abstract List<ClassLoader> getDependences();
}

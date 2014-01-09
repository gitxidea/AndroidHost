package org.xidea.android;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;

public interface Plugin {
	public Context getBaseContext();
	public List<ClassLoader> getDependences();
	public LayoutInflater getLayoutInflater();

}

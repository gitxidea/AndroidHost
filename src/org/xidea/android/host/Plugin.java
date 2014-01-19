package org.xidea.android.host;

import android.app.Fragment;

public interface Plugin {
	public void install(PluginPackage context);
	public void uninstall();
	public Fragment index();
}

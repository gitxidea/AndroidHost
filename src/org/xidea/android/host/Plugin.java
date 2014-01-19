package org.xidea.android.host;

public interface Plugin {
	public void install(PluginPackage context);
	public void uninstall();
}

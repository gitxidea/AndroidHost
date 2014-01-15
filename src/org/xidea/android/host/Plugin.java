package org.xidea.android.host;

public interface Plugin {
	public void install(PluginLoader context);
	public void uninstall();
}

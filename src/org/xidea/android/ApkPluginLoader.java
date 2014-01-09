package org.xidea.android;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import dalvik.system.DexClassLoader;

public class ApkPluginLoader extends DexClassLoader implements Plugin{
	private ArrayList<ClassLoader> dependences = new ArrayList<ClassLoader>();
	private PublishConfig publishConfig = new PublishConfig();
	private ApkConfig apkConfig ;
	public ApkPluginLoader(Application app, File source){
		this(new ApkConfig(app,source,ApkPluginLoader.class.getClassLoader()));
	}
	private ApkPluginLoader(ApkConfig config){
		super(config.dexPath,config.optimizedDirectory,config.libraryPath,config.parent);
		this.apkConfig = config;
		config.classLoader = this;
	}
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return super.loadClass(className, false);
    }
	@Override
	public Context getBaseContext() {
		return apkConfig;
	}
	@Override
	public List<ClassLoader> getDependences() {
		return dependences;
	}
	public void addDependence(ClassLoader loader){
		dependences.add(loader);
	}
	protected Class<?> findClass(String name) throws ClassNotFoundException{
		for(ClassLoader loader: dependences){
			Class<?> clazz ;
			if(loader instanceof ApkPluginLoader){
				clazz = ((ApkPluginLoader)loader).getPublicClass(name);
				
			}else{
				clazz = loader.loadClass(name);
			}
			if(clazz != null){
				return clazz;
			}
		}
		return super.findClass(name);
	}

	protected Class<?> getPublicClass(String name){
		boolean contains = publishConfig.containsClass(name);
		if(contains){
			try{
				return super.loadClass(name,false);
			}catch(ClassNotFoundException cnf){
			}
		}
		return null;
	}
	public void addPublicPackage(String name){
		publishConfig.addPublicClass(name+".[^.]+");
	}

	private static class PublishConfig{
		private StringBuilder buf = new StringBuilder();
		private Pattern publicPattern ;

		public void addPublicClass(String name){
			if(buf.length()>0){
				buf.append('|');
			}
			buf.append("^"+name.replaceAll("[\\.]","\\.")+"$");
			publicPattern = null;
		}

		private boolean containsClass(String name) {
			Pattern p = publicPattern;
			if(p == null){
				publicPattern = p = Pattern.compile(buf.length() == 0?".":buf.toString());
			}
			boolean contains = p.matcher(name).find();
			return contains;
		}
		
	}
	private  static class ApkConfig extends ContextWrapper{
		private AssetManager assetManager ;
		private Resources resources;
		static Method addAssetPath ;
		static{
			try {
				addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
				addAssetPath.setAccessible(true);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Plugin init failed",e);
			}
		}
		private final String dexPath;
		private final String optimizedDirectory;
		private final String libraryPath;
		private final ClassLoader parent;
		private ClassLoader classLoader;
		private LayoutInflater layoutInflater ;
		private Theme theme;
		
		public ApkConfig(Application app,File source,ClassLoader parent){
			super(app);
			File pluginDir = app.getDir("plugin",0);
			if (!pluginDir.exists()) {
				pluginDir.mkdir();
			}
			this.dexPath = source.getAbsolutePath();
			this.optimizedDirectory = pluginDir.getAbsolutePath();
			this.libraryPath = null;
			this.parent = parent;
			try {
				this.assetManager = AssetManager.class.newInstance();
				addAssetPath.invoke(assetManager, dexPath);
			} catch (Exception e) {
				throw new RuntimeException("Plugin init failed",e);
			}
		}
		public 	Resources.Theme getTheme(){
			if(this.theme == null){
				Theme oldTheme = super.getTheme();
				this.theme = this.getResources().newTheme();
				this.theme.setTo(oldTheme);
			}
			return this.theme;
		}
		public LayoutInflater getLayoutInflater() {
			if(layoutInflater == null){
				LayoutInflater layoutInflater =
		                (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				this.layoutInflater = layoutInflater.cloneInContext(this);
			}
			return layoutInflater;
		}
		public AssetManager getAssets(){
			return assetManager;
		}
		public ClassLoader getClassLoader(){
			return classLoader;
		}
		public Resources getResources() {
			if(resources == null){
				Resources parent = super.getResources();
				this.resources= new Resources(assetManager, parent.getDisplayMetrics(),parent.getConfiguration());
			}
			return resources;
		}
		
	}
	@Override
	public LayoutInflater getLayoutInflater() {
		return apkConfig.getLayoutInflater();
	}
}

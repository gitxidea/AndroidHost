package org.xidea.android;

import java.util.ArrayList;
import java.util.regex.Pattern;

import dalvik.system.DexClassLoader;

public class PluginDexLoader extends DexClassLoader{
	private ArrayList<ClassLoader> dependences = new ArrayList<ClassLoader>();
	private StringBuilder buf = new StringBuilder();
	private Pattern publicPattern ;

	public PluginDexLoader(String dexPath, String optimizedDirectory,
			String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}
	protected Class<?> findClass(String name) throws ClassNotFoundException{
		for(ClassLoader loader: dependences){
			Class<?> clazz ;
			if(loader instanceof PluginDexLoader){
				clazz = ((PluginDexLoader)loader).getPublicClass(name);
				
			}else{
				clazz = loader.loadClass(name);
			}
			if(clazz != null){
				return clazz;
			}
		}
		return super.findClass(name);
	}
	public void addPublicPackage(String name){
		addPublicClass(name+".[^.]+");
	}
	public void addPublicClass(String name){
		if(buf.length()>0){
			buf.append('|');
		}
		buf.append("^"+name.replaceAll("[\\.]","\\.")+"$");
		publicPattern = null;
	}
	protected Class<?> getPublicClass(String name){
		Pattern p = publicPattern;
		if(p == null){
			publicPattern = p = Pattern.compile(buf.length() == 0?".":buf.toString());
		}
		boolean contains = p.matcher(name).find();
		if(contains){
			try{
				return super.loadClass(name,false);
			}catch(ClassNotFoundException cnf){
			}
		}
		return null;
	}

}

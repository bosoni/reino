package com.mjt.reino.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mjt.reino.Reino;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg  = new LwjglApplicationConfiguration();
		cfg.title = "Reino";
		cfg.width = 1024;
		cfg.height = 768;
		
		new LwjglApplication(new Reino(), cfg);
	}
}

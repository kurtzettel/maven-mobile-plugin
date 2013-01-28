package com.goodformobile.sample;

import net.rim.device.api.ui.UiApplication;

/**
 * This class extends the UiApplication class, providing a graphical user
 * interface.
 */
public class MyApplication extends UiApplication {
	/**
	 * Entry point for application
	 * 
	 * @param args
	 *            Command line arguments (not used)
	 */
	public static void main(String[] args) {
		// Create a new instance of the application and make the currently
		// running thread the application's event dispatch thread.
		MyApplication theApp = new MyApplication();
		theApp.enterEventDispatcher();
	}

	/**
	 * Creates a new MyApplication object
	 */
	public MyApplication() {
		// Push a screen onto the UI stack for rendering.
		pushScreen(new MyScreen());
	}
}

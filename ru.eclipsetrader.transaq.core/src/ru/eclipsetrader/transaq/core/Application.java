package ru.eclipsetrader.transaq.core;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class Application implements IApplication {
	
	private static final String DEFAULT_CONTAINER_TYPE = "ecf.r_osgi.peer";
	public static final String DEFAULT_CONTAINER_ID = "r-osgi://localhost:9278";

	private static String containerType = DEFAULT_CONTAINER_TYPE;
	private static String containerId = DEFAULT_CONTAINER_ID;
	
	private BundleContext bundleContext;
	private final Object appLock = new Object();
	private boolean done = false;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		bundleContext = CoreActivator.getContext();
		waitForDone();
		return IApplication.EXIT_OK;
	}
	
	private void waitForDone() {
		// then just wait here
		synchronized (appLock) {
			while (!done) {
				try {
					appLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	@Override
	public void stop() {

		bundleContext = null;
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
		}
	}


}

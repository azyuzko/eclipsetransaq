package ru.eclipsetrader.transaq.core;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;

public class Application implements IApplication {
		
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
	
	public BundleContext getContext() {
		return bundleContext;
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

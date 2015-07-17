package ru.eclipsetrader.transaq.core;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

import ru.eclipsetrader.transaq.core.datastorage.TQCandleService;
import ru.eclipsetrader.transaq.core.datastorage.TQClientService;
import ru.eclipsetrader.transaq.core.datastorage.TQMarketService;
import ru.eclipsetrader.transaq.core.datastorage.TQServerService;
import ru.eclipsetrader.transaq.core.osgi.TransaqCommandProvider;
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.server.TransaqServerManager;
import ru.eclipsetrader.transaq.core.services.ITQCandleService;
import ru.eclipsetrader.transaq.core.services.ITQClientService;
import ru.eclipsetrader.transaq.core.services.ITQMarketService;
import ru.eclipsetrader.transaq.core.services.ITQSecurityService;
import ru.eclipsetrader.transaq.core.services.ITQServerService;
import ru.eclipsetrader.transaq.core.services.ITransaqServerManager;

public class CoreActivator implements BundleActivator {

	private static ServiceTracker<EventAdmin, Object> serviceTracker;
	private static BundleContext context;
	static EventAdmin eventAdmin;
	static Map<Class<?>, Object> services = new HashMap<>();
	
	public static EventAdmin getEventAdmin() {
		if (eventAdmin == null) {
			ServiceReference<EventAdmin> srEventAdmin = context
					.getServiceReference(EventAdmin.class);
			serviceTracker = new ServiceTracker<>(context,
					EventAdmin.class.getName(), null);
			serviceTracker.open();
			eventAdmin = (EventAdmin) serviceTracker.getService(srEventAdmin);
		}
		return eventAdmin;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getServiceInstance(Class<T> class_) {
		if (services.containsKey(class_)) {
			return (T) services.get(class_);
		} else {
			T t = context.getService(context.getServiceReference(class_));
			services.put(class_, t);
			return t;
		}
	}

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		CoreActivator.context = bundleContext;

		registerCommandProvider();

		registerServices();

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				TransaqServerManager.getInstance().connect(Constants.DEFAULT_SERVER_ID);
			}
		});
		thread.start();
	}

	public void registerServices() {
		context.registerService(ITransaqServerManager.class, TransaqServerManager.getInstance(), null);
		context.registerService(ITQServerService.class, TQServerService.getInstance(), null);
		context.registerService(ITQMarketService.class, TQMarketService.getInstance(), null);
		context.registerService(ITQSecurityService.class, TQSecurityService.getInstance(), null);
		context.registerService(ITQClientService.class,	TQClientService.getInstance(), null);
		context.registerService(ITQCandleService.class, TQCandleService.getInstance(), null);
		System.out.println("Transaq Core: Services Registered");
	}

	public void registerCommandProvider() {
		TransaqCommandProvider commandProvider = new TransaqCommandProvider();
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(org.osgi.framework.Constants.SERVICE_RANKING, new Integer(
				Integer.MAX_VALUE - 100));
		context.registerService(CommandProvider.class.getName(),
				commandProvider, props);
		System.out.println("Transaq Core: Command Provider Registered");
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if (serviceTracker != null) {
			serviceTracker.close();
		}
		CoreActivator.context = null;
	}

}

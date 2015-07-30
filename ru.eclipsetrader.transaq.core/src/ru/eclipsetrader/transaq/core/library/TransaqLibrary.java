package ru.eclipsetrader.transaq.core.library;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import ru.eclipsetrader.transaq.core.exception.CommandException;
import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.util.Utils;
import ru.eclipsetrader.transaq.core.xml.handler.ResultHandler;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class TransaqLibrary {
	
	private static final Logger logger = LogManager.getLogger(TransaqLibrary.class);

	static ITransaqLibrary library = (ITransaqLibrary)Native.loadLibrary(
			"x86".equals(System.getProperty("os.arch")) ? "txmlconnector" :  "txmlconnector64", 
					ITransaqLibrary.class);;

	public static void Initialize(String path, int level) {
		System.setProperty("jna.encoding", "UTF-8");
		Pointer pResult = library.Initialize(path, level);
		if (pResult != null) {
			String error = pResult.getString(0);
			library.FreeMemory(pResult);
			throw new RuntimeException(error);
		}
	}

	public static String SetLogLevel(int level) {
		Pointer pResult = library.SetLogLevel(level);
		String result = pResult.getString(0);
		library.FreeMemory(pResult);
		return result;
	}

	public static CommandResult SendCommand(String data) {
		
		//TODO refactoring if (TransaqServer.INSTANCE.getServer().isDbLogging()) {
		//	DatabaseManager.writeOutputEvent(data);
		// }
		logger.debug("Send " + data);
		synchronized (library) {
			Pointer pResult = library.SendCommand(data);
			String result = pResult.getString(0);
			// System.out.println("SendCommand result = " + result);
			library.FreeMemory(pResult);
			ResultHandler handler = new ResultHandler();
			try {
				SAXParser parser = Utils.getSAXParserFactory().newSAXParser();
				parser.parse(new InputSource(new StringReader(result)), handler);
			} catch (Exception e) {
				throw new CommandException(e);
			}
			CommandResult commandResult = handler.getCommandResult();
			if (!commandResult.isSuccess()) { 
				throw new CommandException(commandResult);
			}
			logger.debug(commandResult);
			return commandResult;
		}
	}

	public static boolean SetCallback(Callback pCallback) {
		return library.SetCallback(pCallback);
	}

	public static boolean SetCallbackEx(Callback pCallbackEx) {
		return library.SetCallbackEx(pCallbackEx);
	}

	public static boolean FreeMemory(Pointer pData) {
		return library.FreeMemory(pData);
	}

	public static void UnInitialize() {
		Pointer pData = library.UnInitialize();
		if (pData != null) {
			String error = pData.getString(0);
			library.FreeMemory(pData);
			throw new RuntimeException(error);
		}
	}

	
}

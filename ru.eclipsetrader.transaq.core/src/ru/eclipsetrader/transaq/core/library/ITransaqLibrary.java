package ru.eclipsetrader.transaq.core.library;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface ITransaqLibrary extends Library {
    
    // BYTE* Initialize(const BYTE* logPath, int logLevel)
    Pointer Initialize(String path, int level);
    
    // BYTE* SetLogLevel(int logLevel)
    Pointer SetLogLevel(int level);
    
    // BYTE* SendCommand(BYTE* pData)
    Pointer SendCommand(String data);
    
    //bool SetCallback(tcallback pCallback);
    boolean SetCallback(Callback pCallback);
    
    //bool SetCallbackEx(tcallbackEx pCallbackEx, void* userData);
    boolean SetCallbackEx(Callback pCallbackEx);
    
    // bool FreeMemory(BYTE* pData);
    boolean FreeMemory(Pointer pData);
    
    // BYTE* UnInitialize()
    Pointer UnInitialize(); 
    
}
package com.wada.gesprek.service;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NsdHelper {

    Context context;

    private NsdManager nsdManager;
    private ResolveListener resolveListener;
    private DiscoveryListener discoveryListener;
    private RegistrationListener registrationListener;
    private boolean discoveryStarted = false;
    private boolean serviceRegistered = false;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public String serviceName = "Gesprek";
    
    private int portaConectada;
    private InetAddress hostConectado;

    NsdServiceInfo mService;
    private InetAddress myIP;

    public NsdHelper(Context context) {
        this.context = context;
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        byte[] ipBytes = BigInteger.valueOf(ip).toByteArray();
        try {
			myIP = InetAddress.getByAddress(ipBytes);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void initializeNsd() {
    	initializeRegistrationListener();
        initializeResolveListener();
        initializeDiscoveryListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                discoveryStarted = true;
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getHost() == null) {
                    Log.d(TAG, "Same machine: " + serviceName);
                } else if (service.getServiceName().contains(serviceName)){
                	nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (service.getHost() == null) {
                	mService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                discoveryStarted = false;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getHost().equals(myIP)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                portaConectada = serviceInfo.getPort();
                hostConectado = serviceInfo.getHost();
            }
        };
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            	serviceRegistered = true;
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            	serviceRegistered = false;
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
            
        };
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        
        this.nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        
    }

    public void discoverServices() {
    	if (discoveryStarted) {
    		this.nsdManager.stopServiceDiscovery(discoveryListener);
    	}
        this.nsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }
    
    public int getPortaConectada() {
		return portaConectada;
	}

	public InetAddress getHostConectado() {
		return hostConectado;
	}

	public DiscoveryListener getDiscoveryListener() {
		return discoveryListener;
	}

	public RegistrationListener getRegistrationListener() {
		return registrationListener;
	}

	public void stopDiscovery() {
        this.nsdManager.stopServiceDiscovery(this.discoveryListener);
    }
    
    public void unregisterService() {
    	this.nsdManager.unregisterService(this.registrationListener);
    }
    
    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }
    
    public void tearDown() {
    	if (registrationListener != null) {
    		if (serviceRegistered) {
    			this.nsdManager.unregisterService(registrationListener);
    		}
    	}
    	if (discoveryListener != null) {
    		if (discoveryStarted) {
    			this.nsdManager.stopServiceDiscovery(discoveryListener);
    		}
    	}
    }
}

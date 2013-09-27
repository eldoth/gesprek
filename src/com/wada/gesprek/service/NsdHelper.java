package com.wada.gesprek.service;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.wada.gesprek.activity.Buscador;
import com.wada.gesprek.core.Contato;
import com.wada.gesprek.core.Usuario;

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
    private InetAddress myIP;
    private Buscador buscador;
    private Set<Contato> contatos;
    
    NsdServiceInfo mService;

    public NsdHelper(Context context, Buscador buscador) {
        this.context = context;
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.buscador = buscador;
        this.contatos = new HashSet<Contato>();
    }
    
    public void initializeServer() {
    	initializeRegistrationListener();
    }

    public void initializeClient() {
    	initializeResolveListener();
        initializeDiscoveryListener();
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
                } else if (service.getServiceName().contains(serviceName) && !service.getServiceName().contains(Usuario.getInstance().getNome())){
                	if (!containsService(service)) {
                		Contato c = new Contato(service);
                		getContatos().add(c);
                		buscador.addContato(c);
                	}
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (containsService(service)) {
                	removerContato(service);
                	buscador.removeContato(new Contato(service));
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
//                if (isNovoServico(serviceInfo)) {
//                	buscador.addContato(new Contato(serviceInfo));
//                	getServicosNovos().add(serviceInfo.getServiceName());
//                } else {
//                	getServicosNovos().remove(serviceInfo.getServiceName());
//                	buscador.removeServico(new Contato(serviceInfo));
//                }
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
            	System.out.println("Blah!");
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

    public void registerService(int port, InetAddress myIp) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(this.serviceName + ";"+Usuario.getInstance().getNome());
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setHost(myIp);
        
        this.nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        
    }

    public void discoverServices() {
//    	if (discoveryStarted) {
//    		this.nsdManager.stopServiceDiscovery(discoveryListener);
//    	}
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
    
    public void setMyIP(InetAddress myIP) {
		this.myIP = myIP;
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

	public Set<Contato> getContatos() {
		return contatos;
	}

	public void setContatos(Set<Contato> contatos) {
		this.contatos = contatos;
	}

	private boolean containsService(NsdServiceInfo nsdServiceInfo) {
		for (Contato contato : contatos) {
			if (contato.getNsdServiceInfo().getServiceName().equals(nsdServiceInfo.getServiceName())) {
				return true;
			}
		}
		return false;
	}
	
	private void removerContato(NsdServiceInfo service) {
		Set<Contato> contatos = new HashSet<Contato>(this.getContatos());
		for (Contato c : contatos) {
			if (c.getNsdServiceInfo().equals(service)) {
				this.getContatos().remove(c);
			}
		}
	}
	
}

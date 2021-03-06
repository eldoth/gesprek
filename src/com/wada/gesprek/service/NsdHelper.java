package com.wada.gesprek.service;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	private Socket socket;

	public static final String SERVICE_TYPE = "_http._tcp.";

	public static final String TAG = "NsdHelper";
	public String serviceName = "Ugesprek";

	private InetAddress myIP;
	
	//TODO: tirar o buscador daqui e tratar com handler
	private Buscador buscador;
	private List<Contato> contatos;

	private NsdServiceInfo myService;

	public NsdHelper(Context context, Buscador buscador) {
		this.context = context;
		this.nsdManager = (NsdManager) context
				.getSystemService(Context.NSD_SERVICE);
		this.buscador = buscador;
		this.contatos = new ArrayList<Contato>();
	}

	public void initializeServer() {
		initializeRegistrationListener();
	}

	public void initializeClient() {
		if (isDiscoveryStarted()) {
			this.nsdManager.stopServiceDiscovery(this.discoveryListener);
		}
		if (this.discoveryListener == null) {
			this.initializeDiscoveryListener();
		}
		
		initializeResolveListener();
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
					Log.d(TAG,
							"Unknown Service Type: " + service.getServiceType());
				} else if (service.getServiceName().contains(serviceName)) {
					nsdManager.resolveService(service, resolveListener);
				}
			}

			@Override
			public void onServiceLost(NsdServiceInfo service) {
				Log.e(TAG, "service lost" + service);
				manipulaContato(service, true);
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
			public void onResolveFailed(NsdServiceInfo serviceInfo,
					int errorCode) {
				Log.e(TAG, "Resolve failed" + errorCode);
			}

			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

				if (serviceInfo.getHost().equals(myIP)) {
					Log.d(TAG, "Same IP.");
					return;
				}
				if (!serviceInfo.getServiceName().contains(
						Usuario.getInstance().getNome())) {
					manipulaContato(serviceInfo, false);
				}
			}

		};
	}

	public void initializeRegistrationListener() {
		registrationListener = new NsdManager.RegistrationListener() {

			@Override
			public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
				serviceRegistered = true;
				Log.d(TAG, "Service registered");
			}

			@Override
			public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
				Log.d(TAG, "Failed to register service!");
			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo arg0) {
				serviceRegistered = false;
				Log.d(TAG, "Service Unregistered");
			}

			@Override
			public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
					int errorCode) {
			}

		};
	}

	public void registerService(int port, InetAddress myIp) {
		if (this.getChosenServiceInfo() != null) {
			this.unregisterService();
		}
			Log.d(TAG, "register service foi chamado");
			NsdServiceInfo serviceInfo = new NsdServiceInfo();
			serviceInfo.setPort(port);
			serviceInfo.setServiceName(this.serviceName + ";"
					+ Usuario.getInstance().getNome());
			serviceInfo.setServiceType(SERVICE_TYPE);
			serviceInfo.setHost(myIp);

			this.nsdManager.registerService(serviceInfo,
					NsdManager.PROTOCOL_DNS_SD, registrationListener);
			this.setChosenServiceInfo(serviceInfo);
			this.setServiceRegistered(true);

	}

	public void discoverServices() {
		this.nsdManager.discoverServices(SERVICE_TYPE,
				NsdManager.PROTOCOL_DNS_SD, discoveryListener);
		this.setDiscoveryStarted(true);
	}

	public DiscoveryListener getDiscoveryListener() {
		return discoveryListener;
	}

	public RegistrationListener getRegistrationListener() {
		return registrationListener;
	}

	public void stopDiscovery() {
		this.nsdManager.stopServiceDiscovery(this.discoveryListener);
		this.setDiscoveryStarted(false);
	}

	public void unregisterService() {
		this.nsdManager.unregisterService(this.registrationListener);
		this.setChosenServiceInfo(null);
		this.setServiceRegistered(false);
	}

	public NsdServiceInfo getChosenServiceInfo() {
		return myService;
	}

	public void setChosenServiceInfo(NsdServiceInfo mService) {
		this.myService = mService;
	}

	public void setMyIP(InetAddress myIP) {
		this.myIP = myIP;
	}

	public void tearDown() {
		if (registrationListener != null && serviceRegistered) {
			this.nsdManager.unregisterService(registrationListener);
			this.serviceRegistered = false;
		}
		if (discoveryListener != null && discoveryStarted) {
			this.nsdManager.stopServiceDiscovery(discoveryListener);
			this.discoveryStarted = false;
		}
	}
	
	public List<Contato> getContatos() {
		return contatos;
	}

	public void setContatos(List<Contato> contatos) {
		this.contatos = contatos;
	}

	private void manipulaContato(NsdServiceInfo nsdServiceInfo,
			boolean tentaRemover) {
		boolean existeMesmoHost = false;
		boolean existeMesmoServiceName = false;
		for (Contato contato : contatos) {
			if (nsdServiceInfo.getHost() != null
					&& contato.getHost().equals(nsdServiceInfo.getHost())) {
				existeMesmoHost = true;
			}
			if (contato.getServiceName()
					.equals(nsdServiceInfo.getServiceName())) {
				existeMesmoServiceName = true;
			}
		}
		if (tentaRemover) {
			if (existeMesmoServiceName) {
				removerContato(nsdServiceInfo);
			}
		} else {
			if (existeMesmoHost && !existeMesmoServiceName) {
				atualizarContato(nsdServiceInfo);
			} else if (!existeMesmoHost && !existeMesmoServiceName) {
				adicionarContato(nsdServiceInfo);
			}
		}

	}

	private void adicionarContato(NsdServiceInfo nsdServiceInfo) {
		Contato c = new Contato(nsdServiceInfo);
		getContatos().add(c);
		buscador.addContato(c);
	}

	private void atualizarContato(NsdServiceInfo nsdServiceInfo) {
		Set<Contato> contatos = new HashSet<Contato>(this.getContatos());
		for (Contato c : contatos) {
			if (c.getHost().equals(nsdServiceInfo.getHost())) {
				Contato contatoAtualizado = c;
				contatoAtualizado.setNsdServiceInfo(nsdServiceInfo);
				this.getContatos().set(this.getContatos().indexOf(c),
						contatoAtualizado);
			}
		}
	}

	private void removerContato(NsdServiceInfo nsdServiceInfo) {
		Set<Contato> contatos = new HashSet<Contato>(this.getContatos());
		for (Contato c : contatos) {
			if (c.getServiceName().equals(nsdServiceInfo.getServiceName())) {
				this.getContatos().remove(c);
				buscador.removeContato(nsdServiceInfo);
			}
		}
	}

	public boolean isDiscoveryStarted() {
		return discoveryStarted;
	}

	public void setDiscoveryStarted(boolean discoveryStarted) {
		this.discoveryStarted = discoveryStarted;
	}

	public boolean isServiceRegistered() {
		return serviceRegistered;
	}

	public void setServiceRegistered(boolean serviceRegistered) {
		this.serviceRegistered = serviceRegistered;
	}

	public Socket getSocket() {
		return socket;
	}

}

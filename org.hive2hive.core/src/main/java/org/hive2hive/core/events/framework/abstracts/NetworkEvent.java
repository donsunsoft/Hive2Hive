package org.hive2hive.core.events.framework.abstracts;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.events.framework.interfaces.INetworkEvent;

public abstract class NetworkEvent implements INetworkEvent {

	private final INetworkConfiguration networkConfiguration;
	private final IFileConfiguration fileConfiguration;

	public NetworkEvent(INetworkConfiguration networkConfiguration, IFileConfiguration fileConfiguraiton) {
		this.networkConfiguration = networkConfiguration;
		this.fileConfiguration = fileConfiguraiton;
	}
	
	@Override
	public INetworkConfiguration getNetworkConfiguration() {
		return networkConfiguration;
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}
}

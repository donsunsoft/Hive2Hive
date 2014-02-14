package org.hive2hive.core.network.data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.futures.FutureGetListener;
import org.hive2hive.core.network.data.futures.FuturePutListener;
import org.hive2hive.core.network.data.futures.FutureRemoveListener;

public class DataManager implements IDataManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DataManager.class);

	private final NetworkManager networkManager;

	public DataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	/**
	 * Helper to get the <code>TomP2P</code> peer.
	 * 
	 * @return the current peer
	 */
	private Peer getPeer() {
		return networkManager.getConnection().getPeer();
	}

	@Override
	public boolean put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);
		FuturePut putFuture = put(lKey, dKey, cKey, content, protectionKey);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(lKey, dKey, cKey, content, protectionKey, this);
		putFuture.addListener(listener);
		return listener.await();
	}

	@Override
	public boolean putUserProfileTask(String userId, Number160 contentKey, NetworkContent content,
			KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(userId);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);
		FuturePut putFuture = put(lKey, dKey, contentKey, content, protectionKey);
		if (putFuture == null) {
			return false;
		}

		FuturePutListener listener = new FuturePutListener(lKey, dKey, contentKey, content, protectionKey,
				this);
		putFuture.addListener(listener);
		return listener.await();
	}

	/**
	 * Putting without changing the protection key (normal case)
	 */
	public FuturePut put(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			NetworkContent content, KeyPair protectionKey) {
		// hand over the same protection key twice
		return put(locationKey, domainKey, contentKey, content, protectionKey, protectionKey);
	}

	/**
	 * Putting and changing the protection key (only used for sharing, ...). The old and the new protection
	 * keys can be the same, meaning that the key is not changed
	 */
	public FuturePut put(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			NetworkContent content, KeyPair oldProtectionKey, KeyPair newProtectionKey) {
		logger.debug(String
				.format("put content = '%s' location key = '%s' domain key = '%s' content key = '%s' version key = '%s' protected = '%b' ttl = '%s'",
						content.getClass().getSimpleName(), locationKey, domainKey, contentKey,
						content.getVersionKey(), newProtectionKey != null, content.getTimeToLive()));
		try {
			Data data = new Data(content);
			data.ttlSeconds(content.getTimeToLive()).basedOn(content.getBasedOnKey());
			if (newProtectionKey == null) {
				// the content won't be protected after this put
				if (oldProtectionKey == null) {
					// previous content was not protected either
					return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
							.setVersionKey(content.getVersionKey()).start();
				} else {
					// previous content was protected, now it should be unprotected.
					// TODO TomP2P does not support this yet
					return null;
				}
			} else {
				// the content will be protected after this put
				if (oldProtectionKey == null) {
					// the content is protected for the first time
					data.setProtectedEntry().sign(newProtectionKey);
					return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
							.setVersionKey(content.getVersionKey()).keyPair(newProtectionKey).start();
				} else {
					// change the protection keys
					data.setProtectedEntry().sign(newProtectionKey);
					return getPeer().put(locationKey).setData(contentKey, data).setDomainKey(domainKey)
							.setVersionKey(content.getVersionKey()).keyPair(oldProtectionKey).start();
				}
			}
		} catch (IOException | InvalidKeyException | SignatureException e) {
			logger.error(String
					.format("Put failed. location key = '%s' domain key = '%s' content key = '%s' version key = '%s' exception = '%s'",
							locationKey, domainKey, contentKey, content.getVersionKey(), e.getMessage()));
			return null;
		}
	}

	@Override
	public NetworkContent get(String locationKey, String contentKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureGet futureGet = get(lKey, dKey, cKey);
		FutureGetListener listener = new FutureGetListener(lKey, dKey, cKey, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public NetworkContent get(String locationKey, String contentKey, Number160 versionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureGet futureGet = get(lKey, dKey, cKey, versionKey);
		FutureGetListener listener = new FutureGetListener(lKey, dKey, cKey, versionKey, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	@Override
	public NetworkContent getUserProfileTask(String userId) {
		Number160 lKey = Number160.createHash(userId);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);

		FutureGet futureGet = getPeer().get(lKey)
				.from(new Number640(lKey, dKey, Number160.ZERO, Number160.ZERO))
				.to(new Number640(lKey, dKey, Number160.MAX_VALUE, Number160.MAX_VALUE)).ascending()
				.returnNr(1).start();
		FutureGetListener listener = new FutureGetListener(lKey, dKey, this);
		futureGet.addListener(listener);
		return listener.awaitAndGet();
	}

	public FutureGet get(Number160 locationKey, Number160 domainKey, Number160 contentKey) {
		logger.debug(String.format("get location key = '%s' domain key = '%s' content key = '%s'",
				locationKey, domainKey, contentKey));
		return getPeer().get(locationKey)
				.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO))
				.to(new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE)).descending()
				.returnNr(1).start();
	}

	public FutureGet get(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey) {
		logger.debug(String.format(
				"get location key = '%s' domain Key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, versionKey));
		return getPeer().get(locationKey).setDomainKey(domainKey).setContentKey(contentKey)
				.setVersionKey(versionKey).start();
	}

	@Override
	public boolean remove(String locationKey, String contentKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureRemove futureRemove = remove(lKey, dKey, cKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, cKey, protectionKey, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	@Override
	public boolean remove(String locationKey, String contentKey, Number160 versionKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(locationKey);
		Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
		Number160 cKey = Number160.createHash(contentKey);

		FutureRemove futureRemove = remove(lKey, dKey, cKey, versionKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, cKey, versionKey, protectionKey,
				this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	@Override
	public boolean removeUserProfileTask(String userId, Number160 contentKey, KeyPair protectionKey) {
		Number160 lKey = Number160.createHash(userId);
		Number160 dKey = Number160.createHash(H2HConstants.USER_PROFILE_TASK_DOMAIN);

		FutureRemove futureRemove = remove(lKey, dKey, contentKey, protectionKey);
		FutureRemoveListener listener = new FutureRemoveListener(lKey, dKey, contentKey, protectionKey, this);
		futureRemove.addListener(listener);
		return listener.await();
	}

	public FutureRemove remove(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			KeyPair protectionKey) {
		logger.debug(String.format("remove location key = '%s' domain key = '%s' content key = '%s'",
				locationKey, domainKey, contentKey));
		return getPeer().remove(locationKey)
				.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO))
				.to(new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE))
				.keyPair(protectionKey).start();
	}

	public FutureRemove remove(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey, KeyPair protectionKey) {
		logger.debug(String.format(
				"remove location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, versionKey));
		return getPeer().remove(locationKey).setDomainKey(domainKey).contentKey(contentKey)
				.setVersionKey(versionKey).keyPair(protectionKey).start();
	}

	public DigestBuilder getDigest(Number160 locationKey) {
		return getPeer().digest(locationKey);
	}

}

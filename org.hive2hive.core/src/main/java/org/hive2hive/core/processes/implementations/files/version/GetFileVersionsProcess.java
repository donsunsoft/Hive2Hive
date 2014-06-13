package org.hive2hive.core.processes.implementations.files.version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.interfaces.IProcessResultListener;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.common.File2MetaFileComponent;
import org.hive2hive.core.processes.implementations.context.RecoverFileContext;

public class GetFileVersionsProcess extends SequentialProcess implements IResultProcessComponent<List<IFileVersion>> {

	private final List<IProcessResultListener<List<IFileVersion>>> listeners = new ArrayList<IProcessResultListener<List<IFileVersion>>>();
	private final List<IFileVersion> result = new ArrayList<IFileVersion>();
	private final RecoverFileContext context;

	public GetFileVersionsProcess(File file, NetworkManager networkManager) throws NoSessionException,
			NoPeerConnectionException {
		context = new RecoverFileContext(file);
		add(new File2MetaFileComponent(file, context, context, networkManager));
	}

	@Override
	public List<IFileVersion> getResult() {
		return result;
	}

	@Override
	public void notifyResultComputed(List<IFileVersion> result) {
		for (IProcessResultListener<List<IFileVersion>> listener : listeners) {
			listener.onResultReady(result);
		}
	}

	@Override
	public void attachListener(IProcessResultListener<List<IFileVersion>> listener) {
		listeners.add(listener);
	}

	@Override
	public void detachListener(IProcessResultListener<List<IFileVersion>> listener) {
		listeners.remove(listener);
	}

	@Override
	protected void succeed() {
		collectResult();
		super.succeed();
		notifyResultComputed(getResult());
	}

	private void collectResult() {
		MetaFile metaFile = context.consumeMetaFile();
		if (metaFile instanceof MetaFileSmall) {
			MetaFileSmall metaFileSmall = (MetaFileSmall) metaFile;
			result.clear();
			for (FileVersion fileVersion : metaFileSmall.getVersions()) {
				result.add(fileVersion);
			}
		}
	}

}

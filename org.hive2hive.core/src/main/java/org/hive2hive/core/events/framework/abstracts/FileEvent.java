package org.hive2hive.core.events.framework.abstracts;

import java.io.File;

import org.hive2hive.core.events.framework.interfaces.IFileEvent;

public abstract class FileEvent implements IFileEvent {

	private final File file;

	public FileEvent(File file) {
		this.file = file;
	}
	
	@Override
	public File getFile() {
		return file;
	}

}

package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.IEventGenerator;

public interface IFileEventGenerator extends IEventGenerator {

	void addEventListener(IFileEventListener listener);

	void removeEventListener(IFileEventListener listener);
	
}

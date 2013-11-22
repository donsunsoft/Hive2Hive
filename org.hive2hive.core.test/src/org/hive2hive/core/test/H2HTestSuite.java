package org.hive2hive.core.test;

import org.hive2hive.core.test.file.FileManagerTest;
import org.hive2hive.core.test.file.FileSynchronizerTest;
import org.hive2hive.core.test.model.FileTreeNodeTest;
import org.hive2hive.core.test.model.MetaFileTest;
import org.hive2hive.core.test.model.UserCredentialsTest;
import org.hive2hive.core.test.network.ConnectionTest;
import org.hive2hive.core.test.network.data.DataManagerTest;
import org.hive2hive.core.test.network.messages.BaseMessageTest;
import org.hive2hive.core.test.network.messages.BaseRequestMessageTest;
import org.hive2hive.core.test.process.ProcessTest;
import org.hive2hive.core.test.process.common.get.BaseGetProcessStepTest;
import org.hive2hive.core.test.process.common.get.GetLocationStepTest;
import org.hive2hive.core.test.process.common.get.GetUserProfileStepTest;
import org.hive2hive.core.test.process.common.massages.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.test.process.common.massages.BaseMessageProcessStepTest;
import org.hive2hive.core.test.process.common.put.BasePutProcessStepTest;
import org.hive2hive.core.test.process.common.put.PutLocationStepTest;
import org.hive2hive.core.test.process.common.put.PutUserProfileStepTest;
import org.hive2hive.core.test.process.common.remove.BaseRemoveProcessStepTest;
import org.hive2hive.core.test.process.download.DownloadFileTest;
import org.hive2hive.core.test.process.files.NewFileTest;
import org.hive2hive.core.test.process.manager.ProcessManagerTest;
import org.hive2hive.core.test.process.register.RegisterTest;
import org.hive2hive.core.test.tomp2p.ReplicationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of <code>hive2hive</code>.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({
// TomP2P
		ReplicationTest.class,

		// Network
		ConnectionTest.class, DataManagerTest.class, BaseMessageTest.class, BaseRequestMessageTest.class,

		// Processes
		ProcessTest.class, ProcessManagerTest.class,

		// ProcessStep, Common, Get
		BaseGetProcessStepTest.class, GetLocationStepTest.class, GetUserProfileStepTest.class,

		// ProcessStep, Common, Put
		BasePutProcessStepTest.class, PutLocationStepTest.class, PutUserProfileStepTest.class,

		// ProcessStep, Common, Remove
		BaseRemoveProcessStepTest.class,

		// ProcessStep, Common, Message
		BaseMessageProcessStepTest.class, BaseDirectMessageProcessStepTest.class,

		// Process: Register
		RegisterTest.class,

		// Process: Upload
		NewFileTest.class,

		// Process: Download
		DownloadFileTest.class,

		// Files
		FileManagerTest.class, FileSynchronizerTest.class,

		// Model
		FileTreeNodeTest.class, UserCredentialsTest.class, MetaFileTest.class

})
public class H2HTestSuite {

}

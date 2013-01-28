package com.goodformobile.build.mobile;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class RIMCleanupMojoTest extends AbstractRIMMojoTest<RIMCleanupMojo>{

	@Test
	public void testExecuteOnBasicApplication() throws Exception {

		File projectDirectory = getRelativeFile("projects/test-large-app-rapced/pom.xml").getParentFile();
		File workProjectDirectory = setupCleanCopyOfProject(projectDirectory);

		RIMCleanupMojo mojo = setupMojo();

		MavenProject project = getProject(mojo);

		setupProject(workProjectDirectory, project);

		File targetDirectory = new File(workProjectDirectory, "target");
		File expectedCod = new File(targetDirectory.getAbsoluteFile() + File.separator + "classes" + File.separator + "test_app_rapced.cod");
		assertTrue("Generated cod should be exist before clean: " + expectedCod.getAbsolutePath(), expectedCod.exists());
		
		
		mojo.execute();


		// Ensure a cod file is deleted.
		assertFalse("Generated cod should be deleted: " + expectedCod.getAbsolutePath(), expectedCod.exists());
	}


	@Override
	protected String getPluginConfigFileName() {
		return "com/goodformobile/build/mobile/execute-rapc-plugin-config.xml";
	}

	@Override
	protected String getGoal() {
		return "rim-cleanup";
	}

}

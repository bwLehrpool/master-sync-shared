package org.openslx.virtualization.configuration.container;

public enum ContainerBuildContextMethod {

	FILE, GIT_REPOSITORY,IMAGE_REPO;

	public static ContainerBuildContextMethod fromInt(int index) {
		return values()[index];
	}
}

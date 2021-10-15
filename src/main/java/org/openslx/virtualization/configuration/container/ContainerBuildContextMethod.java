package org.openslx.virtualization.configuration.container;

public enum ContainerBuildContextMethod {

	FILE, GIT_REPOSITORY, IMAGE_REPO, DOCKER_TAR;

	public static ContainerBuildContextMethod fromInt(int index) {
		return values()[index];
	}
}

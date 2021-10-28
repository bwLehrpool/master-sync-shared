package org.openslx.virtualization.configuration.container;

public enum ContainerImageContext {

	DOCKERFILE, GIT_REPOSITORY, IMAGE_REPOSITORY, DOCKER_ARCHIVE;

	public static ContainerImageContext fromInt(int index) {
		return values()[index];
	}
}

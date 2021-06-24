package org.openslx.virtualization.configuration.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ContainerMeta is used to store container specific information.
 * An object of this class will be serialized with gson to a json file.
 * <p>
 * TODO remove build_context_method
 * no need to distinguish between methods
 * TODO rename build_context_url to build_context
 */
public class ContainerMeta {

	public enum ContainerImageType implements org.apache.thrift.TEnum {
		LECTURE("Lecture"), BATCH("Batch"), DATA("Data");

		private final String displayLable;

		ContainerImageType(String name) {
			this.displayLable = name;
		}

		public boolean equalNames(String other) {
			return displayLable.equals(other);
		}

		@Override public String toString() {
			return this.displayLable;
		}

		@Override public int getValue() {
			return this.ordinal();
		}
	}

	private int build_context_method;
	private String image_repo;
	private String build_context_url;
	private String image_name;
	private String run_options;
	private String run_command;
	private String image_type;
	private List<ContainerBindMount> bind_mount_config = new ArrayList<>();

	public ContainerMeta() {

		image_repo = "";
		build_context_method = ContainerBuildContextMethod.FILE.ordinal();
		build_context_url = "";
		image_name = "";
		run_options = "";
		run_command = "";
		image_type = ContainerImageType.LECTURE.toString();
		bind_mount_config = new ArrayList<>();
	}

	public ContainerMeta(ContainerMeta containerMeta) {
		build_context_method = containerMeta.build_context_method;
		build_context_url = containerMeta.build_context_url;
		image_name = containerMeta.image_name;
		run_options = containerMeta.run_options;
		run_command = containerMeta.run_command;
		image_repo = containerMeta.image_repo;

		for (ContainerBindMount bm : containerMeta.bind_mount_config)
			bind_mount_config.add(new ContainerBindMount(bm.getSource(), bm.getTarget(), bm.getOptions()));

	}

	public int getBuildContextMethod() {
		return build_context_method;
	}

	public void setBuildContextMethod(int buildContextMethod) {
		this.build_context_method = buildContextMethod;
	}

	public String getBuildContextUrl() {
		return build_context_url;
	}

	public void setBuildContextUrl(String buildContextUrl) {
		this.build_context_url = buildContextUrl;
	}

	public String getRunOptions() {
		return run_options;
	}

	public void setRunOptions(String run_options) {
		this.run_options = run_options;
	}

	public void setRunCommand(String run_command) {
		this.run_command = run_command;
	}

	public String getRunCommand() {
		return this.run_command;
	}

	public String getImageName() {
		return image_name;
	}

	public void setImageName(String image_name) {
		this.image_name = image_name;
	}

	public List<ContainerBindMount> getBindMountConfig() {
		return bind_mount_config;
	}

	public void setBindMountConfig(List<ContainerBindMount> bindMountConfig) {
		this.bind_mount_config = bindMountConfig;
	}

	public String getImageRepo() {
		return image_repo;
	}

	public void setImageRepo(String from_image) {
		this.image_repo = from_image;
	}

	public ContainerImageType getImageType() {
		if (image_type == null || image_type.length() == 0)
			return ContainerImageType.LECTURE;

		// turn string representation into enum-var 'LECTURE' -> ContainerImageType.LECTURE
		return ContainerImageType.valueOf(image_type);
	}

	public void setImageType(ContainerImageType image_type) {
		// set constant representation of the enum-var e.g. ContainerImageType.LECTURE -> 'LECTURE'
		this.image_type = image_type.name();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ContainerMeta that = (ContainerMeta) o;
		return Objects.equals(build_context_url, that.build_context_url) && Objects.equals(image_name,
				that.image_name) && Objects.equals(run_options, that.run_options) && Objects.equals(
				run_command, that.run_command) && Objects.equals(bind_mount_config, that.bind_mount_config)
				&& Objects.equals(image_repo, that.image_repo) && Objects.equals(image_type, that.image_type);
	}

	@Override public int hashCode() {
		return Objects.hash(build_context_url, image_name, run_options, run_command, bind_mount_config,
				image_repo, image_type);
	}
}

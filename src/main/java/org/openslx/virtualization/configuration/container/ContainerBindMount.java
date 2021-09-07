package org.openslx.virtualization.configuration.container;

import java.util.Objects;

/**
 * This class implements a model for a bind mount entry in the docker context
 * (eg. docker run  ... --mount type=bind,source=source,target=target,options ... ). A list of objects of this class is stored in
 * {@link ContainerMeta}.
 */
public class ContainerBindMount {

	public enum ContainerMountType {
		DEFAULT,
		CONTAINER_IMAGE
	}

	private ContainerMountType mount_type = ContainerMountType.DEFAULT;
	private String source = "";
	private String target = "";
	private String options = "";

	public ContainerBindMount() {
	}

	public ContainerBindMount(String source, String target, String options) {
		this(ContainerMountType.DEFAULT,source,target,options);
	}

	public ContainerBindMount(ContainerMountType mount_type, String source, String target, String options) {
		this.mount_type = mount_type;
		this.source = source;
		this.target = target;
		this.options = options;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public ContainerMountType getMountType() {
		return this.mount_type;
	}


	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ContainerBindMount that = (ContainerBindMount) o;
		return Objects.equals(source, that.source) && Objects.equals(mount_type, that.mount_type)
				&& Objects.equals(target, that.target) && Objects.equals(options, that.options);
	}

	@Override public int hashCode() {
		return Objects.hash(source, target, options);
	}
}

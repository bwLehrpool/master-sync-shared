package org.openslx.virtualization.configuration.container;

import java.util.Objects;

/**
 * This class implements a model for a bind mount entry in the docker context
 * (eg. docker run  ... --mount type=bind,source=source,target=target,options ... ). A list of objects of this class is stored in
 * {@link ContainerMeta}.
 */
public class ContainerBindMount {

	private String source;
	private String target;
	private String options;

	public ContainerBindMount() {
	}

	public ContainerBindMount(String source, String target, String options) {
		this.source = source;
		this.target = target;
		this.options = options;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ContainerBindMount that = (ContainerBindMount) o;
		return Objects.equals(source, that.source) && Objects.equals(target, that.target) && Objects.equals(
				options, that.options);
	}

	@Override public int hashCode() {
		return Objects.hash(source, target, options);
	}
}

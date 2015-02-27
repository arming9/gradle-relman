package com.github.gradle.plugins.relman;

public class VersionedArtifactName implements Comparable<VersionedArtifactName> {

	private UnversionedArtifactName unversionedArtifactName;
	private String version;

	public VersionedArtifactName(
			UnversionedArtifactName unversionedArtifactName, String version) {
		this.unversionedArtifactName = unversionedArtifactName;
		this.version = version;
	}

	public VersionedArtifactName(String groupId, String artifactId,
			String version) {
		this.unversionedArtifactName = new UnversionedArtifactName(groupId,
				artifactId);
		this.version = version;
	}

	public UnversionedArtifactName getUnversionedArtifactName() {
		return unversionedArtifactName;
	}

	public String getVersion() {
		return version;
	}

	public String getGroupId() {
		return unversionedArtifactName.getGroupId();
	}

	public String getArtifactId() {
		return unversionedArtifactName.getArtifactId();
	}

	public String getCompleteGradleDependencyString() {
		return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
	}
	
	@Override
	public String toString() {
		return getCompleteGradleDependencyString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((unversionedArtifactName == null) ? 0
						: unversionedArtifactName.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionedArtifactName other = (VersionedArtifactName) obj;
		if (unversionedArtifactName == null) {
			if (other.unversionedArtifactName != null)
				return false;
		} else if (!unversionedArtifactName
				.equals(other.unversionedArtifactName))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public int compareTo(VersionedArtifactName o) {
		int retVal = this.unversionedArtifactName.compareTo(o.unversionedArtifactName);
		if(0 == retVal) {
			retVal = this.version.compareTo(o.version);
		}
		return retVal;
	}

}

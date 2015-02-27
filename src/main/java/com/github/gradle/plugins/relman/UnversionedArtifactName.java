package com.github.gradle.plugins.relman;


public class UnversionedArtifactName implements
		Comparable<UnversionedArtifactName> {

	private String groupId;
	private String artifactId;

	public UnversionedArtifactName(String groupId, String artifactId) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
		UnversionedArtifactName other = (UnversionedArtifactName) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		return true;
	}

	public String toString() {
		return groupId + ":" + artifactId;
	}

	public int compareTo(UnversionedArtifactName o) {
		int retVal = this.groupId.compareTo(o.groupId);
		if(0==retVal){
			retVal = this.artifactId.compareTo(o.artifactId);
		}
		return retVal;
	}
}

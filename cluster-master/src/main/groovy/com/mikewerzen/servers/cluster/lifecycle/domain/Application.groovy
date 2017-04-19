package com.mikewerzen.servers.cluster.lifecycle.domain

class Application {

	String name;
	String version;
	String command;



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (!obj)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Application other = (Application) obj;
		if (!name) {
			if (!other.name)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!version) {
			if (!other.version)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Application [name=" + name + ", version=" + version + ", command=" + command + "]";
	}
}

package com.mikewerzen.servers.cluster.lifecycle.domain

class Slave {
	String name;
	double load;
	Set<Application> appsDeployed = new HashSet<Application>();
	Date lastUpdate;

	public List<Application> getAllDeployedVersionsOfApplication(Application application) {
		return appsDeployed.stream().filter({app -> app.name == application.name}).collect();
	}

	public List<Application> getDeployedApplication(Application application) {
		return appsDeployed.stream().filter({app -> app == application}).collect();
	}

	public String isRunningThisVersionOfApplication(Application application) {
		Application version = appsDeployed.find({app -> app.equals(application)});
		return (version) ? version.name : null;
	}

	public String isRunningAnotherVersionOfApplication(Application application) {
		Application version = appsDeployed.find({app -> app.name == application.name && app.version != application.version});
		return (version) ? version.name : null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!obj)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slave other = (Slave) obj;
		if (!name) {
			if (!other.name)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Slave [name=" + name + ", load=" + load + ", lastUpdate=" + lastUpdate + ", appsDeployed=" + appsDeployed.toString() + "]";
	}
}

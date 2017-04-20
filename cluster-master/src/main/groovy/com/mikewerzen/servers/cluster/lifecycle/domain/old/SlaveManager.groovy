package com.mikewerzen.servers.cluster.lifecycle.domain.old

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.mikewerzen.servers.cluster.lifecycle.domain.SlaveMessenger
import com.sun.corba.se.impl.io.ObjectStreamClass.CompareObjStrFieldsByName

//@Component
class SlaveManager {

//	@Autowired
	SlaveMessenger messenger;

	Set<Slave> slavesInCluster = new HashSet<Slave>();

	Map<Slave, Application> lastUndeploy = new HashMap<Slave, Application>();

	public Slave getSlaveForDeployment() {
		return slavesInCluster.iterator().min({x, y -> x.load <=> y.load});
	}

	public boolean deployToSlave(Application application) {

		Slave slaveForDeployment = getSlaveForDeployment();
		if (slaveForDeployment) {
			findSlavesRunningOtherVersionOfApplication(application).each{slave -> triggerUndeployEvent(application, slave)};

			messenger.sendDeployCommand(application, slaveForDeployment);
		}
	}

	public boolean undeployThisVersionFromAllSlaves(Application application) {
		slavesInCluster.each{slave -> undeployThisVersionFromSlave(application, slave)}
	}

	public boolean undeployAllVersionsFromAllSlaves(Application application) {
		slavesInCluster.each{slave -> undeployAllVersionsFromSlave(application, slave)}
	}

	public void rebootSlave(Slave slave) {
		slave.load = Double.MAX_VALUE;
		slave.appsDeployed.each {app -> deployToSlave(app)};
		messenger.sendRebootCommand(slave);
	}

	public void cleanOldSlaves() {
		Date oneMinuteAgo = getCurrentDateMinusNSeconds(60);

		def oldSlaves = slavesInCluster.stream().filter({slave -> slave.lastUpdate.before(oneMinuteAgo)}).collect();
		slavesInCluster.removeAll(oldSlaves);
		oldSlaves.each { slave -> this.lastUndeploy.remove(slave)}
		oldSlaves.each {slave -> this.rebootSlave(slave) };
	}

	public void triggerUndeployEvent(Application app, Slave slave) {
		slave.appsDeployed.remove(app);
		lastUndeploy.put(slave, app);
		messenger.sendUndeployCommand(app, slave);
	}


	public void undeployAllVersionsFromSlave(Application application, Slave slave) {
		slave.getAllDeployedVersionsOfApplication(application).each{app -> triggerUndeployEvent(app, slave)};
	}

	public void undeployThisVersionFromSlave(Application application, Slave slave) {
		slave.getDeployedApplication(application).each{app -> triggerUndeployEvent(app, slave)};
	}

	public List<Slave> findSlavesRunningSameVersionOfApplication(Application application) {
		return slavesInCluster.stream().filter({ slave -> slave.isRunningThisVersionOfApplication(application) != null}).collect();
	}

	public List<Slave> findSlavesRunningOtherVersionOfApplication(Application application) {
		return slavesInCluster.stream().filter({ slave -> slave.isRunningAnotherVersionOfApplication(application) != null}).collect();
	}

	public Set<Application> getAllDeployedApplications() {
		Set<Application> allApps = new HashSet<Application>();
		slavesInCluster.each{slave -> allApps.addAll(slave.appsDeployed)};
		return allApps;
	}

	public boolean handleSlaveUpdate(Slave updatedSlave) {
		Slave old = slavesInCluster.find({x-> x.equals(updatedSlave)})

		slavesInCluster.remove(old);
		slavesInCluster.add(updatedSlave);

		if(old) {
			getUndeployedApplications(old, updatedSlave).each {Application app -> this.deployToSlave(app)}
		}
	}

	private List<Application> getUndeployedApplications(Slave old, Slave updated) {
		List<Application> appsTerminated = old.appsDeployed.stream().filter({Application app -> !updated.appsDeployed.contains(app)}).collect();
		List<Application> undeployedApps = appsTerminated.stream().filter({Application app -> this.findSlavesRunningSameVersionOfApplication(app).isEmpty()}).collect()
		undeployedApps.remove(lastUndeploy.get(old));
		return undeployedApps;
	}

	@Override
	public String toString() {
		return "SlaveManager [slavesInCluster=" + slavesInCluster + "]";
	}

	private Date getCurrentDateMinusNSeconds(int n) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 0 - n);
		Date oneMinuteAgo = cal.getTime()
		return oneMinuteAgo
	}
}

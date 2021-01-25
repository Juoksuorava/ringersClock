package fi.utu.tech.ringersClock.entities;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * Entity class presenting a WakeUpGroup. The class is not complete.
 * You need to add some variables.
 */

public class WakeUpGroup implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private Integer ID;
	private Alarm alarm = null;
	private ArrayList<Integer> ports;

	public WakeUpGroup(Integer id, String name, Alarm alarm) {
		super();
		this.ID = id;
		this.name = name;
		this.alarm = alarm;
		ports = new ArrayList<Integer>();
		this.ports.add(id);
	}

	public void addUser(Integer port) { this.ports.add(port); }
	public void removeUser(Integer port) { this.ports.remove(port); }
	public boolean userExistsInGroup(Integer port) {
		for (Integer i: ports) {
			if (i == port) {
				return true;
			}
		}
		return false;
	}

	public String getName() { return this.name; }
	public void setName(String name) { this.name = name;}

	public Integer getID() { return this.ID; }
	public void setID(Integer ID) { this.ID = ID; }

	public Integer[] getPorts() {
		return this.ports.toArray(new Integer[this.ports.size()]);
	}

	public Alarm getAlarm() { return alarm; }
	public long getAlarmTime() {
		return alarm.getTime().toEpochMilli();
	}
	public void setAlarm(Alarm alarm) { this.alarm = alarm; }

	@Override
	public String toString() {
		return this.getName();
	}

}

package fi.utu.tech.ringersClockServer;

import fi.utu.tech.ringersClock.entities.ServerCall;
import fi.utu.tech.ringersClock.entities.ServerCallType;
import fi.utu.tech.ringersClock.entities.WakeUpGroup;
import fi.utu.tech.weatherInfo.FMIWeatherService;
import fi.utu.tech.weatherInfo.WeatherData;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class WakeUpService extends Thread {

	private ArrayBlockingQueue<WakeUpGroup> queue;
	private final int limit = 100;
	private ServerSocketListener ssl;
	private Map<Integer, WakeUpGroup> groups = Collections.synchronizedMap(new HashMap<Integer, WakeUpGroup>());

	public synchronized void addGroup(int ID, WakeUpGroup wug) {
		if(!groups.containsKey(ID)) {
			groups.put(ID, wug);
			addGroupToQueue(wug);
			updateExistingGroupsForAllUsers();
			ServerSocketListener.sendCallToClient(wug.getID(),
					new ServerCall<>(
							ServerCallType.STATUS_UPDATE,
							"Created new group :" + wug.getName()
					));
			ServerSocketListener.sendCallToClient(wug.getID(),
					new ServerCall<>(
							ServerCallType.ALARM_TIME_RESPONSE,
							wug.getAlarm().getTime()
					));
			System.out.println("Group + " + wug.getName() + " created by user " + ID + ".");
		} else {
			System.out.println("Group already exists, group founder: " + ID + ".");

		}
	}


	public synchronized void resignGroup(int groupID, int clientID) {
		WakeUpGroup group = groups.get(groupID);
		if (group.userExistsInGroup(clientID)) {
			group.removeUser(clientID);
			ServerSocketListener.sendCallToClient(clientID,
					new ServerCall<>(
							ServerCallType.ALARM_TIME_CANCELLED
					));
			ServerSocketListener.sendCallToClient(clientID,
					new ServerCall<>(
							ServerCallType.STATUS_UPDATE, "Removed from group: " + group.getName()
					));
		} else {
			System.out.println("No user " + clientID + " found in group " + group.getName() + ".");
		}
	}

	public synchronized void joinGroup(WakeUpGroup wug, int clientID) {
		WakeUpGroup group = groups.get(wug.getID());
		if (!group.userExistsInGroup(clientID)) {
			group.addUser(clientID);
			ServerSocketListener.sendCallToClient(clientID,
					new ServerCall<>(
						ServerCallType.STATUS_UPDATE, "Added to group: " + group.getName()
			));
			ServerSocketListener.sendCallToClient(clientID,
					new ServerCall<>(
							ServerCallType.ALARM_TIME_RESPONSE,
							wug.getAlarm().getTime()
					));
		} else {
			System.out.println("User " + clientID + " is already in group " + group.getID() + ".");
		}
	}

	public WakeUpService() {
		groups = Collections.synchronizedMap(new HashMap<Integer, WakeUpGroup>());
		queue = new ArrayBlockingQueue<WakeUpGroup>(limit);

	}

	private synchronized void updateExistingGroupsForAllUsers() {
		WakeUpGroup[] groupList = groups.values().toArray(new WakeUpGroup[groups.size()]);
		ServerSocketListener.sendCallToEveryClient(
				new ServerCall<WakeUpGroup[]>(
						ServerCallType.UPDATE_EXISTING_GROUPS, groupList)
		);
	}
	public void run() {
		while (true) {
			try {
				if (queue.size() == 0) {
					sleep(Integer.MAX_VALUE);
				} else {
					long wait = queue.peek().getAlarmTime() - Instant.now().toEpochMilli();
					sleep(wait);
					synchronized (queue) {
						while (queue.size() > 0 && Instant.now().toEpochMilli() >= queue.peek().getAlarmTime()) {
							var wug = queue.remove();
							triggerConfirm(wug);
						}
					}
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

	}

	private void triggerConfirm(WakeUpGroup wug) {
		try {
			WeatherData data = FMIWeatherService.getWeather(0);
			if (wug.getAlarm().weatherConfirm(data)) {
				ServerSocketListener.sendCallToClient(wug.getID(),
						new ServerCall<>(
								ServerCallType.CONFIRM_ALARM, wug
						));
			} else {
				removeGroup(wug);
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	public synchronized WakeUpGroup[] getGroups() {
		return groups.values().toArray((new WakeUpGroup[groups.size()]));
    }

	public void alarmAll(int port) {
		var wug = groups.get(port);
		if (wug != null) {
			var ports = wug.getPorts();
			ServerSocketListener.sendCallToMultipleClients(ports,
					new ServerCall<>(
							ServerCallType.ALARM_USER
					));
			removeGroup(wug);
		} else {
			System.out.println("User not leader in any active groups");
		}
	}

	public void cancelAlarm(int port) {
		var wug = groups.get(port);
		if (wug != null) {
			var ports = wug.getPorts();
			ServerSocketListener.sendCallToMultipleClients(ports,
					new ServerCall<>(
							ServerCallType.ALARM_TIME_CANCELLED
					));
			removeGroup(wug);
		} else {
			System.out.println("User not leader in any active groups");
		}
	}

	public void removeGroup(WakeUpGroup wug) {
		if (groups.containsKey(wug.getID())) {
			groups.remove(wug.getID());
			removeGroupFromQueue(wug);
			updateExistingGroupsForAllUsers();

			ServerSocketListener.sendCallToClient(wug.getID(),
					new ServerCall<>(
							ServerCallType.ALARM_TIME_CANCELLED
					));

			ServerSocketListener.sendCallToMultipleClients(wug.getPorts(),
					new ServerCall<>(
							ServerCallType.ALARM_TIME_CANCELLED
					));
		}
	}

	private void removeGroupFromQueue(WakeUpGroup wug) {
		synchronized (queue) {
			queue.clear();
			var array = queue.toArray();
			for (int i = 0; i < array.length; i++) {
				WakeUpGroup tempWug = (WakeUpGroup) array[i];
				if (wug.getID() == tempWug.getID()) {
					continue;
				}
				queue.add(tempWug);
			}
		}
		this.interrupt();
	}

	private void addGroupToQueue(WakeUpGroup wug) {
		synchronized (queue) {
			if (queue.size() == 0) {
				queue.add(wug);
			} else {
				queue.clear();
				var array = queue.toArray();
				boolean inQueue = false;
				for (int i = 0; i < array.length; i++) {
					var obj = (WakeUpGroup)array[i];
					if (!inQueue && wug.getAlarmTime() <= obj.getAlarmTime()) {
						queue.add(wug);
						inQueue = true;
					}
					queue.add(obj);
				}
			}
		}
		this.interrupt();
	}

}

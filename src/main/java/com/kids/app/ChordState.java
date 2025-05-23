package com.kids.app;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import com.kids.app.reliability.ServentActivityTracker;
import com.kids.app.reliability.ServentPulseManager;
import com.kids.app.servent.ServentIdentity;
import com.kids.app.servent.ServentInfo;
import com.kids.app.system.SystemManager;
import com.kids.file.FileData;
import com.kids.mutex.DistributedMutex;
import com.kids.mutex.SuzukiKasamiMutex;
import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.message.core.WelcomeMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * <p>
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
@Getter
public class ChordState {

	public static int CHORD_SIZE;
	@Getter private int chordLevel; // log_2(CHORD_SIZE)

	public static int chordHash(String value) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(value.getBytes());
			BigInteger hash = new BigInteger(messageDigest.digest());
			hash = hash.mod(BigInteger.valueOf(64));
			return hash.intValue();
		} catch (NoSuchAlgorithmException e) {
			return -1;
		}
	}

	@Getter private final DistributedMutex<ServentIdentity, SuzukiKasamiToken> mutex;
	@Getter private final SystemManager systemManager;
	@Getter private final ServentActivityTracker tracker;
	@Getter @Setter private ServentPulseManager pulseManager;

	@Setter private ServentInfo[] successorTable;
	@Setter private ServentInfo predecessorInfo;
	@Setter private Map<Integer, Map<String, FileData>> data;
	private final List<ServentInfo> allNodeInfo;
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			// Not power of two
			if (tmp % 2 != 0) {
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];

		IntStream.range(0, chordLevel)
				.forEach(i -> successorTable[i] = null);
		
		this.predecessorInfo = null;
		this.data = new HashMap<>();
		this.allNodeInfo = new CopyOnWriteArrayList<>();
		this.mutex = new SuzukiKasamiMutex(CHORD_SIZE, AppConfig.myServentInfo.getChordId());
		this.systemManager = new SystemManager(data, mutex, new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>());
		this.tracker = new ServentActivityTracker();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		// Set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort());
		this.data = welcomeMsg.getData();
		
		// Tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_ADDRESS, AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + "\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (Exception ignored) { }
	}

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) return true;
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();

		// No overflow
		if (predecessorChordId < myChordId) {
            return key <= myChordId && key > predecessorChordId;
		}
		// Overflow
		else return key <= myChordId || key > predecessorChordId;
    }
	
	/**
	 * Main chord operation - find the nearest node to hop to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) return AppConfig.myServentInfo;
		
		// Normally we start the search from our first successor
		int startInd = 0;
		
		/*
			If the key is smaller than us, and we are not the owner,
			then all nodes up to CHORD_SIZE will never be the owner,
			so we start the search from the first item in our table after CHORD_SIZE
			we know that such a node must exist, because otherwise we would own this key
		 */
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) return successorTable[i-1];
			// Overflow
			if (key > previousId && successorId < previousId) return successorTable[i-1];
			previousId = successorId;
		}
		// If we have only one node in all slots in the table, we might get here then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		// First node after me has to be successorTable[0]
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		// i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			// We are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			// This loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					// Before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						// Try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { // Node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					// Check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						// Try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		allNodeInfo.sort(Comparator.comparingInt(ServentInfo::getChordId));
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			}
			else newList.add(serventInfo);
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (!newList2.isEmpty()) {
			predecessorInfo = newList2.get(newList2.size()-1);
		}
		else predecessorInfo = newList.get(newList.size()-1);
		
		updateSuccessorTable();
	}

	/**
	 * This method removes the node from the list of all nodes and updates the successor table.
	 * It also resets the predecessor's timestamp, because we may have a new predecessor.
	 */
	public void removeNode(ServentInfo terminationNode) {
		for(int i = 0; i < allNodeInfo.size(); i++) {
			ServentInfo serventInfo = allNodeInfo.get(i);
			if (serventInfo.getIpAddress().equals(terminationNode.getIpAddress()) && serventInfo.getListenerPort() == terminationNode.getListenerPort()) {
				allNodeInfo.remove(i);
				break;
			}
		}

		allNodeInfo.sort(Comparator.comparingInt(ServentInfo::getChordId));

		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();

		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			}
			else newList.add(serventInfo);
		}

		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (!newList2.isEmpty()) {
			predecessorInfo = newList2.get(newList2.size()-1);
		}
		else if(!newList.isEmpty()) {
			predecessorInfo = newList.get(newList.size()-1);
		}
		// We are the only node in the system
		else {
			predecessorInfo = null;
			for (int i = 0; i < chordLevel; i++) {
				successorTable[i] = null;
			}
			return;
		}

		tracker.resetTimestamp();
		updateSuccessorTable();
	}

	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}

	public String getNextNodeIp() {
		return successorTable[0].getIpAddress();
	}

}

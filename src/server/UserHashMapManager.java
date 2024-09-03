package server;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import remote.WhiteBoardClientInterface;

public class UserHashMapManager implements Iterable<WhiteBoardClientInterface> {

	// clientManager maintains the HashMap -> Set<WhiteBoardClientInterface> of users
	private Set<WhiteBoardClientInterface> userHashMap;
	
	// constructor
	public UserHashMapManager(WhiteBoardServerServant serverServant) {
		this.userHashMap = Collections.newSetFromMap(new ConcurrentHashMap<WhiteBoardClientInterface, Boolean>());
	}
	
	
	// add user
	public void addUser(WhiteBoardClientInterface user) {
		this.userHashMap.add(user);
	}
	
	// remove user
	public void deleteUser(WhiteBoardClientInterface user) {
		this.userHashMap.remove(user);
	}
	
	// check if the user HashMap is empty
	public boolean isEmpty() {
		return userHashMap.size() == 0;
	}
	
	// getter for the userHashMap
	public Set<WhiteBoardClientInterface> getUserHashMap(){
		return this.userHashMap;
	}

	@Override
	public Iterator<WhiteBoardClientInterface> iterator() {
		return userHashMap.iterator();
	}

	
}

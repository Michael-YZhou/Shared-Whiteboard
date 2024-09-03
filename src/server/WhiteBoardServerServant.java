package server;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import client.WhiteBoardCanvas;
import remote.MessageTransmissionInterface;
import remote.WhiteBoardClientInterface;
import remote.WhiteBoardServerInterface;

public class WhiteBoardServerServant extends UnicastRemoteObject implements WhiteBoardServerInterface, Serializable{

	// server servant controls the clientManager
	private UserHashMapManager userHashMapManager;
	
	protected WhiteBoardServerServant() throws RemoteException {
		this.userHashMapManager = new UserHashMapManager(this);
	}

	@Override
	public void registerUser(WhiteBoardClientInterface user) throws RemoteException {
		// is userHashMap isEmpty(no manager), set the first user as manager
		if (userHashMapManager.isEmpty()) {
			user.setManager();
		}
		
		// if there is already a manager, ask manager for permission to join the session
		boolean joinPermission = true;
		
		// loop the userHashMap, find the manager, and send permission request to the manager
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			// check if the user in the HashMap is manager
			if (currentUser.getManager()) {
				try {
					// if user is manager, send join request
					joinPermission = currentUser.sendJoinRequest(user.getName());
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
		// grant permission for client
		if (!joinPermission) {
			try {
				user.grantPermission(joinPermission);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
		// manager is labeled with "Manager:" in front of the username
		if(user.getManager()) {
			user.setName("Manager: " + user.getName());
		}
		
		// add the new user to the userHashMap
		this.userHashMapManager.addUser(user);
		System.out.println(this.userHashMapManager.getUserHashMap());
		
		// broadcast updated userHashMap for all other users
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			currentUser.updateUserHashMap(this.userHashMapManager.getUserHashMap());
		}
	}

	// broadcast other user not including self!
	@Override
	public void broadcast(MessageTransmissionInterface msg) throws RemoteException {
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			currentUser.syncWhiteBoard(msg);
		}
	}

	// clear all client canvas when manager create new one
	@Override
	public void newWhiteBoard() throws RemoteException {
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			currentUser.cleanWhiteBoard();
		}
	}

	// non-manager user willing quit the session
	@Override
	public void quitSession(String username) throws RemoteException {
		// only remove self. Need to find the user from the HashMap
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			// find the user and remove from the HashMap
			if (currentUser.getName().equals(username)) {
				this.userHashMapManager.deleteUser(currentUser);
				System.out.println(username + "has left.");
			}
		}
		// broadcast the updated userHashMap to all other users' list when someone left the session.
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			currentUser.updateUserHashMap(this.userHashMapManager.getUserHashMap());
		}
	}

	// manager removes a user from the session
	@Override
	public void removeUser(String username) throws RemoteException {
		// find the user in the HashMap and remove
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			// find the user and remove from the HashMap
			if (currentUser.getName().equals(username)) {
				this.userHashMapManager.deleteUser(currentUser);
				System.out.println(username + "has been removed from the room.");
				// close the user's UI after kicked out
				currentUser.closeUI();
			}
		}
		// broadcast the updated userHashMap to all users
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			currentUser.updateUserHashMap(this.userHashMapManager.getUserHashMap());
		}
	}

	// manager ends the session and removes all users from the session
	@Override
	public void removeAllUsers() throws RemoteException, IOException {
		// remove all users
		System.out.println("The manager has closed the session.");
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			this.userHashMapManager.deleteUser(currentUser);
			currentUser.closeUI();
		}
	}

	// add chat to all users' chat windows
	@Override
	public void addChat(String text) throws RemoteException {
		// TODO Auto-generated method stub
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			try {
				currentUser.addChat(text);
			} catch (Exception e) {
				// Catch exception for server closed
				e.printStackTrace();
			}
		}
	}

	// when new user join the session, manager broadcast his canvas to the new user
	// can combine with openCanvas
	@Override
	public byte[] sendImage() throws RemoteException, IOException {
		// image is saved in byte array
		// the manager's image to all users
		byte[] img = null;
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			if (currentUser.getManager()) {
				img = currentUser.sendImageClient();
			}
		}
		return img;
	}

	// manager opens another canvas file and broadcast to all other users
	@Override
	public void openCanvas(byte[] canvas) throws RemoteException, IOException {
		for (WhiteBoardClientInterface currentUser : this.userHashMapManager) {
			// anyone who is not manager, canvas will be updated
			if (currentUser.getManager() == false) {
				currentUser.drawOpenedImage(canvas);
			}
		}
	}

	@Override
	public Set<WhiteBoardClientInterface> getClientHashMap() throws RemoteException {
		// TODO Auto-generated method stub
		return this.userHashMapManager.getUserHashMap();
	}

}

package remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

// implement these server methods for clients to invoke remotely

public interface WhiteBoardServerInterface extends Remote {
	
	// register client in server when they join, save username, check permission
	public void registerUser(WhiteBoardClientInterface user) throws RemoteException;
	
	// send changes to the server, server broadcast to other clients
	public void broadcast(MessageTransmissionInterface msg) throws RemoteException;
	
	// clear all client canvas when manager create new one
	// when manager calls this method, server will loop through client list and clear all canvas
	public void newWhiteBoard() throws RemoteException;
	
	// when client willingly quit the session, server remove the client from list and broadcast other users
	public void quitSession(String username) throws RemoteException;
	
	// manager removes a user from the session
	public void removeUser(String username) throws RemoteException;
	
	// manager ends the session and removes all users from the session
	public void removeAllUsers() throws RemoteException, IOException;
	
	// add chat to all users' chat windows
	public void addChat(String text) throws RemoteException;
	
	// ********************** ADVANCED FEATURES *************************
	
	// when new user join the session, manager broadcast his canvas to the new user
	// can combine with openCanvas
	public byte[] sendImage() throws RemoteException, IOException;
	
	// manager opens another canvas file and broadcast to all other users
	public void openCanvas(byte[] canvas) throws RemoteException, IOException;
	
	// get the Set of client list from server clientManager
	public Set<WhiteBoardClientInterface> getClientHashMap() throws RemoteException;
}

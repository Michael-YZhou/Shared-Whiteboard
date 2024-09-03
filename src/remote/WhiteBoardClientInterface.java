package remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface WhiteBoardClientInterface extends Remote {
	
	// set the user as manager
	void setManager() throws RemoteException;
	
	// check whether the user is manager
	boolean getManager() throws RemoteException;
	
	// set username
	void setName(String string) throws RemoteException;
	
	// get username
	String getName() throws RemoteException;

	// ask manager for join permission
	boolean sendJoinRequest(String username) throws RemoteException;

	void grantPermission(boolean joinPermission) throws RemoteException;

	// update userHashMap and the user list in UI
	void updateUserHashMap(Set<WhiteBoardClientInterface> userHashMap) throws RemoteException;

	// sync all users' white board
	void syncWhiteBoard(MessageTransmissionInterface msg) throws RemoteException;
	
	// clean the white board
	void cleanWhiteBoard() throws RemoteException;

	void closeUI() throws RemoteException;

	void addChat(String text) throws RemoteException;

	byte[] sendImageClient() throws RemoteException, IOException;

	void drawOpenedImage(byte[] canvas) throws RemoteException, IOException;

	void drawBoard(WhiteBoardServerInterface server) throws RemoteException;

	boolean checkPermission() throws RemoteException;

}

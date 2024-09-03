package remote;

import java.awt.Color;
import java.awt.Point;
import java.rmi.Remote;
import java.rmi.RemoteException;

// This interface ensure the message transmission format is consistent
// the message transmission will rely on object implies this interface.
// to extract information from the object, the sender and receiver need to access the following getters via RMI.
public interface MessageTransmissionInterface extends Remote {
	// getters for message wrapper
	public String getState() throws RemoteException;
	public String getUsername() throws RemoteException;
	public String getMode() throws RemoteException;
	public Color getColor() throws RemoteException;
	public Point getPoint() throws RemoteException;
	public String getText() throws RemoteException;
}

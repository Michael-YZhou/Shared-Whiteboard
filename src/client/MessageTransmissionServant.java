package client;

import java.awt.Color;
import java.awt.Point;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import remote.MessageTransmissionInterface;

public class MessageTransmissionServant extends UnicastRemoteObject implements MessageTransmissionInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String State;
	private String username;
	private String mode;
	private Color color;
	private Point point;
	
	@Override
	public String toString() {
		return "MessageTransmissionServant [State=" + State + ", username=" + username + ", mode=" + mode + ", color="
				+ color + ", point=" + point + ", text=" + text + "]";
	}

	private String text;

	// after drawing, record the state, who draw it, which mode, color, starting/ending point, text
	protected MessageTransmissionServant(String state, String username, String mode, Color color, Point point, String text) throws RemoteException {
		this.State = state;
		this.username = username;
		this.mode = mode;
		this.color = color;
		this.point = point;
		this.text = text;
	}

	@Override
	public String getState() throws RemoteException {
		// TODO Auto-generated method stub
		return this.State;
	}

	@Override
	public String getUsername() throws RemoteException {
		// TODO Auto-generated method stub
		return this.username;
	}

	@Override
	public String getMode() throws RemoteException {
		// TODO Auto-generated method stub
		return this.mode;
	}

	@Override
	public Color getColor() throws RemoteException {
		// TODO Auto-generated method stub
		return this.color;
	}

	@Override
	public Point getPoint() throws RemoteException {
		// TODO Auto-generated method stub
		return this.point;
	}

	@Override
	public String getText() throws RemoteException {
		// TODO Auto-generated method stub
		return this.text;
	}

}

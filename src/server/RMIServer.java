package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;

import remote.WhiteBoardServerInterface;

public class RMIServer {
	public static void main(String[] args) {
		try {
			WhiteBoardServerInterface whiteBoardServer = new WhiteBoardServerServant();
			// create registry
			// Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			Registry registry = LocateRegistry.createRegistry(3000);
			
			registry.bind("SharedCanvasServer", whiteBoardServer);
			
			JOptionPane.showMessageDialog(null, "Server is running!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("port already in use.");
		}
	}
}
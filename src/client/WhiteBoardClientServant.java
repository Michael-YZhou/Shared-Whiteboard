package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import remote.MessageTransmissionInterface;
import remote.WhiteBoardClientInterface;
import remote.WhiteBoardServerInterface;

// WhiteBoardClient class directly manages all White Board functions except the canvas UI
// WhiteBoardClient indirectly manages the canvas UI via the Canvas class
public class WhiteBoardClientServant extends UnicastRemoteObject implements WhiteBoardClientInterface {

	static WhiteBoardServerInterface server;
	private String username;
	// true if the user is the manager
	private boolean isManager;
	// true if the user has the permission to join the session
	private boolean joinPermission;
	
	// Client UI components
	private JFrame frame;
	private DefaultListModel<String> userListModel; // display user list
	private DefaultListModel<String> chatListModel; // list to save the chat history
	private JButton clearBtn, saveBtn, saveAsBtn, openBtn;
	private JButton blackBtn, blueBtn;  // NEED MORE COLOUR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	private JButton drawBtn, lineBtn, rectangleBtn, circleBtn, ovalBtn, textBoxBtn, eraserBtn;
	private JScrollPane messageArea; 
	private JTextArea tellColourArea, displayColourArea;
	private JList<String> chat;  // text area to display chat history
	private ArrayList<JButton>btnList;
	private WhiteBoardCanvas canvasUI;

	// filename and path for saving canvas picture
	private String fileName;
	private String filePath;
	// store the start point of the drawing and the drawing mode
	private Hashtable<String, Point> startPointHashtable = new Hashtable<String, Point>();
	

	
	protected WhiteBoardClientServant() throws RemoteException {
		super();
		isManager = false;
		joinPermission = true;
		userListModel = new DefaultListModel<String>();
		chatListModel = new DefaultListModel<String>();
		btnList = new ArrayList<JButton>();
	}
	
	// listening to client UI mouse events and perform actions accordingly
	ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// Add border to the selected mode
			LineBorder noBorder = new LineBorder(new Color(238,238,238), 2);
			LineBorder showBorder = new LineBorder(Color.black, 2);
			
			if (e.getSource() == clearBtn) {
				canvasUI.reset();
				if(isManager) {
					try {
						server.newWhiteBoard();
					} catch (RemoteException e1) {
						// TODO: handle exception
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "Server is interupted.");
					}
				}
			} else if (e.getSource() == openBtn) {
				try {
					open();
				} catch (IOException e1) {
					// TODO: handle exception
					e1.printStackTrace();
					System.out.println("Cannot open the file.");
				}
			} else if (e.getSource() == saveBtn) {
				try {
					save();
				} catch (IOException e1) {
					// TODO: handle exception
					e1.printStackTrace();
					System.out.println("Cannot save the file.");
				}
			} else if (e.getSource() == saveAsBtn) {
				try {
					saveAs();
				} catch (IOException e1) {
					// TODO: handle exception
					e1.printStackTrace();
					System.out.println("Cannot saveAs the file.");
				}
			} else if (e.getSource() == blackBtn) {
				// choose the paint color
				canvasUI.black();
			} else if (e.getSource() == blueBtn) {
				// choose the paint color
				canvasUI.blue();
			} else if (e.getSource() == drawBtn) {
				canvasUI.draw();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == drawBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			} else if (e.getSource() == lineBtn) {
				canvasUI.line();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == lineBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			} else if (e.getSource() == rectangleBtn) {
				canvasUI.rect();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == rectangleBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			} else if (e.getSource() == circleBtn) {
				canvasUI.circle();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == circleBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			} else if (e.getSource() == ovalBtn) {
				canvasUI.oval();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == ovalBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			} else if (e.getSource() == textBoxBtn) {
				canvasUI.text();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == textBoxBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			} else if (e.getSource() == eraserBtn) {
				canvasUI.eraser();
				// set boarder for the draw button if it is selected
				for(JButton btn : btnList) {
					if (btn == eraserBtn) {
						btn.setBorder(showBorder);
					} else {
						btn.setBorder(noBorder);
					}
				}
			}
			// if a color button is clicked, set the display color area to that color
			if (e.getSource() == blackBtn || e.getSource() == blueBtn) {
				displayColourArea.setBackground(canvasUI.getCurrentColor());
			}
		}
	};
	
	// manager opens a canvas file
	private void open() throws IOException {
		
	}
	
	// manager save a canvas file
	private void save() throws IOException {
		
	}
	
	// manager saveAs a canvas file
	private void saveAs() throws IOException {
		
	}
	
	// generate the client UI
	@Override
	public void drawBoard(WhiteBoardServerInterface server) throws RemoteException {
		// initiate the window
		frame = new JFrame(username); // display username as window title
		Container content = frame.getContentPane();
		
		// canvasUI need to interact with server directly, so pass the server object to it
		canvasUI = new WhiteBoardCanvas(username, isManager, server);
		
		// create the buttons on UI
		blackBtn = new JButton();
		blackBtn.setBackground(Color.black);
		blackBtn.setBorderPainted(false); // no default boarder
		blackBtn.setOpaque(true); // the underlying color won't show through
		blackBtn.addActionListener(actionListener);
		
		blueBtn = new JButton();
		blueBtn.setBackground(Color.blue);
		blueBtn.setBorderPainted(false); // no default boarder
		blueBtn.setOpaque(true); // the underlying color won't show through
		blueBtn.addActionListener(actionListener);
		
		// create the left tool bar buttons on client UI
		LineBorder border = new LineBorder(Color.black, 2);
		// resize the icons and a to the function buttons
		ImageIcon icon = new ImageIcon(".\\src\\btnIcons\\pen.png");
		Image imageIcon = icon.getImage(); // Transform the original icon to image
		Image newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH); // Scale it smoothly to 25x25 pixels
        ImageIcon resizedIcon = new ImageIcon(newImageIcon);  // Transform it back
        drawBtn = new JButton();
        drawBtn.setIcon(resizedIcon);
		drawBtn.setToolTipText("Pen Button");
		drawBtn.setBorder(border);
		drawBtn.addActionListener(actionListener);
        
		
		border = new LineBorder(new Color(238,238,238),2);
		icon = new ImageIcon(".\\src\\btnIcons\\line.png");
		imageIcon = icon.getImage();
		newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH);
        resizedIcon = new ImageIcon(newImageIcon);
		lineBtn = new JButton();
		lineBtn.setIcon(resizedIcon);
		lineBtn.setToolTipText("Line Button");
		lineBtn.setBorder(border);
		lineBtn.addActionListener(actionListener);
		
		icon = new ImageIcon(".\\src\\btnIcons\\rectangle.png");
		imageIcon = icon.getImage();
		newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH);
        resizedIcon = new ImageIcon(newImageIcon);
        rectangleBtn = new JButton();
		rectangleBtn.setIcon(resizedIcon);
		rectangleBtn.setToolTipText("Rectangle Button");
		rectangleBtn.setBorder(border);
		rectangleBtn.addActionListener(actionListener);
		
		icon = new ImageIcon(".\\src\\btnIcons\\circle.png");
		imageIcon = icon.getImage();
		newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH);
        resizedIcon = new ImageIcon(newImageIcon);
        circleBtn = new JButton();
		circleBtn.setIcon(resizedIcon);
		circleBtn.setToolTipText("Circle Button");
		circleBtn.setBorder(border);
		circleBtn.addActionListener(actionListener);
		
		icon = new ImageIcon(".\\src\\btnIcons\\oval.png");
		imageIcon = icon.getImage();
		newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH);
        resizedIcon = new ImageIcon(newImageIcon);
        ovalBtn = new JButton();
		ovalBtn.setIcon(resizedIcon);
		ovalBtn.setToolTipText("Oval Button");
		ovalBtn.setBorder(border);
		ovalBtn.addActionListener(actionListener);
		
		icon = new ImageIcon(".\\src\\btnIcons\\text.png");
		imageIcon = icon.getImage();
		newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH);
        resizedIcon = new ImageIcon(newImageIcon);
        textBoxBtn = new JButton();
		textBoxBtn.setIcon(resizedIcon);
		textBoxBtn.setToolTipText("Text Box Button");
		textBoxBtn.setBorder(border);
		textBoxBtn.addActionListener(actionListener);
		
		icon = new ImageIcon(".\\src\\btnIcons\\eraser_s.png");
		imageIcon = icon.getImage();
		newImageIcon = imageIcon.getScaledInstance(25, 25,  Image.SCALE_SMOOTH);
        resizedIcon = new ImageIcon(newImageIcon);
        eraserBtn = new JButton();
		eraserBtn.setIcon(resizedIcon);
		eraserBtn.setToolTipText("Eraser Button");
		eraserBtn.setBorder(border);
		eraserBtn.addActionListener(actionListener);
		
		btnList.add(drawBtn);
		btnList.add(lineBtn);
		btnList.add(rectangleBtn);
		btnList.add(circleBtn);
		btnList.add(ovalBtn);
		btnList.add(textBoxBtn);
		btnList.add(eraserBtn);
		
//		 create right tool bar buttons (new, save, save as, open close)
		clearBtn = new JButton("New");
		clearBtn.setToolTipText("Create new canvas");
		clearBtn.addActionListener(actionListener);
		
		saveBtn = new JButton("Save");
		saveBtn.setToolTipText("Save canvas");
		saveBtn.addActionListener(actionListener);
		
		saveAsBtn = new JButton("Save As");
		saveAsBtn.setToolTipText("Save as canvas");
		saveAsBtn.addActionListener(actionListener);
		
		openBtn = new JButton("Open");
		openBtn.setToolTipText("Open a canvas");
		openBtn.addActionListener(actionListener);
		
		
		// set the color in current color area
		tellColourArea = new JTextArea("Current color:");
		tellColourArea.setBackground(new Color(238,238,238));
		displayColourArea = new JTextArea("");
		displayColourArea.setBackground(Color.black);
		
		// hide the right tool bar if not manager
		if(isManager == false) {
			clearBtn.setVisible(false);
			saveBtn.setVisible(false);
			saveAsBtn.setVisible(false);
			openBtn.setVisible(false);
		}
		
		// create user list
		JList<String> users = new JList<String>(userListModel);
		JScrollPane currUsers = new JScrollPane(users);
		currUsers.setMinimumSize(new Dimension(100, 150));
		
		
		
		// create chat box
		chat = new JList<>(chatListModel);
		messageArea = new JScrollPane(chat);
		messageArea.setMinimumSize(new Dimension(100,100));
		JTextField messageTextField = new JTextField(); // text field to enter the chat text
		JButton sendButton = new JButton("Send Text");
		sendButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				String chatMsg = messageTextField.getText();
				if(!chatMsg.equals("")) {
					try {
						// send the message, server's add chat will call client's add chat
						server.addChat(username + ": " + chatMsg);
						// scrollpane always show the latest chat
						SwingUtilities.invokeLater(() -> {
							JScrollBar vertical = messageArea.getVerticalScrollBar();
							vertical.setValue(vertical.getMaximum());
						});
					} catch (RemoteException e2) {
						e2.printStackTrace();
						JOptionPane.showConfirmDialog(null, "Server is interupted.");
					}
					// reset the chat text field after sending a message
					messageTextField.setText("");
				}
			}
		});
		
		// UI layout
		GroupLayout layout = new GroupLayout(content);
		content.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()// horizontal has 3 vertical sub-groups
			.addGroup(layout.createParallelGroup() // vertical group 1
				.addComponent(drawBtn)
				.addComponent(lineBtn)
				.addComponent(rectangleBtn)
				.addComponent(circleBtn)
				.addComponent(ovalBtn)
				.addComponent(textBoxBtn)
				.addComponent(eraserBtn)
				)
			.addGroup(layout.createParallelGroup(Alignment.CENTER) // vertical group 2
				.addComponent(canvasUI)
				.addComponent(messageArea)
				.addGroup(layout.createSequentialGroup()
					.addComponent(messageTextField)
					.addComponent(sendButton)
					)
				.addGroup(layout.createSequentialGroup()
					.addComponent(blackBtn)
					)
				.addGroup(layout.createSequentialGroup()
					.addComponent(blueBtn)
					)
				)
			.addGroup(layout.createParallelGroup(Alignment.CENTER) // vertical group 3
//					.addComponent(clearBtn)
//					.addComponent(saveBtn)
//					.addComponent(saveAsBtn)
//					.addComponent(openBtn)
					.addComponent(currUsers)
					.addComponent(tellColourArea)
					.addComponent(displayColourArea)
					)
			);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(drawBtn)
					.addComponent(lineBtn)
					.addComponent(rectangleBtn)
					.addComponent(circleBtn)
					.addComponent(ovalBtn)
					.addComponent(textBoxBtn)
					.addComponent(eraserBtn)
					)
				.addComponent(canvasUI)
				.addGroup(layout.createSequentialGroup()
//					.addComponent(clearBtn)
//					.addComponent(saveBtn)
//					.addComponent(saveAsBtn)
//					.addComponent(openBtn)
					.addComponent(currUsers)
					.addComponent(tellColourArea)
					.addComponent(displayColourArea)
					)
				)
				.addGroup(layout.createSequentialGroup()
					.addComponent(messageArea)
					.addGroup(layout.createParallelGroup()
						.addComponent(messageTextField)
						.addComponent(sendButton)
						)
					)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(blackBtn)
								)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(blueBtn)
								)
						)
			);
		
		// give the buttons same size
//		layout.linkSize(SwingConstants.HORIZONTAL, clearBtn, saveBtn, saveAsBtn, openBtn);
		
		
		// set minimum window size
		frame.setMinimumSize(new Dimension(820, 600));
		frame.setLocationRelativeTo(null); // window will show up in the center of screen
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // default action when window is closed
		frame.setVisible(true);
		
		// when manager close the window, all other users will also be asked to close window
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(isManager) {
					if(JOptionPane.showConfirmDialog(
							frame, 
							"Do you want to end the session?", 
							"End the Session",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						try {
							// if manager quit, remove all users
							server.removeAllUsers();
						} catch (Exception e1) {
							e1.printStackTrace();
							System.out.println("There is an error when remove all users");
						} finally {
							System.exit(0); // close the application
						}
					}
				} else {
					// if not manager, only remove self from session
					if (JOptionPane.showConfirmDialog(
							frame, 
							"Do you want to leave the session?",
							"End the Session",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						try {
							// remove the user and update the user list
							server.removeUser(username);
							updateUserHashMap(server.getClientHashMap());
						} catch (Exception e2) {
							e2.printStackTrace();
							JOptionPane.showMessageDialog(null, "Server is interupted.");
						} finally {
							System.exit(0);
						}
					}
				}
			}
		});
	}

	@Override
	public void setManager() throws RemoteException {
		this.isManager = true;
	}

	@Override
	public boolean getManager() throws RemoteException {
		return this.isManager;
	}

	@Override
	public void setName(String username) throws RemoteException {
		this.username = username;
	}

	@Override
	public String getName() throws RemoteException {
		return this.username;
	}
	
	@Override
	public void addChat(String text) throws RemoteException {
		this.chatListModel.addElement(text);	
	}
	
	@Override
	public void cleanWhiteBoard() throws RemoteException {
		if (this.isManager == false) {
			this.canvasUI.reset();
		}
	}
	
	// send canvas to the new user (sync)
	@Override
	public byte[] sendImageClient() throws RemoteException, IOException {
		ByteArrayOutputStream imgArray = new ByteArrayOutputStream();
		ImageIO.write(this.canvasUI.getCanvas(), "png", imgArray);
		return imgArray.toByteArray();
	}

	// manager opens an image file
	@Override
	public void drawOpenedImage(byte[] image) throws RemoteException, IOException {
		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
		this.canvasUI.drawImage(bufferedImage);
	}

	@Override
	public boolean sendJoinRequest(String username) throws RemoteException {
		if (JOptionPane.showConfirmDialog(
				frame, 
				username + " requests to join the session.", 
				"Request to Join",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void grantPermission(boolean permission) throws RemoteException {
		this.joinPermission = permission;
	}
	
	
	@Override
	public boolean checkPermission() throws RemoteException {
		return this.joinPermission;
	}
	
	@Override
	public void closeUI() throws RemoteException {
		// if permission not granted
		if (!this.joinPermission) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(
							null, 
							"Sorry, your join request has been rejected.", 
							"Permission Required", 
							JOptionPane.WARNING_MESSAGE);
					System.exit(0);
				}
			});
			thread.start();
			return;
		}
		// if user is kicked out
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(
						frame, 
						"You have been removed from the session.", 
						"Connection Lost", 
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		});
		thread.start();
	}

	// this method updates everyone's user list in the UI
	// must be synchronized other when one user clear the list, 
	// other users may read the empty list before new elements get added!
	@Override
	public synchronized void updateUserHashMap(Set<WhiteBoardClientInterface> userHashMap) throws RemoteException {
		this.userListModel.removeAllElements();
		for (WhiteBoardClientInterface currentUser : userHashMap) {
			try {
				userListModel.addElement(currentUser.getName());
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	// free draw or draw a line, takes the current shape and change it to line.
	public Shape makeLine(Shape shape, Point startPoint, Point endPoint) {
		shape = new Line2D.Double(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		return shape;
	}
	
	// to draw rectangle we need start, end, width, length
	public Shape makeRect(Shape shape, Point startPoint, Point endPoint) {
		int x = Math.min(startPoint.x, endPoint.x);
		int y = Math.min(startPoint.y, endPoint.y);
		int width = Math.abs(startPoint.x - endPoint.x);
		int length = Math.abs(startPoint.y - endPoint.y);
		shape = new Rectangle2D.Double(x, y, width, length);
		return shape;
	}
	
	public Shape makeCircle(Shape shape, Point startPoint, Point endPoint) {
		int x = Math.min(startPoint.x, endPoint.x);
		int y = Math.min(startPoint.y, endPoint.y);
		int width = Math.abs(startPoint.x - endPoint.x);
		int length = Math.abs(startPoint.y - endPoint.y);
		shape = new Ellipse2D.Double(x, y, Math.max(width, length), Math.max(width, length));
		return shape;
	}
	
	public Shape makeOval(Shape shape, Point startPoint, Point endPoint) {
		int x = Math.min(startPoint.x, endPoint.x);
		int y = Math.min(startPoint.y, endPoint.y);
		int width = Math.abs(startPoint.x - endPoint.x);
		int length = Math.abs(startPoint.y - endPoint.y);
		shape = new Ellipse2D.Double(x, y, width, length);
		return shape;
	}
	
	public Shape makeText(Shape shape, Point startPoint) {
		int x = startPoint.x - 5;
		int y = startPoint.y - 20;
		int width = 130;
		int length = 25;
		shape = new RoundRectangle2D.Double(x, y, width, length, 15, 15);
		return shape;
	}

	// take the drawing information from other user and draw at local
	@Override
	public void syncWhiteBoard(MessageTransmissionInterface msg) throws RemoteException {
		// skip the user himself
		System.out.println("SYNC: " + msg.getState() + " " + msg.getUsername()+ " " + msg.getMode()+ " " + msg.getColor()+ " " + msg.getPoint() + " " + msg.getText());
		if (msg.getUsername().compareTo(username) == 0) {
			return;
		}
		Shape shape = null;
		
		if (msg.getState().equals("start")) {
			// store the start point and wait for next action
			startPointHashtable.put(msg.getUsername(), msg.getPoint());
			return;
		}
		
		// start from start point
		Point startPoint = (Point)startPointHashtable.get(msg.getUsername());
		
		// set paint color
		canvasUI.getGraphics2d().setPaint(msg.getColor());
		
		if (msg.getState().equals("drawing")) {
			canvasUI.getGraphics2d().setStroke(new BasicStroke(1.0f));
			if (msg.getMode().equals("eraser")) {
				// if mode is eraser, set color to white
				canvasUI.getGraphics2d().setPaint(Color.white);
				canvasUI.getGraphics2d().setStroke(new BasicStroke(15.0f));
			}
			// if not eraser, set color to user selected color
			canvasUI.getGraphics2d().setPaint(msg.getColor());
			
			shape = makeLine(shape, startPoint, msg.getPoint());
			startPointHashtable.put(msg.getUsername(), msg.getPoint());
			canvasUI.getGraphics2d().draw(shape);
			canvasUI.repaint();
			return;
		}
		
		// draw the shape when the mouse is released
		if(msg.getState().equals("end")) {
			canvasUI.getGraphics2d().setStroke(new BasicStroke(1.0f));
			if(msg.getMode().equals("draw") || msg.getMode().equals("line")) {
				canvasUI.getGraphics2d().setPaint(msg.getColor());
				shape = makeLine(shape, startPoint, msg.getPoint());
			} else if (msg.getMode().equals("eraser")) {
				shape = makeLine(shape, startPoint, msg.getPoint());
				canvasUI.getGraphics2d().setPaint(Color.white);
				canvasUI.getGraphics2d().setStroke(new BasicStroke(15.0f));
			} else if (msg.getMode().equals("rect")) {
				canvasUI.getGraphics2d().setPaint(msg.getColor());
				shape = makeRect(shape, startPoint, msg.getPoint());
			} else if (msg.getMode().equals("circle")) {
				canvasUI.getGraphics2d().setPaint(msg.getColor());
				shape = makeCircle(shape, startPoint, msg.getPoint());
			} else if (msg.getMode().equals("oval")) {
				canvasUI.getGraphics2d().setPaint(msg.getColor());
				shape = makeOval(shape, startPoint, msg.getPoint());
			}  else if (msg.getMode().equals("text")) {
				canvasUI.getGraphics2d().setPaint(msg.getColor());
				canvasUI.getGraphics2d().setFont(new Font("TimesRoman", Font.PLAIN, 20));
				canvasUI.getGraphics2d().drawString(msg.getText(), msg.getPoint().x, msg.getPoint().y);
			}
			
			// actually draw the shape if mode is not text
			if (!msg.getMode().equals("text")) {
				try {
					canvasUI.getGraphics2d().draw(shape);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			
			canvasUI.repaint();
			startPointHashtable.remove(msg.getUsername());
			return;
		}
		return;
	}


	
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		String hostname = "localhost";
		String port = "3000";
		String serverName = "SharedCanvasServer";
		String serverAddress = "//" + hostname + ":" + port + "/" + serverName;
		
		server = (WhiteBoardServerInterface) Naming.lookup(serverAddress);
		
		// Initialize the user instance
		WhiteBoardClientInterface user = new WhiteBoardClientServant();
		
		// check if the username is already exist
		boolean isValid = false;
		String user_name = "";
		
		// while the username is not valid
		while(!isValid) {
			// ----------------------------------USER NAME from command line
			user_name = JOptionPane.showInputDialog("Please enter your username:").strip();
			// check if the user enters empty string or only white space
			if(!user_name.equals("")) {
				isValid = true;
			} else {
				JOptionPane.showInputDialog("username cannot be empty!");
			}
			// check if the username already exists in the userHashMap
			for (WhiteBoardClientInterface currentUser : server.getClientHashMap()) {
				if (user_name.equals(currentUser.getName()) || ("Manager: " + user_name).equals(currentUser.getName())) {
					isValid = false;
					// --------------------Add 2 random numbers to the username, make it valid
					JOptionPane.showMessageDialog(null, "This username is already exist, please try a different one.");
				}
			}
		}
		// if the username is valid and not exist, give it to the new user.
		user.setName(user_name);
		
		// server register the new user
		try {
			// only register user with permission granted
			if (user.checkPermission() == true) {
				server.registerUser(user);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// launch the client GUI
		user.drawBoard(server);
	}



}

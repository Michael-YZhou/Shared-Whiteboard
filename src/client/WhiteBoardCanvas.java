package client;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import remote.WhiteBoardServerInterface;

// This class only responsible for the canvas UI
public class WhiteBoardCanvas extends JComponent {
	
	private static final long serialVersionUID = 1L;
	private String username;
	private boolean isManager;
	private Point startPoint, endPoint;
	private Color color;
	private String mode;
	private String text;
	
	private BufferedImage image; // store the image for save
	private BufferedImage previousCanvas; // save the state of canvas before drawing the next pixel
	private Graphics2D graphics2d; // control the color, size of pen
	private WhiteBoardServerInterface server;
	
	
	
	public WhiteBoardCanvas(String name, boolean isManager, WhiteBoardServerInterface remoteInterface) {
		this.server = remoteInterface;
		this.username = name;
		this.isManager = isManager;
		this.color = Color.black; // default color is black
		this.mode = "draw"; // default mode is free draw
		this.text = "";
		
		setDoubleBuffered(false);
		
		// listen to the mouse click, store the point axis and send to server
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// get the axis on canvas where the mouse clicked
				startPoint = e.getPoint();
				// draw the point at the local user's canvas
				saveCanvas();
				try {
					// set the color because eraser will change to white, 
					// and Text only require Press down and release event,
					// in case we use Text immediately after eraser we need to change the Stroke color back from white.
					graphics2d.setPaint(color);
					// in case we draw immediately after another user used eraser,
					// we need to change the Stroke size back from eraser size to pen size.
					graphics2d.setStroke(new BasicStroke(1.0f));
					// wrap all information of the action in a message,
					// then broadcast the message to all other users.
					// state is "start", which means the mouse start pressing.
					MessageTransmissionServant message = new MessageTransmissionServant("start", username, mode, color, startPoint, text);
					System.out.println(message);
					server.broadcast(message);
				} catch(RemoteException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Server is interupted");
				}
			}
		});
		
		// listen to the mouse drag, record where the mouse lifted(end point), send the shape to all users.
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				// get the end point when mouse lifted
				endPoint = e.getPoint();
				
				Shape shape = null;
				if (graphics2d != null) { // if we are drawing
					graphics2d.setPaint(color); // set the paint color
					graphics2d.setStroke(new BasicStroke(1.0f)); // set the pen size
					if(mode.equals("draw")) {
						shape = makeLine(shape, startPoint, endPoint); // makeLine() draws a line
						// in the free draw mode, every point is drawn instantly,
						// so after drawing a pixel, the start point of the next draw 
						// will be the end point of last draw.
						startPoint = endPoint; 
						try {
							// wrap the drawing of the pixel in a message and broadcast to all other users
							MessageTransmissionServant message = new MessageTransmissionServant("drawing", username, mode, color, endPoint, "");
							System.out.println(message);
							server.broadcast(message);
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, "Server is interupted");
						}
					} else if (mode.equals("eraser")) { // eraser is similar to free draw
						shape = makeLine(shape, startPoint, endPoint);
						startPoint = endPoint;
						graphics2d.setPaint(Color.white);  // set the paint color to white for eraser
						graphics2d.setStroke(new BasicStroke(10.0f));
						try {
							MessageTransmissionServant message = new MessageTransmissionServant("drawing", username, mode, Color.white, endPoint, "");
							System.out.println(message);
							server.broadcast(message);
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, "Server is interupted");
						}
					} else if (mode.equals("line")) {
						// draw the previous image then add the new line to it
						drawPreviousCanvas();
						shape = makeLine(shape, startPoint, endPoint);
					} else if (mode.equals("rect")) {
						drawPreviousCanvas();
						shape = makeRect(shape, startPoint, endPoint);
					} else if (mode.equals("circle")) {
						drawPreviousCanvas();
						shape = makeCircle(shape, startPoint, endPoint);
					} else if (mode.equals("oval")) {
						drawPreviousCanvas();
						shape = makeOval(shape, startPoint, endPoint);
					} else if (mode.equals("text")) {
						drawPreviousCanvas();
						graphics2d.setFont(new Font("TimesRoman", Font.PLAIN, 18));
						graphics2d.drawString("Enter text", endPoint.x, endPoint.y);
						shape = makeText(shape, startPoint);
						Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{3}, 0);
						graphics2d.setStroke(stroke);
					}
					// show the shape at local while dragging, but not drawing
					graphics2d.draw(shape);
					// paint the graph(like flush)
					repaint();
				}
			}
		});
		
		// draw the shape when mouse lifted
		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				// when mouse lifted, get the end point
				endPoint = e.getPoint();
				Shape shape = null;
				if (graphics2d != null) { // if we are drawing
					if (mode.equals("draw")) {
						shape = makeLine(shape, startPoint, endPoint);
					} else if (mode.equals("line")) {
						shape = makeLine(shape, startPoint, endPoint);
					} else if (mode.equals("rect")) {
						shape = makeRect(shape, startPoint, endPoint);
					} else if (mode.equals("circle")) {
						shape = makeCircle(shape, startPoint, endPoint);
					} else if (mode.equals("oval")) {
						shape = makeOval(shape, startPoint, endPoint);
					} else if (mode.equals("text")) {
						text = JOptionPane.showInputDialog("Please enter text:");
						if (text == null) {
							text = "";
						}
						drawPreviousCanvas();
						graphics2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));
						graphics2d.drawString(text, endPoint.x, endPoint.y);
						graphics2d.setStroke(new BasicStroke(1.0f));
					}
					// if is drawing shape
					if(!mode.equals("text")) {
						try {
							// only draw the shape when mouse is released
							graphics2d.draw(shape);
						} catch (Exception e1) {
							// no need to handle, this is when we don't need to send the action
						}
					}
					
					repaint();
					
					try {
						MessageTransmissionServant message = new MessageTransmissionServant("end", username, mode, color, endPoint, text);
						System.out.println(message);
						server.broadcast(message);
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "Server is interupted");
					}
				}
			}
		});
	}
	
	
	// when a user join, if new user is manager, start a blank canvas, 
	// else, sync manager's canvas to the user.
	protected void paintComponent(Graphics g) {		
		if (image == null) {
			if(isManager) {
				image = new BufferedImage(700, 350, BufferedImage.TYPE_INT_RGB);
				graphics2d = (Graphics2D) image.getGraphics();
				graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				reset();
				
			} else {
				try {
					byte[] rawImage = server.sendImage();
					image = ImageIO.read(new ByteArrayInputStream(rawImage));
					graphics2d = (Graphics2D) image.getGraphics();
					graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					graphics2d.setPaint(color);
					
				} catch (Exception e) {
					System.out.print("Cannot receive image.");
				}
			}
		}
		g.drawImage(image, 0, 0, null);
	}
	
	public Color getCurrentColor() {
		return color;
	}
	
	public String getCurrentMode() {
		return mode;
	}
	
	public Graphics2D getGraphics2d() {
		return graphics2d;
	}
	
	public BufferedImage getCanvas() {
		saveCanvas();
		return previousCanvas;
	}
	
	// to reset canvas, paint the whole canvas with white
	public void reset() {
		graphics2d.setPaint(Color.white);
		graphics2d.fillRect(0, 0, 700, 350);
		graphics2d.setPaint(color); // reset paint color to black
		repaint();
	}
	
	// save the canvas image
	public void saveCanvas() {
		ColorModel colorModel = image.getColorModel();
		WritableRaster writableRaster = image.copyData(null);
		previousCanvas = new BufferedImage(colorModel, writableRaster, false, null);
	}
	
	// show the previous drawing on canvas but not drawing
	public void drawPreviousCanvas() {
		drawImage(previousCanvas);
	}
	
	public void drawImage(BufferedImage buffImg) {
		graphics2d.drawImage(buffImg, null, 0, 0);
		repaint();
	}
	
	// color plate
	public void black() {
		this.color = Color.black;
		graphics2d.setPaint(color);
	}
	
	public void cyan() {
		this.color = Color.cyan;
		graphics2d.setPaint(color);
	}
	
	public void blue() {
		this.color = Color.blue;
		graphics2d.setPaint(color);
	}
	
	public void darkGray() {
		this.color = Color.darkGray;
		graphics2d.setPaint(color);
	}
	
	public void gray() {
		this.color = Color.gray;
		graphics2d.setPaint(color);
	}
	
	public void green() {
		this.color = Color.green;
		graphics2d.setPaint(color);
	}
	
	public void lightGray() {
		this.color = Color.lightGray;
		graphics2d.setPaint(color);
	}
	
	public void magenta() {
		this.color = Color.magenta;
		graphics2d.setPaint(color);
	}
	
	public void orange() {
		this.color = Color.orange;
		graphics2d.setPaint(color);
	}
	
	public void pink() {
		this.color = Color.pink;
		graphics2d.setPaint(color);
	}
	
	public void red() {
		this.color = Color.red;
		graphics2d.setPaint(color);
	}
	
	public void yellow() {
		this.color = Color.yellow;
		graphics2d.setPaint(color);
	}
	
	public void brown() {
		this.color = new Color(150, 70, 0);
		graphics2d.setPaint(color);
	}
	
	// set mode
	public void draw() {
		mode = "draw";
	}
	
	public void line() {
		mode = "line";
	}
	
	public void rect() {
		mode = "rect";
	}
	
	public void circle() {
		mode = "circle";
	}
	
	public void oval() {
		mode = "oval";
	}
	
	public void text() {
		mode = "text";
	}
	
	public void eraser() {
		mode = "eraser";
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
		int width = 150;
		int length = 30;
		shape = new RoundRectangle2D.Double(x, y, width, length, 15, 15);
		return shape;
	}
}

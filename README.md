# Project README

## Introduction
This project implements a shared whiteboard using Client-Server Architecture and RMI (Remote Method Invocation). The server manages a list of users using a ConcurrentHashMap and facilitates communication between clients by forwarding actions performed by one client to all others.

## 1. System Design

### 1.1 Application Structure
The project utilizes Client-Server Architecture with RMI for data transmission. RMI abstracts internet socket management and enables remote method invocation. The server registers methods on the RMI registry, which clients can then invoke remotely.

### 1.2 Internet Protocol
RMI handles internet protocol using TCP/IP as the underlying network protocol, ensuring reliable, ordered, and error-checked data transmission.

### 1.3 Concurrency Control
Concurrency is managed using a ConcurrentHashSet from java.util.concurrent to store and manage the collection of users. This provides thread-safe operations without external synchronization.

### 1.4 User Interface
The project's UI is built using Java AWT and Swing packages. AWT supports UI creation and provides methods for drawing shapes (lines, rectangles, circles, text boxes), while Swing simplifies UI element creation and layout management.

### 1.5 Message Exchange Protocol
Communication between clients and the server uses a custom format. The MessageTransmissionInterface, registered in the RMI registry, allows clients to extract data from the MessageTransmissionServant class, which stores drawing data. Messages include username, action status, action type (pen, eraser, text box, shape), color, and canvas coordinates.

## 2. Class Design

### 2.1 Class Structure
Both server and client sides implement the RMI model. The server registers its methods, and clients make their methods remotely accessible by implementing a remote interface. Clients perform local actions, which are sent to the server via remote method invocation. The server then forwards these actions to all other clients, ensuring synchronized canvas boards.


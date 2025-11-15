# DonCEy Kong Jr.

**Version:** 1.11.2 

---

## Project Description
**DonCEy Kong Jr.** is a distributed gaming system based on a **client-server architecture**. 
A **Java server** manages the core game logic, while multiple **C clients** connect as players or spectators. 
The server maintains the global game state and coordinates communication between clients, ensuring a synchronized and consistent gameplay experience.

---

## Team Members
- **Eduardo José Canessa-Quesada** 
- **Luis Felipe Chaves-Mena** 
- **Deiler Morera-Valverde**

---

##  Prerequisites

### System Requirements
- **Java Development Kit (JDK) 8 or higher** – for server compilation and execution 
- **GCC Compiler** – for client compilation 
- **Network Connectivity** – all components must be on the same LAN/Wi-Fi network 

### Network Configuration
- Obtain the server’s **IPv4 address** before client configuration 
- Ensure the **firewall allows TCP connections** on the chosen port 
- Verify that **all devices can communicate** over the local network 

---

## Installation & Execution

### Server Setup

1. **Configure the Network** 
   Ensure the server machine has a **static IP** or note the current dynamic IP address.

2. **Navigate to the Server Directory**
   ```bash
   cd Server/Network
   ```

3. **Compile the Java Server**
   ```bash
   javac Server.java
   ```

4. **Launch the Server**
   ```bash
   java Server
   ```

####  Important Notes
- The generated `.class` files are **required** for execution – do not delete them. 
- The server console will indicate successful startup and will **await client connections**. 
- The server must **remain running** throughout the gameplay session.

---

### Client Setup

1. **Configure the Server IP** 
   Edit `client.c` at **line 9**, replacing the placeholder with the **server’s actual IPv4 address**.

2. **Navigate to the Client Directory**
   ```bash
   cd Client
   ```

3. **Compile the Client Application**
   ```bash
   gcc client.c -o client
   ```

4. **Execute the Client**
   ```bash
   ./client
   ```

## Course
CE1106 - Paradigmas de Programación 
Instituto Tecnológico de Costa Rica  
2025 Semester II

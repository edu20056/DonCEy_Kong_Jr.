# DonCEy Kong Jr.
Version: 0.0.1

## Project Description
Design and implementation of a Java server and C clients to send game information. Java server will control all logic meanwhile C clients will work as players or spectators, only players can send information to server and server will send information to all clients.

## Team Members
- Eduardo Jose Canessa-Quesada
- Luis Felipe Chaves-Mena
- Deiler Morera-Valverde

## How to run each code..
- **Server**: To run de Java server you must be connected to a certain wi-fi, then you must get your IPAdress (IPV4), then you go to Server/Code directory and use the next commands:
- javac Server.java
- java Server
These will compile and run the java server and must be ready to receive clients. It will generate some .class files, do not deleted them, they are important to run the code.

- **Clients**: To run de C clients you must have your servers IPAdress and plug it on client.c before compiling, it is located on line 9. Remember server and clients must be connected to the same wi-fi. Then go to Client directory and use the next commands:
- gcc client.c -o client
- ./client
These commands will compile and run your code. If adress was correct a menu to select the client type will be shown.

## Course
CE1106 - Paradigmas de Programación 
Instituto Tecnológico de Costa Rica  
2025 Semester II
# PennPals - Java Chat Server

**Course**: CIS 1200 - Programming Languages and Techniques II  
**Project**: Building an Internet Chat Server in Java

---

## Project Overview

**PennPals** is a Java-based internet chat server that simulates the backend operations of a chat service. The project models how clients connect to a server, join or create channels, and communicate through message exchanges. It demonstrates the fundamentals of client-server architecture and focuses on designing efficient, scalable systems.

This project was an integral part of my course on programming languages and helped deepen my understanding of software design, networking protocols, and data structures.

---

## Key Concepts and What I Learned

1. **Client-Server Architecture**:  
   I implemented a system where multiple clients (users) interact with a single server. This introduced me to the concept of state management on a server, user sessions, and how servers handle multiple requests concurrently.
   
2. **Networking and Protocol Design**:  
   I gained hands-on experience with protocols in client-server communication. By designing command-based interactions (like creating channels or sending messages), I understood how communication over the network can be standardized for efficient data exchange.

3. **Data Structures for Real-time Systems**:  
   Choosing appropriate data structures was a critical learning point. I utilized `TreeSet`, `TreeMap`, and `LinkedList` to manage users, channels, and message history. These data structures allowed efficient access and manipulation of data, minimizing latency in server responses.

4. **Concurrency and User Management**:  
   The project required handling multiple users simultaneously. Each user is uniquely identified by an ID, with state management ensuring that operations like joining channels or sending messages are thread-safe.

5. **Testing and Code Quality**:  
   Implementing robust unit tests was key to ensuring that each component of the system behaved as expected. I developed custom tests for edge cases, ensuring the system could handle everything from invalid inputs to high user loads. This taught me the importance of thorough testing and its role in building reliable systems.

6. **Refactoring and Code Design**:  
   As the system complexity grew, I refined my code to ensure modularity, readability, and maintainability. This included creating helper classes, improving method organization, and reducing redundancy, which helped me practice best coding practices for long-term project sustainability.

---

## Project Structure

- **ServerModel**: Central to the project, this class handles the server state, user sessions, and processing of commands. It is responsible for managing all users, channels, and messages.
- **Command.java**: This abstract class defines the protocol used for client-server communication. Various commands like `CreateCommand`, `JoinCommand`, and `MessageCommand` enable users to interact with the server.
- **PLAN.txt**: A document detailing the design choices made throughout the project, including explanations for data structure usage and protocol implementation.
- **Test Suite**: Includes unit tests to validate functionality and ensure the reliability of the system. Tests were created for each feature, including edge cases such as invalid command inputs or incorrect user actions.

---

## Major Features

- **User Management**: Clients can connect to the server, register with unique IDs, and modify their nicknames.
- **Channels**: Users can create public or private channels, join or leave channels, and send messages within them.
- **Private Channels**: Channel owners can invite users to private channels or kick them out, adding an extra layer of access control.
- **Protocol Handling**: Commands like `NicknameCommand`, `MessageCommand`, and `CreateCommand` are used to handle all communication between clients and the server.
- **Concurrency**: The server manages multiple clients simultaneously, ensuring proper state handling and data consistency across all user interactions.

---

## Technical Challenges Overcome

1. **Managing State Across Multiple Users**:  
   Implementing a system that manages the state of multiple users, channels, and their relationships was challenging. I had to ensure that the data structures I used were efficient and avoided unnecessary complexity.

2. **Designing a Command-based Protocol**:  
   Creating a robust and extensible protocol for communication between the server and clients was another significant learning experience. This involved abstracting out common operations into commands and ensuring these commands interacted seamlessly with the server state.

3. **Private and Public Channels**:  
   Balancing privacy controls and user access in private and public channels required careful management of permissions and channel ownership. The design allowed owners to manage their channels without impacting the server's performance or usability.

---

## What I Learned

This project taught me the importance of designing systems that are both scalable and maintainable. From data structure selection to managing client-server communication, I gained a strong understanding of software design principles. Moreover, I learned how critical testing is in the software development lifecycle, ensuring each feature behaves as intended in various scenarios. 

---

## Running the Project

### In Codio
1. Start the server from the menu.
2. Launch the client by selecting "PennPals" from the menu.

### In IntelliJ
1. Run `ServerMain.java` to start the server.
2. Launch a client by running the `hw07-client.jar` file. Use multiple clients for testing.


# Distributed P2P System with Chord and Suzuki-Kasami

This project implements a distributed P2P system. It uses the Chord protocol for DHT functionality and the Suzuki-Kasami algorithm for distributed mutex. The system supports basic file sharing operations (upload, list, remove) with considerations for reliability and fault tolerance.

## Table of Contents

1.  [Features](#features)
2.  [Core Concepts](#core-concepts)
    *   [Chord DHT](#chord-dht)
    *   [Bootstrap Process](#bootstrap-process)
    *   [File Operations](#file-operations)
    *   [Data Replication](#data-replication)
    *   [Fault Tolerance](#fault-tolerance)
    *   [Distributed Mutex (Suzuki-Kasami)](#distributed-mutex-suzuki-kasami)
    *   [File Visibility and Following](#file-visibility-and-following)
3.  [Why Apache Avro?](#why-apache-avro)
4.  [Technology Stack](#technology-stack)
5.  [Getting Started](#getting-started)
    *   [Prerequisites](#prerequisites)
    *   [Building the Project](#building-the-project)
    *   [Running the Simulation](#running-the-simulation)
6.  [Configuration](#configuration)
7.  [Available CLI Commands](#available-cli-commands)

## Features

*   **Chord DHT Implementation:** Dynamic node joining, leaving and routing.
*   **Distributed File Storage:** Upload, list and remove image files.
*   **Data Replication:** Configurable replication factor for fault tolerance.
*   **Fault Tolerance:**
    *   Node crash detection via PING/PONG and a helper mechanism.
    *   Automatic node removal and system recovery.
    *   Data re-replication after node failure.
*   **Distributed Mutex:** Suzuki-Kasami algorithm for critical sections.
*   **Selective Avro Serialization:** For Suzuki-Kasami token and request messages.
*   **File Visibility Control:** `public` and `private` modes.
*   **Follower System:** Nodes can follow others to access private files.
*   **Command-Line Interface (CLI):** For interacting with individual servents.
*   **Multi-Servent Simulation:** Supports running a full system simulation from configuration files.

## Core Concepts

### Chord DHT

The system is built upon the Chord protocol, a distributed lookup protocol that provides DHT functionality.
*   **Nodes (Servents):** Each peer in the system is a servent.
*   **Chord Ring:** Servents are organized in a logical ring based on their Chord IDs, which are hashes of their IP address and port.
*   **Successors & Predecessors:** Each servent maintains information about its successor and predecessor in the ring. It also maintains a finger table (successor table).
*   **Hashing:** Keys (derived from file paths) are hashed to determine which servent is responsible for storing the corresponding data.

### Bootstrap Process

1.  A new servent first contacts a **Bootstrap Server**.
2.  The Bootstrap Server provides the new servent with:
    *   The identity of an existing servent in the Chord ring (if any).
    *   A list of currently active servents.
3.  If it's the first node, it initializes itself.
4.  Otherwise, the new servent acquires a distributed mutex (Suzuki-Kasami token), then sends a `NEW_NODE` message to the known servent.
5.  This message is routed through the Chord ring until it reaches the servent that will become the new node's successor.
6.  The successor responds with a `WELCOME` message, transferring relevant data and updating its predecessor.
7.  The new node then broadcasts an `UPDATE` message to inform other nodes of its presence and integrate itself fully into the ring, sharing its data and synchronizing request numbers for the mutex.

### File Operations

*   **Upload:**
    - A servent wishing to upload a file first acquires the Suzuki-Kasami mutex.
    - The file's path is hashed to get a key.
    - An `UPLOAD` message containing the file data is routed to the servent responsible for that key.
    - The responsible servent stores the file data and initiates replication.
    - An `UPLOAD_ACK` is sent back, and the mutex is released.
    
*   **List Files:**
    - A servent requests a list of files from another servent (identified by its address:port).
    - The requesting servent acquires the mutex.
    - A `LIST_IMAGES_REQUEST` is sent. The key is derived from the target servent's address:port.
    - The message is routed to the servent responsible for the target servent's *actual data* (which might not be the target servent itself if it's not storing its own files due to Chord).
    - The responsible servent checks visibility (public or if the requester is a follower) and returns a `LIST_IMAGES_RESPONSE`.
    - Mutex is released by the original requester.
    
*   **Remove File:**
    - A servent acquires the mutex to remove a file it originally uploaded.
    - A `REMOVE` message is routed to the servent responsible for the file's key.
    - The responsible servent deletes the file data (if the requester is the owner) and initiates the removal of replicas.
    - A `REMOVE_ACK` is sent back, and the mutex is released.

### Data Replication

*   Files are replicated to `REPLICATION_FACTOR - 1` other nodes.
*   Replicas are typically stored on the successors of the primary node responsible for the file.
*   When a node fails or leaves, the system re-evaluates replication needs for the affected data and creates new replicas if necessary.

### Fault Tolerance

*   **Pinging:** Servents periodically ping their predecessors using `PING` messages. Predecessors respond with `PONG`.
*   **Weak Timeout:** If a `PONG` isn't received within `weak_timeout`, the node becomes suspicious.
*   **Helper Mechanism:** The suspicious node sends a `HELP_REQUEST` to another node (preferably its successor) to check the status of the unresponsive node. The helper attempts a direct connection.
*   **Help Confirmation:** The helper responds with a `HELP_CONFIRM` message indicating if the suspicious node is alive or dead.
*   **Strong Timeout:** If the suspicious node remains unresponsive beyond `strong_timeout` AND is confirmed dead it's declared dead.
*   **Node Removal & Recovery:**
    - The detecting node acquires the Suzuki-Kasami mutex.
    - It locally removes the dead node from its Chord state.
    - It broadcasts `RECOVERY` messages to other nodes, informing them of the dead node.
    - It re-evaluates and ensures data replication for files it's responsible for.
    - The mutex is released.
    - Other nodes receiving the `RECOVERY` message also update their Chord state and check their replication responsibilities.
*   **Simulated Crashes:** The `simulate_crash` command allows testing the fault tolerance mechanisms.

### Distributed Mutex (Suzuki-Kasami)

*   The Suzuki-Kasami algorithm is used to ensure that critical operations (like file uploads, removals, and potentially some state updates during node joining) are performed atomically across the distributed system.
*   It uses a **token** that circulates among the nodes. Only the node holding the token can enter a critical section.
*   **Token Request (`SUZUKI_TOKEN_REQUEST`):** A node needing the token increments its request number (RN) for itself and broadcasts a request containing this RN.
*   **Token (`SUZUKI_TOKEN`):** When a node holding the token receives a request, it updates its knowledge of other nodes' RNs. If it's not in a critical section and the requesting node's RN indicates a newer request than its last known satisfaction for that node, it adds the requester to its token queue. When it exits its critical section, it passes the token to the head of its queue.
*   The token itself contains a list of last known satisfied request numbers (LN) for all nodes and a queue of pending requests.
*   **Avro Serialization:** Messages related to the Suzuki-Kasami algorithm (`SUZUKI_TOKEN_REQUEST`, `SUZUKI_TOKEN`) are serialized using Apache Avro.

### File Visibility and Following

*   Servents can set their file visibility to `public` (default) or `private`.
*   If `private`, only servents that "follow" this servent can list its files.
*   The `follow` command sends a `FOLLOW_REQUEST`.
*   The target servent receives this and can `accept` the request, sending a `FOLLOW_ACCEPT` back.

## Why Apache Avro?

This project uses Apache Avro for serializing specific, critical message types: `SUZUKI_TOKEN_REQUEST` and `SUZUKI_TOKEN`. While standard Java serialization is used for other messages, Avro was chosen for these reasons:

1.  **Schema Definition:** Avro messages are defined by schemas (`.avsc` files). This provides:
    *   **Clear Contract:** The structure of the message is explicitly defined, acting as a contract between communicating servents.
    *   **Data Validation:** Ensures that messages conform to the expected structure.
    *   **Code Generation:** Java classes can be automatically generated from these schemas, reducing boilerplate and potential errors.
2.  **Compact Binary Format:** Avro serializes data into a compact binary format. This is more efficient for network transmission compared to Java's default serialization, especially for complex data structures like the Suzuki-Kasami token which includes lists and queues. Reduced message size means less network overhead.
3.  **Schema Evolution:** Avro is designed to handle schema evolution. If the structure of the token messages needed to change (e.g., adding a new field), Avro's rules for schema resolution allow older and newer versions of the software to interoperate without breaking, assuming compatible changes. This is a significant advantage in evolving distributed systems.

## Technology Stack

*   Java
*   Gradle
*   Lombok
*   Apache Avro

## Getting Started

### Prerequisites

*   Java Development Kit (JDK), version 17 or higher recommended.
*   Gradle (or use the provided Gradle wrapper).

### Building the Project

1.  Clone the repository:
    ```bash
    git clone https://github.com/VasilijeJukic01/KIDS-P3
    cd KIDS-P3
    ```
2.  Build the project using Gradle to create a shadow JAR:
    ```bash
    ./gradlew shadowJar
    ```
    On Windows:
    ```bash
    gradlew.bat shadowJar
    ```
    This will create an executable JAR in `build/libs/KiDS-P3-1.0-SNAPSHOT-all.jar`.

### Running the Simulation

The project is designed to be run as a multi-servent simulation controlled by configuration files. The main entry point for this is `MultipleServentStarter`.

1.  The `MultipleServentStarter` class is configured as the `Main-Class` in the JAR manifest and Gradle application plugin.
2.  By default, it expects a directory named `chord` in the project's root, containing the `servent_list.properties` file and `input`, `output`, `error` subdirectories.
3.  To run the simulation after building:
    ```bash
    java -jar build/libs/KiDS-P3-1.0-SNAPSHOT-all.jar
    ```
    This will:
    *   Start a bootstrap server.
    *   Start multiple servent instances as defined in `chord/servent_list.properties`.
    *   Each servent will read its commands from the corresponding `chord/input/serventX_in.txt` file.
    *   Output and error logs will be written to `chord/output/` and `chord/error/` respectively.
4.  To stop the simulation if it doesn't terminate on its own, type `stop` in the console where `MultipleServentStarter` is running.

## Configuration

The primary configuration file for the simulation is `chord/servent_list.properties`. Key parameters include:

| Parameter             | Description                                                              | Example Value        |
| :-------------------- | :----------------------------------------------------------------------- | :------------------- |
| `servent_count`       | Total number of servents in the simulation.                              | `7`                  |
| `chord_size`          | Maximum value for Chord keys (must be a power of 2).                     | `64`                 |
| `bs_address`          | IP address of the bootstrap server.                                      | `localhost`          |
| `bs_port`             | Port for the bootstrap server.                                           | `2002`               |
| `weak_timeout`        | Timeout (ms) to consider a node suspicious.                              | `4000`               |
| `strong_timeout`      | Timeout (ms) to declare a suspicious node dead.                          | `10000`              |
| `servent.ip_address`  | Default IP address for servents.                                         | `localhost`          |
| `serventX.port`       | Listener port for servent `X`.                                           | `1000` (for servent0)|
| `work_dirX`           | Working directory for servent `X` (for storing files).                   | `chord\core\dir0`    |

## Available CLI Commands

Each servent instance accepts the following commands (typically fed via `serventX_in.txt` during simulation):

| Command                     | Arguments                     | Description                                                                |
| :-------------------------- | :---------------------------- | :------------------------------------------------------------------------- |
| `info`                      | -                             | Prints information about the current node.                                 |
| `pause`                     | `[ms]`                        | Pauses execution for the specified number of milliseconds.                 |
| `successor_info`            | -                             | Prints the successor table of the current node.                            |
| `upload`                    | `[file_path]`                 | Uploads the specified image file.                                          |
| `list_files`                | `[address:port]`              | Lists files of the node at `address:port`.                                 |
| `visibility`                | `public` / `private`          | Sets file visibility mode for the current node.                            |
| `remove_file`               | `[file_path]`                 | Removes the specified file (must be owner).                                |
| `follow`                    | `[address:port]`              | Sends a follow request to the node at `address:port`.                      |
| `pending`                   | -                             | Lists pending follow requests received by the current node.                |
| `accept`                    | `[address:port]`              | Accepts a pending follow request from `address:port`.                      |
| `simulate_crash`            | -                             | Simulates an immediate crash of the current node (exits).                  |
| `stop`                      | -                             | Stops the current servent gracefully.                                      |

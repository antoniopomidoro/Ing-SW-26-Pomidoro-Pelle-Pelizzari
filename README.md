# 🎲 Mesos - Software Engineering Project

![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-25-green.svg)
![Jackson](https://img.shields.io/badge/Jackson-2.17-yellow.svg)
![JUnit](https://img.shields.io/badge/JUnit-5-red.svg)

Final project for the **Software Engineering** course (A.Y. 2025/2026) at Politecnico di Milano.
The project consists of the implementation of the board game **Mesos** (GC42), developed in Java
following the MVC (Model–View–Controller) architectural pattern, with support for both **Socket**
and **RMI** networking.

---

## 👥 Group Members

| Name Surname | GitHub |
| :--- | :--- |
| Antonio Pomidoro | [@antoniopomidoro](https://github.com/antoniopomidoro) |
| Alessandro Pelle | [@alepelle1608](https://github.com/alepelle1608) |
| Davide Pelizzari | [@DPeliz](https://github.com/DPeliz) |

---

## 🛠 Technologies

* **Language:** Java 25
* **Build Automation:** Maven
* **User Interface:** JavaFX 25
* **Network:** Socket / RMI
* **JSON Processing:** Jackson 2.17
* **Testing:** JUnit 5

---

## 🏗 Architecture

The application follows the **MVC** pattern and is organized into four modules under the
`it.polimi.ingsw` package:

* `model` — game state and rules
* `controller` — game flow coordination
* `view` — JavaFX user interface
* `network` — Socket and RMI communication layer

Game data (cards, buildings, tiles, configuration) is loaded from JSON resources, while game
saves use Java serialization.

---

## 🚀 Build & Run

### Requirements
* JDK 25
* Maven 3.8+

### Pre-built jars

Ready-to-run fat-jars are available in `deliverables/jars/`:

* `server.jar` — game server (`it.polimi.ingsw.App`)
* `client.jar` — client (`it.polimi.ingsw.view.ClientLauncher`), runnable as either **GUI (JavaFX)** or **CLI**

```bash
java -jar deliverables/jars/server.jar
java -jar deliverables/jars/client.jar
```

On startup the client prompts for the connection options:

* **Server IP** (default `localhost`)
* **Interface** — GUI (JavaFX) or CLI
* **Transport** — Socket or RMI

Each jar bundles all dependencies, including the JavaFX native libraries for Windows and Linux.

### Build from source

```bash
mvn clean package
```

This rebuilds the two fat-jars in `target/` (`server.jar` and `client.jar`).

### Run via Maven

```bash
mvn exec:java@server   # start the server
mvn exec:java@client   # start the client
```

---

## ✅ Tests

```bash
mvn test
mvn test -Dtest=ClassName#methodName   # run a single test
```

## 📜 License
> **Il gioco da tavolo Mesos e tutto il relativo materiale grafico è di esclusiva proprietà di Cranio Creations.**

This project is a non-commercial reimplementation for educational purposes only. All game rights belong to *Cranio Creations*. Unauthorized use, reproduction, or distribution of this project is prohibited.

See the [LICENSE](LICENSE) file for details.
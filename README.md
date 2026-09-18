[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)

# P2Backup
P2Backup ist ein Tool mit dem man Backups erstellen kann. Damit lassen sich die eigenen Daten auf zB. USB-Platten sichern. Es werden mehrere Versionsstände gespeichert. Damit können die unterschiedlichen alten Versionen meiner Daten durchsucht oder wieder hergestellt werden.


![Screenshot](/screenshot.png)


## Infos
Das Programm nutzt den Ordner:  
**Linux:**  
*/home/USER/.p2Backup*  

**Windows:**  
*C:\Users\USER\p2Backup*  
als Konfig-Ordner. Man kann dem Programm auch einen Ordner für die Einstellungen mitgeben (und es z.B. auf einem USB-Stick verwenden):

```
java -jar P2Backup.jar ORDNER 
```

## Systemvoraussetzungen
Unterstützt wird Windows und Linux. Das Programm benötigt eine aktuelle Java-VM ab Version: Java 17. Für Linux-Benutzer wird OpenJDK17 empfohlen. (FX-Runtime bringt das Programm bereits mit und muss nicht installiert werden).


## Download
Das Programm wird in unterschiedlichen Paketen angeboten. Diese unterscheiden sich nur im “Zubehör”, das Programm selbst ist in allen Paketen identisch: 

* **P2Backup-XX__Windows==SETUP__DATUM.exe**  
Mit diesem Programmpaket kann das Programm auf Windows installiert werden: Doppelklick und alles wird eingerichtet, auch ein Startbutton auf dem Desktop. Es muss auch kein Java auf dem System installiert sein. (Die Java-Laufzeitumgebung ist enthalten).

* **P2Backup-XX__DATUM.zip**  
Das Programmpaket bringt nur das Programm und die benötigten Hilfsprogramme aber kein Java mit. Auf dem Rechner muss eine Java-Laufzeitumgebung ab Java17 installiert sein. Dieses Programmpaket kann auf allen Betriebssystemen verwendet werden. Es bringt Startdateien für Linux und Windows mit. Zip entpacken und Programm Starten.

* **P2Backup-XX__Linux+Java__DATUM.zip**  
**P2Backup-XX__Win+Java__DATUM.zip**  
Diese Programmpakete bringen die Java-Laufzeitumgebung mit und sind nur für das angegebene Betriebssystem: Linux oder Windows. Es muss kein Java auf dem System installiert sein. (Die Java-Laufzeitumgebung liegt im Ordner: "Java" und kommt von jdk.java.net). Zip entpacken und Programm starten.

zum Download:  
[github.com/xaverW/P2Backup/releases](https://github.com/xaverW/P2Backup/releases)  


## Installation
P2Backup-XX__Windows==SETUP__DATUM.exe wird durch einen Doppelklick darauf installiert. Die anderen Versionen müssen nicht installiert werden, das Entpacken der heruntergeladenen ZIP-Datei ist quasi die Installation. Die heruntergeladene ZIP-Datei entpacken und den entpackten Ordner “P2Backup...” ins Benutzerverzeichnis verschieben. Das Programm kann dann mit Doppelklick auf:  
Linux: “P2Backup__Linux.sh” oder  
Windows: “P2Backup__Windows.exe”  
gestartet werden.


## Anleitung
Eine Anleitung zum Programm findet sich auf der Website:  
<a target="_blank" href="https://www.p2backup.de/">www.p2backup.de</a>


## Website
[www.p2backup.de]( https://www.p2backup.de)



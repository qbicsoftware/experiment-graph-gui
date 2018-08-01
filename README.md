# Experiment Graph
[![Build Status](https://travis-ci.com/qbicsoftware/experiment-graph-gui.svg?branch=development)](https://travis-ci.com/qbicsoftware/experiment-graph-gui)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/experiment-graph-gui/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/experiment-graph-gui)

Experiment Graph - Parses experimental designs and displays a graph summarizing nodes by their experimental factors

## Author
Created by Andreas Friedrich (andreas.friedrich@qbic.uni-tuebingen.de).

## Description
This viewer summarizes sample sources and extracted samples based on similarity of independent variables, enabling a quick grasp of the scientific question at the core of the experiment even for large experiments. 

## How to Install
To execute this stand-alone viewer, you require the following software components:
* A Java Runtime Environment (JRE) or Java Development Kit (JDK) compatible with Java 8. Installation of Java depends on your operating system.
* JavaFX dependencies. This also depends on your operating system and whether you are using Oracle's JDK or OpenJDK.

### Installing a release
* Find the latest release on the [releases page](https://github.com/qbicsoftware/experiment-graph-gui/releases). 
* Download the `experiment-graph-gui-<version>.tar.gz` file from the release assets (e.g., `experiment-graph-gui-0.2.1.tar.gz`). 
* Unzip the `experiment-graph-gui-<version>.tar.gz` archive. This will create a folder named `experiment-graph-gui`.

To execute the stand-alone viewer, change to the `experiment-graph-gui` folder and execute the following command in a terminal:

```sh
java -jar experiment-graph-gui.jar
```

## Information for developers
We use [Apache Maven](maven) to compile the source code, make sure you have the latest version installed.

You will also need to set the `MAVEN_OPTS` environment variable to contain the Java Virtual Machine (JVM) option `-Xss4m` (i.e., `export MAVEN_OPTS="-Xss4m"` in Linux). This will instruct the JVM to set the thread stack size to `4m`, which is required for the proper compilation of this project. Check [this page](http://maven.apache.org/configure.html) for more information on how to configure Maven. Afterwards, execute the following command on a terminal:

```sh
mvn package
```

This will compile this project and generate the binaries in the `target` folder. After Maven reports a `BUILD SUCCESS`, you can start this stand-alone viewer like so:

```sh
java -jar target/experiment-graph-gui-<version>-jar-with-dependencies.jar
```

## License
This project is licensed under an MIT License:

* https://github.com/qbicsoftware/experiment-graph-gui/blob/development/LICENSE

It is based on third party code and you have to consider the corresponding licenses as well:

* javafx-d3 => MIT: https://github.com/stefaneidelloth/javafx-d3/blob/master/LICENSE
* d3.js => BSD: https://github.com/mbostock/d3/blob/master/LICENSE
* dagre => MIT: https://github.com/dagrejs/dagre/blob/master/LICENSE
* ISAcreator => CPAL: https://github.com/ISA-tools/ISAcreator/blob/master/LICENSE.txt


[maven]: https://maven.apache.org/

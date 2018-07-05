# Experiment Graph

[![Build Status](https://travis-ci.org/qbicsoftware/experiment-graph-gui.svg?branch=development)](https://travis-ci.org/qbicsoftware/experiment-graph-gui)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/experiment-graph-gui/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/experiment-graph-gui)

Experiment Graph - Parses experimental designs and displays a graph summarizing nodes by their experimental factors

## Author
Created by Andreas Friedrich (andreas.friedrich@qbic.uni-tuebingen.de).

## Description
This viewer summarizes sample sources and extracted samples based on similarity of independent variables, enabling a quick grasp of the scientific question at the core of the experiment even for large experiments. 

## How to Install
### Downloading a release


### Compiling the sources
You will need the following tools:
* The latest version of [Apache Maven](maven). 
* A Java Software Development Kit (JDK) (compatible with Java 8). 
* JavaFX dependencies (JavaFX installation depends on operating system and whether you are using Oracle's JDK or OpenJDK).

You will also need to set the `MAVEN_OPTS` environment variable to contain the Java Virtual Machine (JVM) option `-Xss4m` (i.e., `export MAVEN_OPTS="-Xss4m"` in Linux). This will instruct the JVM to set the thread stack size to `4m`, which is required for the proper compilation of this project. Check [this page](http://maven.apache.org/configure.html) for more information on how to configure Maven. Afterwards, execute the following command on a terminal:

```sh
mvn package
```

This will compile this project and generate the binaries in the `target` folder. After Maven reports a `BUILD SUCCESS`, you can execute this stand-alone viewer like so:

```sh
java -jar target/experiment-graph-gui-<version>-jar-with-dependencies.jar
```

For instance, for version `0.1.0`, you would execute:

```sh
java -jar target/experiment-graph-gui-0.1.0-jar-with-dependencies.jar
```

## License

This project is licensed under an MIT License:

* https://github.com/qbicsoftware/experiment-graph-gui/blob/development/LICENSE

It is based on third party code and you have to consider the corresponding licenses as well:

* javafx-d3 => MIT: https://github.com/stefaneidelloth/javafx-d3/blob/master/LICENSE
* d3.js => BSD: https://github.com/mbostock/d3/blob/master/LICENSE
* dagre => MIT: https://github.com/dagrejs/dagre/blob/master/LICENSE


[maven]: https://maven.apache.org/
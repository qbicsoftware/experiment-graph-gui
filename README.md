# Experiment Graph

[![Build Status](https://travis-ci.org/qbicsoftware/experiment-graph-gui.svg?branch=development)](https://travis-ci.org/qbicsoftware/experiment-graph-gui)[![Code Coverage]( https://codecov.io/gh/qbicsoftware/experiment-graph-gui/branch/development/graph/badge.svg)](https://codecov.io/gh/qbicsoftware/experiment-graph-gui)

Experiment Graph, version 0.0.9-SNAPSHOT - Parses experimental designs and displays a graph summarizing nodes by their experimental factors

## Author

Created by Andreas Friedrich (andreas.friedrich@qbic.uni-tuebingen.de).

## Description

## How to Install
If you are compiling this project, you will need to set the `MAVEN_OPTS` environment variable to contain the Java Virtual Machine (JVM) option `-Xss4m` (i.e., `export MAVEN_OPTS="-Xss4m"` in Linux). This will instruct the JVM to set the thread stack size to `4m`, which is required for the proper compilation of this project.

Check [this page](http://maven.apache.org/configure.html) for more information on how to configure Maven.

## License

This project is licensed under an MIT License:

* https://github.com/qbicsoftware/experiment-graph-gui/blob/development/LICENSE

It is based on third party code and you have to consider the corresponding licenses as well:

* javafx-d3 => MIT: https://github.com/stefaneidelloth/javafx-d3/blob/master/LICENSE
* d3.js => BSD: https://github.com/mbostock/d3/blob/master/LICENSE
* dagre => MIT: https://github.com/dagrejs/dagre/blob/master/LICENSE

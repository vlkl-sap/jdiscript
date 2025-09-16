#!/bin/bash

set -e

DIR="$(dirname "$(readlink -f "$0")")"


cd "$DIR/jdiscript"

javac \
  -sourcepath src/main/java/ \
  -d target/classes/ \
  src/main/java/org/jdiscript/JDIScript.java \
  src/main/java/org/jdiscript/util/*.java

#  --add-exports jdk.jdi/com.sun.tools.example.debug.expr=ALL-UNNAMED \




cd "$DIR/example"

javac -g  \
  -cp .:../jdiscript/target/classes/ \
  -sourcepath src/main/java/ \
  -d target/classes/ \
  src/main/java/org/jdiscript/example/*.java


#  --patch-module jdk.jdi="$HOME/jdk/src/jdk.jdi/share/classes/" \

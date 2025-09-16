#!/bin/bash

set -e

DIR="$(dirname "$(readlink -f "$0")")"


cd "$DIR/jdb"

javac -g  \
  -sourcepath src/main/java/ \
  -d target/classes/ \
  src/main/java/com/sun/tools/example/debug/tty/*.java


cd "$DIR/jdiscript"

javac \
  --add-exports jdk.jdi/com.sun.tools.example.debug.expr=ALL-UNNAMED \
  -cp .:../jdb/target/classes/ \
  -sourcepath src/main/java/ \
  -d target/classes/ \
  src/main/java/org/jdiscript/JDIScript.java \
  src/main/java/org/jdiscript/util/*.java


cd "$DIR/example"

javac -g  \
  -cp .:../jdiscript/target/classes/ \
  -sourcepath src/main/java/ \
  -d target/classes/ \
  src/main/java/org/jdiscript/example/*.java


#  --patch-module jdk.jdi="$HOME/jdk/src/jdk.jdi/share/classes/" \

#!/bin/bash

set -e
#set -x

DIR="$(dirname "$(readlink -f "$0")")"

cd "$DIR"


java -cp jdiscript/target/classes:example/target/classes:jdb/target/classes \
  --add-exports jdk.jdi/com.sun.tools.example.debug.expr=ALL-UNNAMED \
  "$@"


#  --patch-module jdk.jdi="$HOME/jdk/src/jdk.jdi/share/classes/" \

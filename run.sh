#!/bin/bash
echo "Compiling Java files..."
javac -d bin src/*.java src/models/*.java src/environment/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful! Starting Simulation..."
    java -cp bin MainController
else
    echo "Compilation failed!"
fi

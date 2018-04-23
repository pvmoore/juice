# JUICE

Java UI Component Engine

### Dependencies

- LWJGL https://www.lwjgl.org/
- JOML https://github.com/JOML-CI/JOML

### Building (on Windows)

This assumes you have a terminal window open in the working directory. Adjust as necessary.
Also, the code requires Java 10.

```shell
@echo off
set LWJGL=path to lwjgl jars directory
set JOML=path to joml jars directory
 
set JUICE_INC=%LWJGL%/lwjgl.jar;%LWJGL%/lwjgl-glfw.jar;%LWJGL%/lwjgl-opengl.jar;%JOML%/joml-1.9.9.jar
 
@echo Compiling source
cd src
javac -d ../bin/ -classpath %JUICE_INC% -sourcepath . juice/*.*
 
@echo Generating juice.jar
cd ..\bin
jar cf ../juice.jar juice
cd ..
```

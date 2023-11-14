rem First use Spring to package it into a jar and then use jpackage to package it into an exe.
set JDKHOME=D:\Java\jdk-17
%JDKHOME%\bin\jpackage.exe --name fx-17-demo --input target\dist --main-jar fx-17-demo.jar --type app-image --dest target
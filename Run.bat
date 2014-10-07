@echo off

SETLOCAL

set javapgm="C:\Program Files\Java\jre7\bin\java"
set wkspace=D:\DevFiles\Java\WorkSpaces\Main\
set genJar=%wkspace%\GenProject\jars\GenProject-1.1-complete.jar

cd /d %wkspace%

%javapgm% -jar %genJar% -c -g Json -n net.certiv.json -p %wkspace%MyJsonProject

timeout 5

@echo off

SETLOCAL

set javapgm="C:\Program Files\Java\jre7\bin\java"
set wkspace="D:\DevFiles\Java\WorkSpaces\Main\"

cd /d %wkspace%

%javapgm% -jar GenProject-1.1-complete.jar -c -g Json -n net.certiv.json -p %wkspace%MyJsonProject
@echo off
rem Initialize a new project

SETLOCAL
set STARTTIME=%TIME%

rem edit these 4 values to define a new project
set wkspace=D:\DevFiles\Java\WorkSpaces\Main
set projectName=TomlTest
set packageName=net.certiv.toml
set grammarName=Toml

rem well-known locations
set ruleSet=%wkspace%\GenProject\GenProjectRuleSet.json
set projConfigFile=%workspace%\%projectName%\%grammarName%GenConfig.json
set genprjar=D:\DevFiles\Java\WorkSpaces\Main\GenProject\jars\GenProject-2.0-complete.jar
set antlrjar=D:\DevFiles\Java\WorkSpaces\Main\GenProject\lib\antlr-4.4-complete.jar
set javahome=C:\Program Files\Java\jre7
set javapgm="%javahome%\bin\java"

set CLASSPATH=%genprjar%;%antlrjar%;%CLASSPATH%

cd /d %wkspace%
%javapgm% net.certiv.antlr.project.gen.GenProject -i -g %grammarName% -n %packageName% -p %wkspace%\%projectName% -r %ruleSet% 

set ENDTIME=%TIME%
set /A STARTTIME=(1%STARTTIME:~6,2%-100)*100 + (1%STARTTIME:~9,2%-100)
set /A ENDTIME=(1%ENDTIME:~6,2%-100)*100 + (1%ENDTIME:~9,2%-100)

if %ENDTIME% LSS %STARTTIME% (
	set /A DURATION=%STARTTIME%-%ENDTIME%
) else (
	set /A DURATION=%ENDTIME%-%STARTTIME%
)

set /A SECS=%DURATION% / 100
set /A REMAINDER=%DURATION% %% 100
echo %SECS%.%REMAINDER% s
ENDLOCAL

timeout 4

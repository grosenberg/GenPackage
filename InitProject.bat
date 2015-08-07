@echo off
rem Initialize a new project

SETLOCAL
set STARTTIME=%TIME%

rem edit these 5 values to define a new project
set javahome=C:\Program Files\Java\jre1.8
set workspace=D:\DevFiles\Java\WorkSpaces\Main
set projectName=net.certiv.testx
set packageName=net.certiv.testx
set grammarName=TestX

rem well-known locations
set ruleSet=%workspace%\GenProject\GenProjectRuleSet.json
set projConfigFile=%workspace%\%projectName%\%grammarName%GenConfig.json
set genprjar=%workspace%\GenProject\jars\GenProject-2.2-complete.jar
set antlrjar=%workspace%\GenProject\lib\antlr-4.5.1-complete.jar
set javapgm="%javahome%\bin\java"

set CLASSPATH=%genprjar%;%antlrjar%;%CLASSPATH%

cd /d %workspace%
%javapgm% net.certiv.antlr.project.gen.GenProject -i -g %grammarName% -n %packageName% -p %workspace%\%projectName% -r %ruleSet%

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

rem timeout 4
pause

@if not exist pom.xml goto to_exit

CALL mvn clean package

if not exist out mkdir out
del BitHeroes.jar >nul 2>&1
echo F|xcopy target\ReRun-*-jar-with-dependencies.jar BitHeroes.jar /Y
if not exist user-config.properties echo. 2>user-config.properties

rem Generating mini client
del mini-game-on-chrome.bat >nul 2>&1
java -jar BitHeroes.jar client

:to_exit
	rem Wrong working directory
	@exit /b 2
@if not exist pom.xml goto to_exit

@CALL mvn clean package

@if not %errorlevel%==0 (
    @echo ** ERROR ** maven build failure
	pause
    goto EXIT_1
)

@if not exist out mkdir out
@del BitHeroes.jar >nul 2>&1
@echo F|xcopy target\99bot-*-jar-with-dependencies.jar BitHeroes.jar /Y
@if not exist user-config.properties echo. 2>user-config.properties

@if not exist im.dev (
    @rem Generating mini client
    @call client.bat
)

:to_exit
	@rem Wrong working directory
	@exit /b 2
	
:EXIT_1
	@exit /b 1

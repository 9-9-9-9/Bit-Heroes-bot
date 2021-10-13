@if not exist pom.xml (
	rem Wrong working directory
    goto L_EXIT2
)

@CALL mvn clean package

@if not %errorlevel%==0 (
    echo ** ERROR ** maven build failure
	pause
    goto L_EXIT1
)

@if not exist out mkdir out
@del BitHeroes.jar >nul 2>&1
@echo F|xcopy target\99bot-*-jar-with-dependencies.jar BitHeroes.jar /Y
@if not exist user-config.properties echo. 2>user-config.properties

@if not exist im.dev (
    rem Generating mini client
    @call client.bat
)
	
:L_EXIT1
	@exit /b 1

:L_EXIT2
	@exit /b 2

@if not exist BitHeroes.jar (
    @echo You are using 99bot from source code version which required to run build.bat in order to compile binary first
    @echo If you are not a developer and you only want to use the bot, please download the zip file with name download-this-file.zip from my website at download.bh99bot.com
    goto L_EXIT1
)

@where java >nul 2>nul
@if %errorlevel%==1 (
    @echo ** ERROR ** You have to install Java 8 in order to use this bot
	@echo You can download Java 8 from the following website: https://adoptium.net/?variant=openjdk8
	start "" https://bh99bot.com/require-java.html
    goto L_EXIT1
)

java -jar BitHeroes.jar %*
@SET EXIT_LVL=%errorlevel%
@if %EXIT_LVL% NEQ 0 (
    @echo exit code: %EXIT_LVL%
)

@goto L_EXIT0

:L_EXIT0
	@pause
	@exit /b 0

:L_EXIT1
	@pause
	@exit /b 1
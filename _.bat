@where java >nul 2>nul
@if %errorlevel%==1 (
    @echo ** ERROR ** You have to install Java 8 in order to use this bot
    @echo ** LOI ** Ban can cai dat Java 8 truoc khi su dung
	@echo You can download Java 8 from the following website: https://adoptium.net/?variant=openjdk8
	start "" https://adoptium.net/?variant=openjdk8
    goto L_EXIT
)

java -jar BitHeroes.jar %*

@goto L_EXIT

:L_EXIT
	@pause
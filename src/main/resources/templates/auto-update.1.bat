@echo off
rem %VERSION%
SETLOCAL

%COPY_SCRIPT%

del %ZIP_FILE% >nul 2>&1
del .*.zip >nul 2>&1
echo Update finished
start "" https://bh99bot.com/updated-success.html
pause
(goto) 2>nul & del "%~f0"
exit /b 0

:PATCH_FILE
    del %2 >nul 2>&1
    echo F|xcopy %1 %2 /Y
    if %errorlevel% NEQ 0 (
        echo ** ERROR ** Failed while attempting to patch the file
        echo %2
        echo from downloaded file
        echo %1
        echo ** ERROR ** Patch new update %VERSION% has failure, please update manually yourself by going to https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest
        echo Sorry for this inconvenient
        start "" https://bh99bot.com/updated-failure.html
        pause
        exit /b 1
    )
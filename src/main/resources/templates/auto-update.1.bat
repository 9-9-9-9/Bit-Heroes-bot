@echo off
SETLOCAL

%COPY_SCRIPT%

del %ZIP_FILE% >nul 2>&1
echo Update finished
pause

:PRINT_WARNING_AND_EXIT
  echo ** ERROR ** Failed while attempting to patch the file '%~2' from downloaded file '%~1
  echo ** ERROR ** Patch new update %VERSION% has failure, please update manually yourself by going to https://download.bh99bot.com
  echo Sorry for this inconvenient
  pause
  exit /b 1

:PATCH_FILE
  del %~2 >nul 2>&1
  echo F|xcopy %~1 %~2 /Y
  if not %errorlevel%==0 (
    CALL PRINT_WARNING_AND_EXIT %~1 , %~2
  )

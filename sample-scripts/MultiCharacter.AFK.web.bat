:: Do chaining tasks Quest, PVP, WB, GVG/Invasion/Expedition, TG, Raid and then exit after completed them all
:: https://github.com/9-9-9-9/Bit-Heroes-bot/wiki/Function-%22afk%22
:: Consider adding `--profile=YourProfileName` so you no longer needed to select profile manually
:: You can remove flag `--ear` to keep game online all day (but still gone if you got Disconnected)
:: With steam version of BH, refer to AFK.steam.bat file

:: Comment Out or Delete Unused Characters

:: Switch to Character #1
echo 'e' | call web.bot.bat change-character 1
echo 'e' | call web.bot.bat afk a --ear --profile=name1

:: Switch to Character #3
echo 'e' | call web.bot.bat change-character 2
echo 'e' | call web.bot.bat afk a --ear --profile=name2

:: Switch to Character #3
echo 'e' | call web.bot.bat change-character 3
echo 'e' | call web.bot.bat afk a --ear --profile=name3

:: Call itself to go in an infinite loop
call MultiCharacter.AFK.web.bat

:: Move this file to bot's folder in order to use (this file was distributed within `sample-script` folder so it unable to run)
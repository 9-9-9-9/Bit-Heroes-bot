#!/bin/bash
echo '%VERSION%'
print_warning_and_exit() {
  echo '** ERROR ** Failed while attempting to patch the file '$2' from downloaded file '$1
  echo '** ERROR ** Patch new update %VERSION% has failure, please update manually yourself by going to https://github.com/9-9-9-9/Bit-Heroes-bot/releases/latest'
  echo 'Sorry for this inconvenient'
  xdg-open https://bh99bot.com/updated-failure.html
  exit 1
}

patch_file() {
  SRC=$1
  DST=$2

  if [ -f $DST ]; then
    rm -f $DST
    echo 'Removed old file '$DST
  fi

  cp $SRC $DST

  err=$?
  if [ $err -ne 0 ]; then
    print_warning_and_exit %SRC% %DST%
  fi

  echo 'Copied new file'$DST
}

%COPY_SCRIPT%

rm -f %ZIP_FILE%
echo 'Update finished'
rm -f ./update-bot.sh
xdg-open https://bh99bot.com/updated-success.html
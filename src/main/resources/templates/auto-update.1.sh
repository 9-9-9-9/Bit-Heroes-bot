patch_file() {
  SRC=$1
  DST=$2

  if [ -f $DST ]; then
    rm -f $DST
    echo 'Removed old file '$DST
  fi

  cp $SRC $DST
}

print_warning() {
  echo '** ERROR ** Failed while attempting to patch the file '$2' from downloaded file '$1
  echo '** ERROR ** Patch new update %VERSION% has failure, please update manually yourself by going to https://download.bh99bot.com'
  echo 'Sorry for this inconvenient'
}

%COPY_SCRIPT%
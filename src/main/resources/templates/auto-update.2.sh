patch_file %SRC% %DST%
err=$?
if [ $err -ne 0 ]; then
  print_warning %SRC% %DST%
  exit 1
fi
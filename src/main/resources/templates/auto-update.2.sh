patch_file %SRC% %DST%
err=$?
if [ $err -ne 0 ]; then
  print_warning_and_exit %SRC% %DST%
fi
#!/bin/bash

BUNDLE_FOLDERS=("./bench/railsbench/bundle/"
		"./bench/erubi_rails/bundle/"
		"./bench/hexapdf/bundle/"
		"./bench/psych-load/bundle/"
		"./bench/mail/bundle/"
)

for d in ${BUNDLE_FOLDERS[@]}; do
	for f in `find $d -name .gitignore` ; do
		# echo '!*.so' >> $f
		# echo '!*.o' >> $f
		# echo '!*.c' >> $f
		echo '!.bundle/' >> $f
	done
done

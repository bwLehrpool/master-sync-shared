#!/bin/bash

WANT_VER="0.17.0"
if ! thrift --version | grep -qF "$WANT_VER"; then
	echo -n "Warning! You should be using Thrift $WANT_VER, but you have $(thrift --version), do you still want to continue? [y/N]: "
	read choice
	if [ "x${choice:0:1}" != "xy" ]; then
		echo "Aborting!"
		exit 0
	fi
	echo "OK, compiling..."
fi

echo "1) Masterserver + Satellite RPC"
[ -e gen-java ] && rm -r gen-java
if thrift --gen java src/main/thrift/bwlp.thrift; then
	# reset all files where only the @Generated line changed, so we don't pollute the git history too much
	for file in gen-java/org/openslx/bwlp/thrift/iface/*.java; do
		bn=$(basename "$file")
		if [ -e "src/main/java/org/openslx/bwlp/thrift/iface/$bn" ]; then
			diff -q \
				<(sed -r 's/_(i|iter|elem|list|map|key|val)[0-9]+/\1x/g;/Autogenerated by|@Generated|@Override/d' "$file") \
				<(sed -r 's/_(i|iter|elem|list|map|key|val)[0-9]+/\1x/g;/Autogenerated by|@Generated|@Override/d' "src/main/java/org/openslx/bwlp/thrift/iface/$bn")
			ret=$?
			[ "$ret" = 0 ] && continue
		fi
		cp -f "$file" "src/main/java/org/openslx/bwlp/thrift/iface/$bn"
		git add "src/main/java/org/openslx/bwlp/thrift/iface/$bn"
	done
	for file in src/main/java/org/openslx/bwlp/thrift/iface/*.java; do
		bn=$(basename "$file")
		[ -e "gen-java/org/openslx/bwlp/thrift/iface/$bn" ] || git rm "$file"
	done
	[ -e gen-java ] && rm -r gen-java
fi

echo "All done!"
exit 0



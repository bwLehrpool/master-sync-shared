#!/bin/bash

if ! thrift --version | grep -q "0\.9\.3"; then
	echo -n "Warning! You should be using Thrift 0.9.3, but you have $(thrift --version), do you still want to continue? [y/N]: "
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
				<(sed -r 's/_i[0-9]+/_ix/g;s/_iter[0-9]+/_iterx/g;s/_elem[0-9]+/_elemx/g;s/_list[0-9]+/_listx/g;/@Generated/d' "$file") \
				<(sed -r 's/_i[0-9]+/_ix/g;s/_iter[0-9]+/_iterx/g;s/_elem[0-9]+/_elemx/g;s/_list[0-9]+/_listx/g;/@Generated/d' "src/main/java/org/openslx/bwlp/thrift/iface/$bn")
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
fi

echo "All done!"
exit 0



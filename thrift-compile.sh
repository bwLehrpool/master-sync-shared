#!/bin/sh

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
	[ -e "src/main/java/org/openslx/bwlp/thrift/iface" ] && rm -rf src/main/java/org/openslx/bwlp/thrift/iface
	if ! cp -r gen-java/org src/main/java/; then
		echo "Error copying compiled files! Aborting!"
		exit 1
	fi
	# reset all files where only the @Generated line changed, so we don't pollute the git history too much
	for file in src/main/java/org/openslx/bwlp/thrift/iface/*.java; do
		TOTAL=$(git diff "$file" | wc -l)
		GENS=$(git diff "$file" | grep -E '^[\+\-]@Gen' | wc -l)
		if [ "$TOTAL" = "13" -a "$GENS" = "2" ]; then
			# Nothing but @Generated annotation changed
			git checkout "$file"
		fi
	done
fi

echo "All done!"
exit 0



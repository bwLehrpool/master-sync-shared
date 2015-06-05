#!/bin/sh

if ! thrift --version | grep -q "0\.9\.1"; then
	echo -n "Warning! You should be using Thrift 0.9.1, but you have $(thrift --version), do you still want to continue? [y/N]: "
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
fi

echo "All done!"
exit 0



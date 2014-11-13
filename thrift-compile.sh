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
[ -e gen-java ] && rm -r gen-java
if thrift --gen java src/main/thrift/imagemaster.thrift; then
	[ -e "src/main/java/org/openslx/imagemaster/thrift/iface" ] && rm -rf src/main/java/org/openslx/imagemaster/thrift/iface
	cp -r gen-java/org src/main/java/ && echo "Success." && exit 0
fi

echo "Error!"
exit 1



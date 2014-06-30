#!/bin/sh

rm -r gen-java
thrift --gen java src/main/thrift/imagemaster.thrift && {
	rm -r src/main/java/org/openslx/imagemaster/thrift/iface
	cp -r gen-java/org src/main/java/
}



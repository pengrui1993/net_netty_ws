#!/bin/bash

java -Xms2G -Xmx2G -client -jar target\client-0.1.jar


java -Xms2G -Xmx2G -client -verbose:gc -Xlog:gc*:.\gc.log -jar target\server-0.1.jar
-verbose:class|module|gc|jni
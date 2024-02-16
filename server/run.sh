#!/bin/bash

java -Xms2G -Xmx2G -server -verbose:gc -Xlog:gc*:.\gc.log -jar target\server-0.1.jar

java -Xms2G -Xmx2G -server -verbose:gc -Xlog:gc -jar target\server-0.1.jar
-verbose:class|module|gc|jni
#!/bin/bash

JAVA_ARGS="-Xms20g -Xmx100g -classpath /home/kratos/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/kratos/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar:/home/kratos/IWB/Libraries/BenchScripts"
#------------------------------Variables To Perform Sweep On-----------------------#
DataPath=/home/kratos/SplitFiles
DataPath2=/home/kratos/SplitFiles100K
DataPath3=/home/kratos/SplitFiles10K

maxBufferedDocs=10
RAMBufferedSizeMB=16
disableIOThrottle='F'
numThreads=24

#------------------------------Start Parameter Sweep ------------------------------#

javac -classpath /home/kratos/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/kratos/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar A_OP_Benchmark.java

if [ $? -eq 0 ]
then
	sync
	sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'
	java ${JAVA_ARGS} A_OP_Benchmark $DataPath2 $maxBufferedDocs $RAMBufferedSizeMB $numThreads $disableIOThrottle $1 >> ~/IWB/Results/laughingHyena.csv
fi

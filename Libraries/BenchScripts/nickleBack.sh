#!/bin/bash

JAVA_ARGS="-Xms20g -Xmx100g -classpath /home/kratos/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/kratos/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar:/home/kratos/IWB/Libraries/BenchScripts"
#------------------------------Variables To Perform Sweep On-----------------------#
DataPath=/home/kratos/hpSSD/SplitFiles
DataPath2=/home/kratos/hpSSD/SplitFiles100K
DataPath3=/home/kratos/hpSSD/SplitFiles10K
DataPath4=/home/kratos/hpSSD/SplitFiles10M
DataPath5=/home/kratos/hpSSD/SplitFiles100M
nT=24

maxBufferedDocs=1000
RAMBufferedSizeMB=4096
disableIOThrottle='F'
numThreads=2

#------------------------------Start Parameter Sweep ------------------------------#

javac -classpath /home/kratos/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/kratos/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar B_OP_Benchmark.java

if [ $? -eq 0 ]
then
	for ((i=1;i<=15;i++));
	do 
		for ((x=1;x<=10;x++));
		do 
			sync
			sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'
			java ${JAVA_ARGS} B_OP_Benchmark $DataPath $maxBufferedDocs $RAMBufferedSizeMB $numThreads $disableIOThrottle $1 >> ~/IWB/Results/laughingHyena.csv
		done
	numThreads=$((numThreads+2))
	done
	echo 'END OF TEST' >> ~/IWB/Results/laughingHyena.csv
	for ((e=1;e<=10;e++));
        do
                sync
                sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'
                java ${JAVA_ARGS} B_OP_Benchmark $DataPath3 $maxBufferedDocs $RAMBufferedSizeMB $nT $disableIOThrottle $1 >> ~/IWB/Results/laughingHyena.csv
        done
	echo 'END OF TEST' >> ~/IWB/Results/laughingHyena.csv
	for ((t=1;t<=10;t++));
        do
                sync
                sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'
                java ${JAVA_ARGS} B_OP_Benchmark $DataPath2 $maxBufferedDocs $RAMBufferedSizeMB $nT $disableIOThrottle $1 >> ~/IWB/Results/laughingHyena.csv
        done
	echo 'END OF TEST' >> ~/IWB/Results/laughingHyena.csv
	for ((p=1;p<=10;p++));
        do
                sync
                sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'
                java ${JAVA_ARGS} B_OP_Benchmark $DataPath $maxBufferedDocs $RAMBufferedSizeMB $nT $disableIOThrottle $1 >> ~/IWB/Results/laughingHyena.csv
        done
	echo 'END OF TEST' >> ~/IWB/Results/laughingHyena.csv
fi

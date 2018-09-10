#!/bin/bash


sync
sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'

javac -classpath /home/cc/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/cc/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar luceneBenchConcurrentv2.java

if [ $? -eq 0 ]
then
#	java -Xms80g -Xmx80g -classpath /home/cc/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/cc/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar:/home/cc/IWB/Libraries/BenchScripts luceneBenchConcurrent 100 2048 $1 
	

#sync
#sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'

	java -Xms80g -Xmx80g -classpath /home/cc/IWB/Libraries/Lucene/core/lucene-core-7.3.1.jar:/home/cc/IWB/Libraries/Lucene/queryparser/lucene-queryparser-7.3.1.jar:/home/cc/IWB/Libraries/BenchScripts luceneBenchConcurrentv2 /home/cc/250k_backup 10000 2048 1000 4 T 1000 48 $1 >> out

fi

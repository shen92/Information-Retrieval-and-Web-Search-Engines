rm -rf *.jar *.class
hadoop com.sun.tools.javac.Main InvertedIndexBigrams.java
jar cf invertedindexbigrams.jar InvertedIndexBigrams*.class
hadoop fs -copyFromLocal ./invertedindexbigrams.jar
hadoop fs -cp ./invertedindexbigrams.jar gs://dataproc-staging-us-west1-511501897297-wwplryjp/JAR

hadoop fs -getmerge gs://dataproc-staging-us-west1-511501897297-wwplryjp/devout ./output2.txt
hadoop fs -copyFromLocal ./output2.txt
hadoop fs -cp ./output2.txt gs://dataproc-staging-us-west1-511501897297-wwplryjp/

sort -o output2_sorted.txt output2.txt
grep -w '^computer science' output2_sorted.txt >> index.txt
grep -w '^information retrieval' output2_sorted.txt >> index.txt
grep -w '^power politics' output2_sorted.txt >> index.txt
grep -w '^los angeles' output2_sorted.txt >> index.txt
grep -w '^bruce willis' output2_sorted.txt >> index.txt
rm -rf *.jar *.class
hadoop com.sun.tools.javac.Main InvertedIndexJob.java
jar cf invertedindex.jar InvertedIndex*.class
hadoop fs -copyFromLocal ./invertedindex.jar
hadoop fs -cp ./invertedindex.jar gs://dataproc-staging-us-west1-511501897297-wwplryjp/JAR

hadoop fs -getmerge gs://dataproc-staging-us-west1-511501897297-wwplryjp/fullout ./output.txt
hadoop fs -copyFromLocal ./output.txt
hadoop fs -cp ./output.txt gs://dataproc-staging-us-west1-511501897297-wwplryjp/

sort -o output_sorted.txt output.txt
grep -w '^architecture' output_sorted.txt >> index.txt
grep -w '^technology' output_sorted.txt >> index.txt
grep -w '^temperature' output_sorted.txt >> index.txt
grep -w '^academics' output_sorted.txt >> index.txt
grep -w '^concurrent' output_sorted.txt >> index.txt
grep -w '^experiment' output_sorted.txt >> index.txt
grep -w '^catalogue' output_sorted.txt >> index.txt
grep -w '^hierarchy' output_sorted.txt >> index.txt
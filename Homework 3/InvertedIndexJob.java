import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndexJob {

	public static class InvertedIndexMapper
			extends Mapper<Object, Text, Text, Text> {

		// map(word, doc_id)
		private Text docId = new Text();
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] lineArr = line.split("\t", 2);
			docId.set(lineArr[0]); // Get docId for current line

			// replace any non-character(one or more occurrence) with space
			String content = lineArr[1].toLowerCase().replaceAll("[^a-z]", " ");
			content = content.replaceAll("\\s+", " ");
			StringTokenizer itr = new StringTokenizer(content);

			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, docId);
			}
		}
	}

	public static class InvertedIndexReducer
			extends Reducer<Text, Text, Text, Text> {

		private Text result = new Text();

		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			// for one word, in a document, count occurrence
			// map: {docId: count}
			Map<String, Integer> occurrenceMap = new HashMap<>();

			for (Text val : values) {
				occurrenceMap.put(val.toString(), occurrenceMap.getOrDefault(val.toString(), 0) + 1);
			}

			// Count occurrence: "DOC_ID:NUM_COUNT "
			String occurrenceCount = "";

			for (String docId : occurrenceMap.keySet()) {
				occurrenceCount += docId + ":" + occurrenceMap.get(docId) + " ";
			}

			result.set(occurrenceCount);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: Inverted Index <inputpath> <outputpath>");
			System.exit(-1);
		}

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Task1");
		job.setJarByClass(InvertedIndexJob.class);
		job.setMapperClass(InvertedIndexMapper.class);
		job.setReducerClass(InvertedIndexReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}
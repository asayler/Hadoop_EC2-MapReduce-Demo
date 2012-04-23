/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * By Andy Sayler
 * 04/2012
 *
 * Adopted from Apache Hadoop WordCount.java example
 * Adopted from Apache Hadoop WPiEstimator.java example
 *
 */

package org.asayler;

import java.io.*;
import java.lang.*;
import java.util.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

/**
 * This is a Hadoop Map/Reduce application.
 * It reads the text input files of Wikipedia article titles, one per line.
 * It then breaks each line into words using '_' deliminators
 * and counts the first word in each title.
 * The output is a locally sorted list of title first words and the 
 * count of how often they occurred.
 * 
 */
public class WikiTitleCount extends Configured implements Tool {
    
    public static class MapClass extends MapReduceBase
	implements Mapper<LongWritable, Text, Text, IntWritable> {
    
	private final static IntWritable one = new IntWritable(1);
	private Text word = new Text();
    
	public void map(LongWritable key, Text value, 
			OutputCollector<Text, IntWritable> output, 
			Reporter reporter) throws IOException {
	    String line = value.toString();
	    String[] titles = line.split("\\s+",0);
	    for (int x=0; x<titles.length; x++) {
		String[] titlewords = titles[x].split("_",0);
		/** grab first word */
		word.set(titlewords[0]);
		output.collect(word, one);
	    }
	}
    }
  
    public static class Reduce extends MapReduceBase
	implements Reducer<Text, IntWritable, Text, IntWritable> {
	
	public void reduce(Text key, Iterator<IntWritable> values,
			   OutputCollector<Text, IntWritable> output, 
			   Reporter reporter) throws IOException {
	    int sum = 0;
	    while (values.hasNext()) {
		sum += values.next().get();
	    }
	    output.collect(key, new IntWritable(sum));
	}
    }
    
    static int printUsage() {
	System.out.println("wikititlecount <input> <output>");
	ToolRunner.printGenericCommandUsage(System.out);
	return -1;
    }
  
    /**
     * The main driver for wikititlecount map/reduce program.
     * Invoke this method to submit the map/reduce job.
     * @throws IOException When there is communication problems with the 
     *                     job tracker.
     */
    public int run(String[] args) throws Exception {
	JobConf conf = new JobConf(getConf(), WikiTitleCount.class);
	JobClient client = new JobClient(conf);
	ClusterStatus cluster = client.getClusterStatus();	
		
	int num_maps = 1;
	int num_reducers = 1;

	conf.setJobName("wikititlecount");
 
	conf.setOutputKeyClass(Text.class);
	conf.setOutputValueClass(IntWritable.class);
    
	conf.setMapperClass(MapClass.class);        
	conf.setCombinerClass(Reduce.class);
	conf.setReducerClass(Reduce.class);
    
	conf.setInputFormat(TextInputFormat.class);
	conf.setOutputFormat(TextOutputFormat.class);
    
	/** Set Default Mappers */
	num_maps = (int) (cluster.getMaxMapTasks());

	/** Set Default Mappers */
	num_reducers = (int) (cluster.getMaxReduceTasks() * 0.9);

	List<String> other_args = new ArrayList<String>();
	for(int i=0; i < args.length; ++i) {
	    try {
		other_args.add(args[i]);
	    } catch (NumberFormatException except) {
		System.out.println("ERROR: Integer expected instead of " + args[i]);
		return printUsage();
	    } catch (ArrayIndexOutOfBoundsException except) {
		System.out.println("ERROR: Required parameter missing from " +
				   args[i-1]);
		return printUsage();
	    }
	}
	// Make sure there are exactly 2 parameters left.
	if (other_args.size() != 2) {
	    System.out.println("ERROR: Wrong number of parameters: " +
			       other_args.size() + " instead of 2.");
	    return printUsage();
	}
	FileInputFormat.setInputPaths(conf, other_args.get(0));
	FileOutputFormat.setOutputPath(conf, new Path(other_args.get(1)));
  
	/* Set Mappers and Reducer */
	conf.setNumMapTasks(num_maps);
	conf.setNumReduceTasks(num_reducers);
  
	JobClient.runJob(conf);
	return 0;
    }
    
    public static void main(String[] args) throws Exception {
	int res = ToolRunner.run(new Configuration(), new WikiTitleCount(), args);
	System.exit(res);
    }
    
}

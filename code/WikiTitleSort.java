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
 * www.andysayler.com
 * 04/2012
 *
 * Adopted from Apache Hadoop WordCount.java example
 * Adopted from Apache Hadoop PiEstimator.java example
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
 * This is a Hadoop Map/Reduce application for sorting <value> <count> files.
 * It reads the text input files, sorts by count, and then writes an output file
 * of thw same format.
 */
public class WikiTitleSort extends Configured implements Tool {
    
    public static class MapClass extends MapReduceBase
	implements Mapper<LongWritable, Text, IntWritable, Text> {
    
	private IntWritable cnt = new IntWritable();
	private Text word = new Text();
    
	public void map(LongWritable key, Text value, 
			OutputCollector<IntWritable, Text> output, 
			Reporter reporter) throws IOException {
	    String line = value.toString();
	    String[] values = line.split("\\s+",0);
	    word.set(values[0]);
	    cnt.set(Integer.parseInt(values[1]));
	    output.collect(cnt, word);
	}
    }
  
    public static class Reduce extends MapReduceBase
	implements Reducer<IntWritable, Text, Text, IntWritable> {
	
	public void reduce(IntWritable key, Iterator<Text> values,
			   OutputCollector<Text, IntWritable> output, 
			   Reporter reporter) throws IOException {
	    
	    while (values.hasNext()) {
		output.collect(values.next(), key);
	    }
	}
    
    }

    static int printUsage() {
	System.out.println("wikititlesort <input> <output>");
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
	JobConf conf = new JobConf(getConf(), WikiTitleSort.class);
	JobClient client = new JobClient(conf);
	ClusterStatus cluster = client.getClusterStatus();

	int num_maps = 1;
	final int num_reducers = 1;

	conf.setJobName("wikititlesort");
 
	conf.setMapperClass(MapClass.class);        
	conf.setReducerClass(Reduce.class);

	conf.setOutputKeyClass(IntWritable.class);
	conf.setOutputValueClass(Text.class);
        
	conf.setInputFormat(TextInputFormat.class);
	conf.setOutputFormat(TextOutputFormat.class);
    
	/** Set Default Mappers */
	num_maps = (int) (cluster.getMaxMapTasks());

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
	int res = ToolRunner.run(new Configuration(), new WikiTitleSort(), args);
	System.exit(res);
    }

}

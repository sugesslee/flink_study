package com.red.flink.java.course07;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * JavaWindowsProcessApp
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/06/17     red        -
 * </pre>
 *
 * @author red
 * @version 1.0.0 2019-06-17 09:35
 * @since 1.0.0
 */
public class JavaWindowsProcessApp {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStreamSource<String> text = env.socketTextStream("localhost", 9999);

		text.flatMap(new FlatMapFunction<String, Tuple2<Integer, Integer>>() {
			@Override
			public void flatMap(String s, Collector<Tuple2<Integer, Integer>> collector) throws Exception {
				String tokens[] = s.toLowerCase().split(",");

				for (String token : tokens) {
					if (token.length() > 0) {
						collector.collect(new Tuple2<>(1, Integer.parseInt(token)));
					}
				}
			}
		}).keyBy(0)
				.timeWindow(Time.seconds(5))
				.process(new ProcessWindowFunction<Tuple2<Integer, Integer>, Object, Tuple, TimeWindow>() {
					@Override
					public void process(Tuple tuple, Context context, Iterable<Tuple2<Integer, Integer>> elements,
										Collector<Object> out) throws Exception {
						System.out.println("------------");

						long count = 0;
						for (Tuple2<Integer, Integer> in : elements) {
							count++;
						}
						out.collect("Windows: " + context.window() + "count: " + count);
					}
				})
				.print()
				.setParallelism(1);

		env.execute("JavaWindowsProcessApp");
	}
}

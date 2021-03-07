package com.flink.example.batch;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.util.Collector;

/**
 * 单词统计
 * @author Jack House
 * @date 2021年03月07日 19:16:06
 */
public class WordCount {

    public static void main(String[] args) throws Exception {
        //解析命令行传过来的参数
        ParameterTool params = ParameterTool.fromArgs(args);

        //获取一个执行环境
        final ExecutionEnvironment evn = ExecutionEnvironment.getExecutionEnvironment();
        //读取输入的数据
        DataSet<String> dataSet = null;
        if (params.has("input")) {
            dataSet = evn.readTextFile(params.get("input"));
        } else {
            dataSet = WordCountData.getDefaultTextLineDataSet(evn);
        }

        //单词词频统计
        DataSet<Tuple2<String, Integer>> counts = dataSet.flatMap(new Tokenizer()).groupBy(0).sum(1);
        if (params.has("output")) {
            // 数据输出为csv 格式
            counts.writeAsCsv(params.get("output"), "\n", "");
            // 提交执行flink应用
            evn.execute("wordcount example");
        } else {
            //数据打印到控制台
            counts.print();
        }
    }

    public static final  class Tokenizer implements FlatMapFunction<String , Tuple2<String, Integer>> {

        @Override
        public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
            String[] tokens = s.toLowerCase().split("\\W+");
            for (String token : tokens) {
                collector.collect(new Tuple2<String, Integer>(token,1));
            }
        }
    }
}

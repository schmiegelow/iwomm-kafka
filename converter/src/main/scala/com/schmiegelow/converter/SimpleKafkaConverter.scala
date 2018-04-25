package com.schmiegelow.converter

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.KStream

object SimpleKafkaConverter extends LazyLogging {

  val configuration: Config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {

    val bootstrapServers = if (args.length > 0) args(0) else "localhost:9092"
    val builder = new StreamsBuilder

    val streamingConfig = {
      val settings = new Properties
      settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-function-scala-example")
      settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      // Specify default (de)serializers for record keys and for record values.
      settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
      settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass.getName)
      settings
    }

    createUppercaseTopology(builder, "topic-in", "topic-out")

    val stream: KafkaStreams = new KafkaStreams(builder.build(), streamingConfig)

    sys.addShutdownHook({
      stream.close()
    })

    stream.start()

  }

  def createUppercaseTopology(builder: StreamsBuilder, input: String, output: String): Unit = {

    // Read the input Kafka topic into a KStream instance.
    val textLines: KStream[String, Array[Byte]] = builder.stream(input)

    val uppercasedWithMapValues: KStream[String, Array[Byte]] = textLines.mapValues(new String(_).toUpperCase().getBytes())

    uppercasedWithMapValues.to(output)

  }
}
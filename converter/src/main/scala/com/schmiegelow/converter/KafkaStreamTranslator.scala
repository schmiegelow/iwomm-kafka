package com.schmiegelow.converter

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}


object KafkaStreamTranslator extends LazyLogging {

  val configuration: Config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {


    val bootstrapServers = if (args.length > 0) args(0) else "kafka01.internal-service:9092"
    val builder = new StreamsBuilder

    val streamingConfig = {
      val settings = new Properties
      settings.put(StreamsConfig.APPLICATION_ID_CONFIG, getClass.getName)
      settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      // Specify default (de)serializers for record keys and for record values.
      settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
      settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass.getName)
      settings
    }

    createTopology(builder, "articles", "translated")

    val stream: KafkaStreams = new KafkaStreams(builder.build(), streamingConfig)

    sys.addShutdownHook({
      stream.close()
    })

    stream.start()

  }

  def createTopology(builder: StreamsBuilder, input: String, output: String): Unit = {
    // Read the input Kafka topic into a KStream instance.
    val textLines: KStream[String, Array[Byte]] = builder.stream(input)

    val incomingValues: KStream[String, Array[Byte]] = textLines.mapValues(value => {

      val text = new String(value)
      val fromLanguage = detectLanguage(text)
      logger.info(s"Translating $text in $fromLanguage to English")
      translateText(text, fromLanguage).getTranslatedText.getBytes()
    })

    incomingValues.to(output)

  }

  def translateText(text: String, fromLanguage: String): Translation = {
    val translate = TranslateOptions.getDefaultInstance.getService
    // Translates some text into Russian
    translate.translate(text, TranslateOption.sourceLanguage(fromLanguage), TranslateOption.targetLanguage("en"))
  }

  def detectLanguage(text:String): String = {
    val translate = TranslateOptions.getDefaultInstance.getService
    translate.detect(text).getLanguage
  }
}

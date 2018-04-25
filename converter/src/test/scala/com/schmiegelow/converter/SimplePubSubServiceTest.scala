package com.schmiegelow.converter

import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent._
import scala.collection.JavaConverters._
import ExecutionContext.Implicits.global
import scala.io.{Codec, Source}
import scala.util.{Failure, Success}

class SimplePubSubServiceTest extends org.scalatest.FunSpec
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  it("Simple Kafka test") {

    val numInputMessages = 100
    val topic = "articles"
    val props = new Properties()
    props.put("bootstrap.servers", "kafka01.internal-service:9092")
    props.put("acks", "all");
    props.put("batch.size", numInputMessages.toString);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("group.id", this.getClass.getName)

    val consumer = new KafkaConsumer[String, String](props)

    consumer.subscribe(Collections.singletonList(topic))

    Given("A number of messages")

    val urls = UrlsCsvReader.getNewsArticles(numInputMessages).toSeq

    When(s"a list of $numInputMessages URLs is pushed to the input queue")

    val producer = new KafkaProducer[String, String](props)

    val future = Future {
      val records = Seq()
      val deadline = 60.seconds.fromNow

      while (deadline.hasTimeLeft) {
        records ++ consumer.poll(100).asScala
      }
      consumer.commitSync()
      records
    }

    urls.foreach(url => {
      println(s"producing ${url.url}")
      producer.send(new ProducerRecord(topic, url.url, url.name)).get()
    })

    producer.flush()
    producer.close()

    Then(s"We expect to receive $numInputMessages messages")

    future onComplete {
      case Success(records) => assert(records.length > numInputMessages)
      case Failure(t) => fail("An error has occured: " + t.getMessage)
    }

  }

}


final case class NewsArticleInfo(name: String, url: String)

object UrlsCsvReader {
  def getNewsArticles(n: Int = 100): Iterator[NewsArticleInfo] = {
    Source
      .fromURL(getClass.getResource("/2018-03-07_news_articles_to_crawl.csv"), Codec.formatted("UTF-8"))
      .getLines.slice(1, n + 1)
      .flatMap { line =>
        val tokens = line.split("\t")
        for {
          name <- tokens.headOption
          url <- tokens.lastOption
        } yield NewsArticleInfo(name, url)
      }
  }
}

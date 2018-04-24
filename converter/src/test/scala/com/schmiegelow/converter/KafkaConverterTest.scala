package com.schmiegelow.converter

import java.util.{Collections, Properties}

import com.madewithtea.mockedstreams.MockedStreams
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.Serdes
import org.scalatest._

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.io.{Codec, Source}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class KafkaConverterTest extends org.scalatest.FunSpec
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  it("should test a stream") {

    val input = Seq(("x", "v1"), ("y", "v2"))
    val exp = Seq(("x", "V1"), ("y", "V2"))
    val strings = Serdes.String()

    MockedStreams()
      .topology { builder => KafkaConverter.createTopology(builder) }
      .input("topic-in", strings, strings, input)
      .output("topic-out", strings, strings, exp.size) shouldEqual exp
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

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

class SimpleKafkaConverterTest extends org.scalatest.FunSpec
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  it("should test a stream") {

    val input = Seq(("x", "v1"), ("y", "v2"))
    val exp = Seq(("x", "V1"), ("y", "V2"))
    val strings = Serdes.String()

    MockedStreams()
      .topology { builder => SimpleKafkaConverter.createUppercaseTopology(builder, "topic-in", "topic-out") }
      .input("topic-in", strings, strings, input)
      .output("topic-out", strings, strings, exp.size) shouldEqual exp
  }
}

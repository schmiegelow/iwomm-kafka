package com.schmiegelow.converter

import com.google.cloud.translate.TranslateException
import com.madewithtea.mockedstreams.MockedStreams
import org.apache.kafka.common.serialization.Serdes
import org.scalatest._

class KafkaStreamTranslatorTest extends FunSpecLike with GivenWhenThen with Matchers {

  describe("KafkaStreamTranslatorTest") {

    it("should translateText") {

      Given("A text in Russian")

      val text = "Спасибо! Ваш заказ был размещен."

      When("It is passed to Translate")

      val translated = KafkaStreamTranslator.translateText(text, "ru")

      Then("It is translated to English")

      assert(translated.getTranslatedText() == "Thank you! Your order has been placed.")
    }


    it("should detect languages") {
      Given("A text in Russian")

      val text = "Спасибо! Ваш заказ был размещен."

      When("It is passed to Detect")

      val detected = KafkaStreamTranslator.detectLanguage(text)

      Then("It is detected as Russian")
      assert(detected == "ru")
    }

    it("should break on en to en translations") {
      Given("A text in English")

      val text = "A text in English"

      When("It is passed to Detect")

      assertThrows[TranslateException](KafkaStreamTranslator.translateText(text, "en"))


    }

    it("should simulate a stream of incoming foreign texts") {
      val input = Seq(("x", "Das ist ein test auf Deutsch"),
        ("y", "Ce ci est un test en Francais"),
        ("z", "This is a test in English"))
      val exp = Seq(("x", "This is a test in German"), ("y", "This is a test in French"))
      val bytes = Serdes.String()

      MockedStreams()
        .topology { builder => KafkaStreamTranslator.createTopology(builder, "topic-in", "topic-out") }
        .input("topic-in", bytes, bytes, input)
        .output("topic-out", bytes, bytes, exp.size) shouldEqual exp
    }
  }


}

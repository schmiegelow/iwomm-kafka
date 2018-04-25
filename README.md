# iwomm-kafka

This is the companion repo to the Kafka and Streams intro a IWOMM in London on April 25, 2018.

## To start a mini cluster on your machine, you'll need docker-compose and docker. just run

```docker-compose up```

to start the cluster.

The converter SBT project contains a few tests:

- SimplePubSubServiceTest requires a running cluster and produces and consumes messages from teh cluster
- SimpleKafkaServiceTests demonstrates a simple Kafka Streams application with Mocking Support
- KafkaStreamTranslatorTest demonstrates using streaming to connect to external APIs, in this case GoogleTranslate, for which you will need valid credentials

## KafkaStreamsTranslator is a simple KafkaStreams app
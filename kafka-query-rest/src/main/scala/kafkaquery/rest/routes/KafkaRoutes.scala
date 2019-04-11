package kafkaquery.rest.routes

import java.util.concurrent.ScheduledExecutorService

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import args4c.RichConfig
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import kafkaquery.connect.{Bytes, KafkaFacade, RichKafkaConsumer}
import kafkaquery.kafka.{KafkaEndpoints, StreamRequest}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.reactivestreams.Publisher

class KafkaRoutes(kafka: KafkaFacade, newStreamHandler: StreamRequest => Flow[Message, Message, NotUsed]) extends KafkaEndpoints with BaseRoutes with StrictLogging {

  val listTopicsRoute: Route = listTopics.listTopicsEndpoint.implementedBy { _ =>
    kafka.listTopics()
  }

  val pullLatestRoute: Route = pullLatest.pullEndpoint.implementedBy {
    case (topic, offset, limit) => kafka.pullLatest(topic, offset, limit)
  }

  val streamRoute: Route = {
    stream.streamEndpoint.request { query: StreamRequest =>
      val data: Publisher[ConsumerRecord[String, Bytes]] = kafka.stream(query)

      val handler = newStreamHandler(query)
      handleWebSocketMessages(handler)
    }
  }

  def routes: Route = streamRoute ~ listTopicsRoute ~ pullLatestRoute
}

object KafkaRoutes {
  import args4c.implicits._
  def apply(rootConfig: Config)(implicit mat: ActorMaterializer, scheduler: ScheduledExecutorService): KafkaRoutes = forRoot(rootConfig)

  private def forRoot(rootConfig: RichConfig)(implicit mat: ActorMaterializer, scheduler: ScheduledExecutorService): KafkaRoutes = {
    val consumerConfig = rootConfig.kafkaquery.consumer.config

    val consumer: RichKafkaConsumer[String, Bytes] = RichKafkaConsumer.byteArrayValues(rootConfig.config)

    val facade = KafkaFacade(
      kafka = consumer,
      pollTimeout = consumerConfig.asFiniteDuration("pollTimeout"),
      timeout = consumerConfig.asFiniteDuration("timeout")
    )
    new KafkaRoutes(facade, _ => SocketAdapter.greeterWebSocketService)
  }
}

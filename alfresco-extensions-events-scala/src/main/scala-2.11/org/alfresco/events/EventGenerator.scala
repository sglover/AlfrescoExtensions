package org.alfresco.events

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationFeature, SerializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.activemq.camel.component.ActiveMQComponent
import org.apache.camel.component.amqp.AMQPComponent
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.scala.dsl.builder.{RouteBuilder, RouteBuilderSupport}
import org.apache.camel.{CamelContext, Exchange}

import scala.collection.JavaConverters._

/**
  * Created by sglover on 29/11/2015.
  */
class EventGenerator extends RouteBuilderSupport {
  val context:CamelContext = new DefaultCamelContext()

  context.addComponent("amqp", AMQPComponent.amqpComponent("amqp://localhost:61616"))
  context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616"))
//  val dataFormat =
  val routeBuilder = new RouteBuilder {
    from("direct:A")
//      .marshal()
      .process(toJson _)
//      .to("activemq:topic:alfresco.repo.events.nodes?jmsMessageType=Text")
      .to("activemq:topic:VirtualTopic.alfresco.repo.events.nodes?jmsMessageType=Text")
  }
  context.addRoutes(routeBuilder)
  context.start()

  val producer = context.createProducerTemplate()

  def sendBlock(): Unit = {
    val body = Block()
    val headers = Map[String, Object]().asJava
    producer.sendBodyAndHeaders("direct:A", body, headers)
  }

  def sendUnBlock(): Unit = {
    val body = Unblock()
    val headers = Map[String, Object]().asJava
    producer.sendBodyAndHeaders("direct:A", body, headers)
  }

  private def toJson(exchange: Exchange): Unit = {
    val body = exchange.getIn.getBody()
    val str = body match {
      case block:Block => {
        "{ \"@class\":\"org.alfresco.service.subscription.api.ControlMessage\", \"command\": \"" + block.command + "\"}"
      }
      case unblock:Unblock => {
        "{ \"@class\":\"org.alfresco.service.subscription.api.ControlMessage\", \"command\": \"" + unblock.command + "\"}"
      }
      case _ => {
        throw new RuntimeException()
      }
    }
    exchange.getOut.setBody(str)
  }

  def shutdown(): Unit = {
    context.stop()
  }
}

object EventGenerator {
  def apply(): EventGenerator = {
    new EventGenerator()
  }
}

case class Block()
{
  val command = "BLOCK"
}

case class Unblock()
{
  val command = "UNBLOCK"
}
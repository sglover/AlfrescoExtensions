package org.alfresco.events

import org.scalatest.{BeforeAndAfter, FunSuite}

/**
  * Created by sglover on 29/11/2015.
  */
class TestGenerator extends FunSuite with BeforeAndAfter {

  this.test("test1") {
    val gen = EventGenerator()
//    gen.sendBlock
    gen.sendUnBlock
    gen.shutdown()
  }

}

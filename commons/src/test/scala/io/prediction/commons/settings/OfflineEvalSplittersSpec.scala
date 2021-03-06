package io.prediction.commons.settings

import org.specs2._
import org.specs2.specification.Step
import com.mongodb.casbah.Imports._

class OfflineEvalSplittersSpec extends Specification { def is =
  "PredictionIO OfflineEvalSplitters Specification"                           ^
                                                                              p^
  "OfflineEvalSplitters can be implemented by:"                               ^ endp^
    "1. MongoOfflineEvalSplitters"                                            ^ mongoOfflineEvalSplitters^end

  def mongoOfflineEvalSplitters =                                             p^
    "MongoOfflineEvalSplitters should"                                        ^
      "behave like any OfflineEvalSplitters implementation"                   ^ offlineEvalSplitters(newMongoOfflineEvalSplitters)^
                                                                              Step(MongoConnection()(mongoDbName).dropDatabase())

  def offlineEvalSplitters(splitters: OfflineEvalSplitters) = {               t^
    "create an OfflineEvalSplitter"                                           ! insert(splitters)^
    "get two OfflineEvalSplitters"                                            ! getByEvalid(splitters)^
    "update an OfflineEvalSplitter"                                           ! update(splitters)^
    "delete an OfflineEvalSplitter"                                           ! delete(splitters)^
                                                                              bt
  }

  val mongoDbName = "predictionio_mongoofflineevalsplitters_test"
  def newMongoOfflineEvalSplitters = new mongodb.MongoOfflineEvalSplitters(MongoConnection()(mongoDbName))

  def insert(splitters: OfflineEvalSplitters) = {
    val splitter = OfflineEvalSplitter(
      id = 0,
      evalid = 123,
      name = "insert",
      infoid = "insert",
      settings = Map())
    val i = splitters.insert(splitter)
    splitters.get(i) must beSome(splitter.copy(id = i))
  }

  /**
   * insert a few and get by evalid
   */
  def getByEvalid(splitters: OfflineEvalSplitters) = {
    val obj1 = OfflineEvalSplitter(
      id = -1,
      evalid = 15,
      name = "abc",
      infoid = "splitter-getByEvalid1",
      settings = Map(("abc1" -> 6), ("bar1" -> "foo1 foo2"), ("bar1b" -> "foo1b"))
    )
    val obj2 = OfflineEvalSplitter(
      id = -1,
      evalid = 15,
      name = "def",
      infoid = "splitter-getByEvalid2",
      settings = Map(("abc2" -> 0), ("bar2" -> "foox"))
    )

    val id1 = splitters.insert(obj1)
    val id2 = splitters.insert(obj2)

    val it = splitters.getByEvalid(15)

    val it1 = it.next()
    val it2 = it.next()
    val left = it.hasNext // make sure it has 2 only

    it1 must be equalTo(obj1.copy(id = id1)) and
      (it2 must be equalTo(obj2.copy(id = id2))) and
      (left must be_==(false))

  }


  def update(splitters: OfflineEvalSplitters) = {
    val id = splitters.insert(OfflineEvalSplitter(
      id = 0,
      evalid = 345,
      name = "update",
      infoid = "update",
      settings = Map()
    ))
    val updatedSplitter = OfflineEvalSplitter(
      id = id,
      evalid = 345,
      name = "updated",
      infoid = "updated",
      settings = Map("set1" -> "dat1", "set2" -> "dat2")
    )
    splitters.update(updatedSplitter)
    splitters.get(id) must beSome(updatedSplitter)
  }

  def delete(splitters: OfflineEvalSplitters) = {
    val id = splitters.insert(OfflineEvalSplitter(
      id = 0,
      evalid = 456,
      name = "deleteByIdAndAppid",
      infoid = "deleteByIdAndAppid",
      settings = Map("x" -> "y")
    ))
    splitters.delete(id)
    splitters.get(id) must beNone
  }
}

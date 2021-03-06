package io.prediction.commons.settings.mongodb

import io.prediction.commons.settings.{OfflineEval, OfflineEvals}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.github.nscala_time.time.Imports._

class MongoOfflineEvals(db: MongoDB) extends OfflineEvals {

  private val emptyObj = MongoDBObject()
  private val offlineEvalColl = db("offlineEvals")
  private val seq = new MongoSequences(db)
  private def genNextId = seq.genNext("offlineEvalid")
  private val getFields = MongoDBObject( // fields to be read
    "engineid" -> 1,
    "name" -> 1,
    "iterations" -> 1,
    "trainingsize" -> 1,
    "testsize" -> 1,
    "timeorder" -> 1,
    "autotune" -> 1,
    "createtime" -> 1,
    "starttime" -> 1,
    "endtime" -> 1
  )

  RegisterJodaTimeConversionHelpers()

  private def dbObjToOfflineEval(dbObj: DBObject) = {
    OfflineEval(
      id = dbObj.as[Int]("_id"),
      engineid = dbObj.as[Int]("engineid"),
      name = dbObj.as[String]("name"),
      iterations = dbObj.as[Int]("iterations"),
      trainingsize = dbObj.as[Int]("trainingsize"),
      testsize = dbObj.as[Int]("testsize"),
      timeorder = dbObj.as[Boolean]("timeorder"),
      autotune = dbObj.as[Boolean]("autotune"),
      createtime = dbObj.getAs[DateTime]("createtime"),
      starttime = dbObj.getAs[DateTime]("starttime"),
      endtime = dbObj.getAs[DateTime]("endtime")
    )
  }

  class MongoOfflineEvalIterator(it: MongoCursor) extends Iterator[OfflineEval] {
    def next = dbObjToOfflineEval(it.next)
    def hasNext = it.hasNext
  }

  /** Insert an OfflineEval and return id (id of the offlineEval parameter is not used) */
  def insert(offlineEval: OfflineEval): Int = {
    val id = genNextId

    // required fields
    val obj = MongoDBObject(
      "_id" -> id,
      "engineid" -> offlineEval.engineid,
      "name" -> offlineEval.name,
      "iterations" -> offlineEval.iterations,
      "trainingsize" -> offlineEval.trainingsize,
      "testsize" -> offlineEval.testsize,
      "timeorder" -> offlineEval.timeorder,
      "autotune" -> offlineEval.autotune)

    // option fields
    val createtimeObj = offlineEval.createtime.map(x => MongoDBObject("createtime" -> x)).getOrElse(emptyObj)
    val starttimeObj = offlineEval.starttime.map(x => MongoDBObject("starttime" -> x)).getOrElse(emptyObj)
    val endtimeObj = offlineEval.endtime.map(x => MongoDBObject("endtime" -> x)).getOrElse(emptyObj)

    val optObj = createtimeObj ++ starttimeObj ++ endtimeObj

    offlineEvalColl.insert(obj ++ optObj)

    id
  }

  def get(id: Int): Option[OfflineEval] = {
    // NOTE: _id field is always returned although it is not specified in getFields.
    offlineEvalColl.findOne(MongoDBObject("_id" -> id), getFields) map { dbObjToOfflineEval(_) }
  }

  def getByEngineid(engineid: Int): Iterator[OfflineEval] = new MongoOfflineEvalIterator(
    offlineEvalColl.find(MongoDBObject("engineid" -> engineid), getFields).sort(MongoDBObject("name" -> 1))
  )

  def update(offlineEval: OfflineEval) = {
    val obj = MongoDBObject(
      "_id" -> offlineEval.id,
      "engineid" -> offlineEval.engineid,
      "name" -> offlineEval.name,
      "iterations" -> offlineEval.iterations,
      "trainingsize" -> offlineEval.trainingsize,
      "testsize" -> offlineEval.testsize,
      "timeorder" -> offlineEval.timeorder,
      "autotune" -> offlineEval.autotune)

    // option fields
    val createtimeObj = offlineEval.createtime.map(x => MongoDBObject("createtime" -> x)).getOrElse(emptyObj)
    val starttimeObj = offlineEval.starttime.map(x => MongoDBObject("starttime" -> x)).getOrElse(emptyObj)
    val endtimeObj = offlineEval.endtime.map(x => MongoDBObject("endtime" -> x)).getOrElse(emptyObj)

    val optObj = createtimeObj ++ starttimeObj ++ endtimeObj

    offlineEvalColl.update(MongoDBObject("_id" -> offlineEval.id), obj ++ optObj)
  }

  def delete(id: Int) = {
    offlineEvalColl.remove(MongoDBObject("_id" -> id))
  }
}

package io.prediction.tools.settingsinit

import io.prediction.commons._
import io.prediction.commons.settings.{OfflineEvalMetricInfo, OfflineEvalSplitterInfo}

import scala.reflect.ClassTag
import scala.util.parsing.json.JSON

/** Extractors: http://stackoverflow.com/questions/4170949/how-to-parse-json-in-scala-using-standard-scala-classes */
class CC[T : ClassTag] { def unapply(a: Any)(implicit e: ClassTag[T]): Option[T] = {
  try { Some(e.runtimeClass.cast(a).asInstanceOf[T]) } catch { case _: Throwable => None } }
}

object M extends CC[Map[String, Any]]
object MSS extends CC[Map[String, String]]
object SS extends CC[Seq[String]]
object OSS extends CC[Option[Seq[String]]]
object S extends CC[String]
object OS extends CC[Option[String]]
object D extends CC[Double]
object B extends CC[Boolean]

object SettingsInit {
  val config = new Config()

  def main(args: Array[String]) {
    val offlineEvalSplitterInfos = config.getSettingsOfflineEvalSplitterInfos
    val offlineEvalMetricInfos = config.getSettingsOfflineEvalMetricInfos

    val settingsFile = try { args(0) } catch { case e: Throwable =>
      println("Please specify the location of the initial settings file in the command line. Aborting.")
      sys.exit(1)
    }

    val settingsString = try { scala.io.Source.fromFile(settingsFile).mkString } catch { case e: Throwable =>
      println(s"Unable to open ${settingsFile}: ${e.getMessage}. Aborting.")
      sys.exit(1)
    }

    val settingsJson = JSON.parseFull(settingsString) getOrElse {
      println(s"Unable to parse initial settings file ${settingsFile}. Aborting.")
    }

    println("PredictionIO settings initialization starting")

    M.unapply(settingsJson) map { settings =>
      M.unapply(settings("offlineevalsplitterinfos")) map { infos =>
        println("Populating OfflineEvalSplitterInfos...")
        for {
          id <- infos.keys
          M(info) = infos(id)
          S(name) = info("name")
          SS(engineinfoids) = info("engineinfoids")
          OS(description) = info.get("description")
          OSS(commands) = info.get("commands")
          SS(paramorder) = info("paramorder")
          MSS(paramnames) = info("paramnames")
          MSS(paramdescription) = info("paramdescription")
          MSS(paramdefaults) = info("paramdefaults")
        } yield {
          val mi = OfflineEvalSplitterInfo(
            id = id,
            name = name,
            engineinfoids = engineinfoids,
            description = description,
            commands = commands,
            paramorder = paramorder,
            paramnames = paramnames,
            paramdescription = paramdescription,
            paramdefaults = paramdefaults)

          offlineEvalSplitterInfos.get(id) map { m =>
            println(s"Updating OfflineEvalSplitterInfo ID: ${id}")
            offlineEvalSplitterInfos.update(mi)
          } getOrElse {
            println(s"Adding OfflineEvalSplitterInfo ID: ${id}")
            offlineEvalSplitterInfos.insert(mi)
          }
        }
      } getOrElse println("Cannot find any OfflineEvalSplitterInfo information. Skipping.")

      M.unapply(settings("offlineevalmetricinfos")) map { infos =>
        println("Populating OfflineEvalMetricInfos...")
        for {
          id <- infos.keys
          M(info) = infos(id)
          S(name) = info("name")
          SS(engineinfoids) = info("engineinfoids")
          OS(description) = info.get("description")
          OSS(commands) = info.get("commands")
          SS(paramorder) = info("paramorder")
          MSS(paramnames) = info("paramnames")
          MSS(paramdescription) = info("paramdescription")
          MSS(paramdefaults) = info("paramdefaults")
        } yield {
          val mi = OfflineEvalMetricInfo(
            id = id,
            name = name,
            engineinfoids = engineinfoids,
            description = description,
            commands = commands,
            paramorder = paramorder,
            paramnames = paramnames,
            paramdescription = paramdescription,
            paramdefaults = paramdefaults)

          offlineEvalMetricInfos.get(id) map { m =>
            println(s"Updating OfflineEvalMetricInfo ID: ${id}")
            offlineEvalMetricInfos.update(mi)
          } getOrElse {
            println(s"Adding OfflineEvalMetricInfo ID: ${id}")
            offlineEvalMetricInfos.insert(mi)
          }
        }
      } getOrElse println("Cannot find any OfflineEvalMetricInfo information. Skipping.")
    } getOrElse println("Root level is not an object. Aborting.")

    println("PredictionIO settings initialization finished")
  }
}

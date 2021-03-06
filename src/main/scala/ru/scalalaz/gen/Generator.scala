/*
 * Copyright 2016 Scalalaz Podcast Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.scalalaz.gen

import java.nio.file.{ Files, Path }
import java.nio.file.Paths

import cats.data.Validated.{ Invalid, Valid }
import cats.data.{ NonEmptyList, ValidatedNel }
import cats.implicits._
import parsing._
import writers._

trait GeneratorFs {

  val EOL = scala.util.Properties.lineSeparator

  def episodeFiles(dir: Path): List[Path] =
    fs.list(dir).filter(p => isMarkdown(p) && isEpisode(p))

  def isMarkdown(p: Path): Boolean = p.toFile.getName.endsWith(".md")

  def isEpisode(p: Path): Boolean = p.toFile.getName.startsWith("series-")

  def notEpisode(p: Path): Boolean = !isMarkdown(p)

  def eitherCatch[A](f: => A): Either[String, A] = eitherCatch(f, "")

  def eitherCatch[A](f: => A, descr: String): Either[String, A] =
    Either.catchNonFatal(f) match {
      case Left(e) =>
        Left(
            descr + "\n"
              + e.toString + "\n"
              + e.getStackTrace().mkString("", EOL, EOL)
        )
      case Right(v) => Right(v)
    }
}

class Generator(settings: SiteSettings, source: Path, target: Path)
    extends GeneratorFs {

  val targetRssPath = target.resolve("rss")

  def generate(): Either[String, Unit] = {
    for {
      _        <- prepare()
      episodes <- parse()
      _        <- eitherCatch(copyOther(), "Copy other resources: ")
      _        <- generateHtml(episodes)
      _        <- generateRss(episodes)
    } yield Right(())
  }

  /**
    * чистим/создаем нужные директории
    */
  def prepare(): Either[String, Unit] =
    for {
      _    <- eitherCatch(fs.clean(target))
      last <- eitherCatch(fs.createDir(targetRssPath))
    } yield last

  /**
    * копируем для лайка-генератора все обычные файлы
    */
  def copyOther(): Unit = {
    Seq("img", "css/layouts").foreach(d => {
      val from = source.resolve(d)
      val to   = target.resolve(d)
      fs.copyDir(from, to)
    })
  }

  def parse(): Either[String, List[EpisodeFile]] =
    episodeFiles(source.resolve("episodes"))
      .sortBy(_.getFileName.toString)
      .traverseU(parseEpisode) match {
      case Invalid(e) => Left(describeErrors(e))
      case Valid(ef)  => Right(ef)
    }

  def generateHtml(episodes: List[EpisodeFile]): Either[String, Unit] = {
    eitherCatch {
      val writer = new HTMLWriter(target.toString, settings.discusCode)
      writer.write(episodes)
    }
  }

  def generateRss(eps: List[EpisodeFile]): Either[String, Unit] = {
    val writer = new RSSWriter(targetRssPath.toString, ITunesInfo.Scalalaz)
    eitherCatch(writer.write(eps), "Generate rss: ")
  }

  def parseEpisode(p: Path): ValidatedNel[EpisodeParseError, EpisodeFile] = {
    val bytes   = Files.readAllBytes(p)
    val content = new String(bytes)
    EpisodeParser
      .fromString(content)
      .leftMap(e => FileParseError(p, e))
      .map(e => EpisodeFile(p, e))
      .toValidatedNel
  }

  def describeErrors(errors: NonEmptyList[EpisodeParseError]): String = {
    errors
      .map(e => s"Error occurred while parsing file:\n\t $e")
      .toList
      .mkString("\n")
  }

}

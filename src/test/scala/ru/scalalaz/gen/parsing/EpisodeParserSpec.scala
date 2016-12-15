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

package ru.scalalaz.gen.parsing

import java.time.LocalDate

import cats.data.Validated.Valid
import org.scalatest.{ FlatSpec, Inside, Matchers }

class EpisodeParserSpec extends FlatSpec with Matchers with Inside {

  val episodeStr = """
      |title=Episode#1
      |page=http://scalalaz.ru/series-01.html
      |date=2016-11-28
      |enc.url=http://scalalaz.ru/mp3/scalalaz-podcast-1.mp3
      |enc.length=6
      |----
      |### Yoyoyo!
      |it is a new episode!""".stripMargin

  it should "parse from string" in {
    val result = EpisodeParser.fromString(episodeStr)
    inside(result) {
      case Valid(episode) =>
        episode.сontent shouldBe "### Yoyoyo!\nit is a new episode!"

        val rss = episode.rss
        rss.title shouldBe "Episode#1"
        rss.page shouldBe "http://scalalaz.ru/series-01.html"

        rss.date shouldBe LocalDate.of(2016, 11, 28)

        rss.enclosure.url shouldBe "http://scalalaz.ru/mp3/scalalaz-podcast-1.mp3"
        rss.enclosure.length shouldBe 6
    }
  }
}
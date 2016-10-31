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

import java.time.LocalDateTime

import cats.Functor
import cats.data.Validated

case class Enclosure(url: String, `type`: String, length: Int)

case class RssItem(title: String,
                   description: String,
                   enclosure: Enclosure,
                   page: String,
                   date: LocalDateTime)

object RssItem {

  def fromMap(map: Map[String, String]): Validated[String, RssItem] = {

    def read(key:String): Validated[String, String] =
      map.get(key).toVa

    for {
      title
    }

  }
}

case class Episode(rssItem: RssItem, сontent: String)

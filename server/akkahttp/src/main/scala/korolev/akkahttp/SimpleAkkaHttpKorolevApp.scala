/*
 * Copyright 2017-2018 Aleksey Fomkin
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

package korolev.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import korolev.execution._

abstract class SimpleAkkaHttpKorolevApp(config: AkkaHttpServerConfig = null) {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  def service: AkkaHttpService

  def main(args: Array[String]): Unit = {
    val escapedConfig =
      if (config == null) AkkaHttpServerConfig()
      else config
    val route = service(escapedConfig)
    Http().bindAndHandle(route, "0.0.0.0", 8080)
    ()
  }
}

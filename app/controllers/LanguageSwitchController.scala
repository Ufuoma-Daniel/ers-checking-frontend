/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, Controller}
import config.ApplicationConfig
import play.api.Configuration
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}

class LanguageSwitchController @Inject()(
                                          configuration: Configuration,
                                          languageUtils: LanguageUtils,
                                          val messagesApi: MessagesApi
                                        ) extends LanguageController(configuration, languageUtils) {

  //private def langToCall(lang: String): (String) => Call = appConfig.routeToSwitchLanguage


  //private def languageMap: Map[String, Lang] = appConfig.languageMap

  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override def fallbackURL: String = routes.CheckingServiceController.startPage().url




}
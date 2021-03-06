/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.auth.{AuthAction, RequestWithOptionalEmpRef}
import models.ERSFileProcessingException
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{CsvFileProcessor, ProcessODSService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.CacheUtil

import scala.concurrent.Future
import scala.util.{Failure, Success}

object UploadController extends UploadController {
	override val processODSService: ProcessODSService = ProcessODSService
	override val cacheUtil: CacheUtil = CacheUtil
	override val csvFileProcessor: CsvFileProcessor = CsvFileProcessor
	override val authAction: AuthAction = AuthAction
}

trait UploadController extends ERSCheckingBaseController {

	val processODSService: ProcessODSService
	val cacheUtil: CacheUtil
	val csvFileProcessor: CsvFileProcessor
	val authAction: AuthAction

	def uploadCSVFile(scheme: String): Action[AnyContent] = authAction.async {
		implicit request =>
			showuploadCSVFile(scheme)
	}

	def showuploadCSVFile(scheme: String)(implicit request: RequestWithOptionalEmpRef[AnyContent], hc: HeaderCarrier, messages: Messages): Future[Result] = {
		val result = csvFileProcessor.processCsvUpload(scheme)(request, hc, messages)
		result.flatMap[Result] {
			case Success(true) => Future.successful(Redirect(routes.CheckingServiceController.checkingSuccessPage()))
			case Success(false) => Future.successful(Redirect(routes.HtmlReportController.htmlErrorReportPage()))
			case Failure(t) => handleException(t)
		}
	}

	def uploadODSFile(scheme: String): Action[AnyContent] = authAction.async {
		implicit request =>
			showuploadODSFile(scheme)
	}

	def showuploadODSFile(scheme: String)(implicit request: RequestWithOptionalEmpRef[AnyContent], hc: HeaderCarrier, messages: Messages): Future[Result] = {
		val result = processODSService.performODSUpload()(request, scheme, hc, messages)
		result.flatMap[Result] {
			case Success(true) => Future.successful(Redirect(routes.CheckingServiceController.checkingSuccessPage()))
			case Success(false) => Future.successful(Redirect(routes.HtmlReportController.htmlErrorReportPage()))
			case Failure(t) => handleException(t)
		}
	}

	def handleException(t: Throwable)(implicit request: Request[AnyContent], hc: HeaderCarrier): Future[Result] = {
		t match {
			case e: ERSFileProcessingException => {
				for {
					_ <- cacheUtil.cache[String](CacheUtil.FORMAT_ERROR_CACHE, e.message)
					_ <- cacheUtil.cache[Seq[String]](CacheUtil.FORMAT_ERROR_CACHE_PARAMS, e.optionalParams)
					_ <- cacheUtil.cache[Boolean](CacheUtil.FORMAT_ERROR_EXTENDED_CACHE, e.needsExtendedInstructions)
				} yield {
					Redirect(routes.CheckingServiceController.formatErrorsPage())
				}
			}
			case _ => throw t
		}
	}

}

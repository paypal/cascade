package com.paypal.stingray.common
package deploymentapi.metadata

import java.util.Date
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import json.jsonscalaz._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/27/12
 * Time: 3:00 PM
 */

case class DeploymentHistory(appId: Long,
                             repoType: String,
                             env: String,
                             source: String,
                             status: String,
                             startTime: Date,
                             repoMetaId: Option[String],
                             commitSha: Option[String],
                             repo: Option[String],
                             org: Option[String],
                             description: Option[String],
                             userId: Option[Long],
                             errorMsg: Option[String],
                             endTime: Option[Date],
                             buildTag: Option[String] = None)

object DeploymentHistory {

  private val appIdKey = "app-id"
  private val repoTypeKey = "repo-type"
  private val envKey = "env"
  private val sourceKey = "source"
  private val statusKey = "status"
  private val startTimeKey = "start-time"
  private val repoMetadataIdKey = "repo-metadata-id"
  private val commitShaKey = "commit-sha"
  private val repoKey = "repo"
  private val orgKey = "org"
  private val descriptionKey = "description"
  private val userIdKey = "user-id"
  private val errorMsgKey = "error-msg"
  private val endTimeKey = "end-time"
  private val buildTagKey = "build-tag"

  implicit val deploymentHistoryJSON = new JSON[DeploymentHistory] {
    override def read(json: JValue): Result[DeploymentHistory] = {
      for {
        appId <- field[Long](appIdKey)(json)
        repoType <- field[String](repoTypeKey)(json)
        env <- field[String](envKey)(json)
        source <- field[String](sourceKey)(json)
        status <- field[String](statusKey)(json)
        startTime <- field[Date](startTimeKey)(json)
        commitSha <- field[Option[String]](commitShaKey)(json)
        description <- field[Option[String]](descriptionKey)(json)
        repoMetadataId <- field[Option[String]](repoMetadataIdKey)(json)
        repo <- field[Option[String]](repoKey)(json)
        org <- field[Option[String]](orgKey)(json)
        userId <- field[Option[Long]](userIdKey)(json)
        errorMsg <- field[Option[String]](errorMsgKey)(json)
        endTime <- field[Option[Date]](endTimeKey)(json)
        buildTag <- field[Option[String]](buildTagKey)(json)
      } yield {
        DeploymentHistory(appId = appId,
          repoType = repoType,
          env = env,
          source = source,
          status = status,
          startTime = startTime,
          commitSha = commitSha,
          description = description,
          repoMetaId = repoMetadataId,
          org = org,
          repo = repo,
          userId = userId,
          errorMsg = errorMsg,
          endTime = endTime,
          buildTag = buildTag)
      }
    }
    override def write(history: DeploymentHistory): JValue = {
      (appIdKey -> history.appId) ~
      (repoTypeKey -> history.repoType) ~
      (envKey -> history.env) ~
      (sourceKey -> history.source) ~
      (statusKey -> history.status) ~
      (startTimeKey -> toJSON(history.startTime)) ~
      (commitShaKey -> history.commitSha) ~
      (descriptionKey -> history.description) ~
      (repoMetadataIdKey -> history.repoMetaId) ~
      (repoKey -> history.repo) ~
      (orgKey -> history.org) ~
      (userIdKey -> history.userId) ~
      (errorMsgKey -> history.errorMsg) ~
      (endTimeKey -> toJSON(history.endTime)) ~
      (buildTagKey -> history.buildTag)
    }
  }

}

case class DeploymentHistoryResponse(history: List[DeploymentHistory])

object DeploymentHistoryResponse {

  private val historyKey = "history"

  implicit val deploymentHistoryResponseJSON = new JSON[DeploymentHistoryResponse] {
    override def read(json: JValue): Result[DeploymentHistoryResponse] = {
      field[List[DeploymentHistory]](historyKey)(json).map { list =>
        DeploymentHistoryResponse(list)
      }
    }
    override def write(r: DeploymentHistoryResponse): JValue = (historyKey -> toJSON(r.history))
  }

}

package com.paypal.stingray.common.deploymentapi.metadata

import scalaz.Scalaz._
import java.util.Date
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.paypal.stingray.common.json
import json.jsonscalaz._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 1/12/12
 * Time: 10:29 PM
 */

case class RepositoryMetadataRequest(id: Option[String],
                                     appId: Long,
                                     repoType: String,
                                     repo: String,
                                     oauthToken: String,
                                     org: Option[String])

case class RepositoryMetadataResponse(id: String,
                                      appId: Long,
                                      repoType: String,
                                      repo: String,
                                      org: Option[String],
                                      lastCommitDev: Option[String],
                                      lastCommitProd: Option[String],
                                      created: Date,
                                      modified: Date)
object RepositoryMetadataResponse {

  private val idKey = "id"
  private val appIdKey = "appId"
  private val repoTypeKey = "repoType"
  private val repoKey = "repo"
  private val orgKey = "org"
  private val lastCommitDevKey = "lastCommitDev"
  private val lastCommitProdKey = "lastCommitProd"
  private val createdKey = "created"
  private val modifiedKey = "modified"

  implicit val repositoryMetadataResponseJSON = new JSON[RepositoryMetadataResponse] {
    override def read(json: JValue): Result[RepositoryMetadataResponse] = {
      (field[String](idKey)(json) |@|
       field[Long](appIdKey)(json) |@|
       field[String](repoTypeKey)(json) |@|
       field[String](repoKey)(json) |@|
       field[Option[String]](orgKey)(json) |@|
       field[Option[String]](lastCommitDevKey)(json) |@|
       field[Option[String]](lastCommitProdKey)(json) |@|
       field[Date](createdKey)(json) |@|
       field[Date](modifiedKey)(json)) { (id, appId, repoType, repo, org, lastCommitDev, lastCommitProd, created, modified) =>
        RepositoryMetadataResponse(
          id = id,
          appId = appId,
          repoType = repoType,
          repo = repo,
          org = org,
          lastCommitDev = lastCommitDev,
          lastCommitProd = lastCommitProd,
          created = created,
          modified = modified)
      }
    }
    override def write(r: RepositoryMetadataResponse): JValue = {
      (idKey -> r.id) ~
      (appIdKey -> r.appId) ~
      (repoTypeKey -> r.repoType) ~
      (repoKey -> r.repo) ~
      (orgKey -> r.org) ~
      (lastCommitDevKey -> r.lastCommitDev) ~
      (lastCommitProdKey -> r.lastCommitProd) ~
      (createdKey -> toJSON(r.created)) ~
      (modifiedKey -> toJSON(r.modified))
    }
  }

}

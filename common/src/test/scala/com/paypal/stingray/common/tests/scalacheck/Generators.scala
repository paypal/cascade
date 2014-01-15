package com.paypal.stingray.common.tests.scalacheck

import com.paypal.stingray.common.primitives._
import com.paypal.stingray.common.enumeration._
import com.paypal.stingray.common.env.{StingrayEnvironmentType, EnvironmentType}
import com.paypal.stingray.common.option._
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import java.util.UUID
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 6/26/12
 * Time: 5:25 PM
 */

trait Generators {

  implicit lazy val arbSymbol: Arbitrary[Symbol] = Arbitrary(arbitrary[String].map(Symbol(_)))

  implicit lazy val arbUUID: Arbitrary[UUID] = Arbitrary(Gen(_ => UUID.randomUUID.some))

  lazy val genInvalidUUID: Gen[String] = arbitrary[String].suchThat(s => Try(UUID.fromString(s)).isFailure)

  lazy val genClientName: Gen[ClientName] = (Gen.listOfN(ClientName.maxLength - 1, Gen.frequency((9, genAlphaLowerNumChar), (1, "-")))
    suchThat { s => ClientName.fromString(s.mkString).isDefined }
  ).flatMap(c => genAlphaLowerNumChar.map(first => ClientName(first + c.mkString)))

  lazy val genInvalidClientName: Gen[String] = arbitrary[String].suchThat(s => ClientName.fromString(s).isEmpty)

  lazy val genAppName: Gen[AppName] = (Gen.listOfN(AppName.maxLength - 1, Gen.frequency((9, genAlphaLowerNumChar), (1, Gen.oneOf("-", "_"))))
    suchThat { s => AppName.fromString(s.mkString).isDefined }
  ).flatMap(a => genAlphaLowerNumChar.map(first => AppName(first + a.mkString)))

  lazy val genSchemaName: Gen[SchemaName] = (Gen.listOfN(SchemaName.maxLength - 1, Gen.frequency((9, genAlphaLowerNumChar), (1, Gen.oneOf("-", "_"))))
    suchThat { s => SchemaName.fromString(s.mkString).isDefined }
  ).flatMap(a => genAlphaLowerNumChar.map(first => SchemaName(first + a.mkString)))

  lazy val genInvalidSchemaName: Gen[String] = arbitrary[String].suchThat(s => SchemaName.fromString(s).isEmpty)

  lazy val genSchemaSafeName: Gen[SchemaSafeName] = (Gen.listOfN(SchemaSafeName.maxLength - 1, Gen.frequency((9, genAlphaLowerNumChar), (1, Gen.oneOf("-", "_"))))
    suchThat { s => SchemaSafeName.fromString(s.mkString).isDefined }
  ).flatMap(a => genAlphaLowerNumChar.map(first => SchemaSafeName(first + a.mkString)))

  lazy val genFieldName: Gen[FieldName] = (Gen.listOfN(FieldName.maxLength - 1, Gen.frequency((9, genAlphaLowerNumChar), (1, Gen.oneOf("-", "_"))))
    suchThat { s => FieldName.fromString(s.mkString).isDefined }
  ).flatMap(a => genAlphaLowerNumChar.map(first => FieldName(first + a.mkString)))

  lazy val genInvalidAppName: Gen[String] = arbitrary[String].suchThat(s => AppName.fromString(s).isEmpty)

  lazy val genServeRootPrefix: Gen[ServeRootPrefix] = (Gen.listOfN(ServeRootPrefix.maxLength, Gen.frequency((9, genAlphaLowerNumChar), (1, Gen.oneOf("-", "_", "/"))))
    suchThat { s => ServeRootPrefix.fromString(s.mkString).isDefined }
  ).map(s => ServeRootPrefix(s.mkString))

  lazy val genPublicKey: Gen[PublicKey] = arbitrary[UUID].map(PublicKey(_))

  lazy val genInvalidPublicKey: Gen[String] = arbitrary[String].suchThat(s => PublicKey.fromString(s).isEmpty)

  lazy val genPrivateKey: Gen[PrivateKey] = arbitrary[UUID].map(PrivateKey(_))

  lazy val genInvalidPrivateKey: Gen[String] = arbitrary[String].suchThat(s => PrivateKey.fromString(s).isEmpty)

  lazy val genClientId: Gen[ClientId] = Gen.posNum[Long].map(ClientId(_))

  lazy val genUserId: Gen[UserId] = Gen.posNum[Long].map(UserId(_))

  lazy val genInvalidUserId: Gen[Long] = Gen.negNum[Long]

  lazy val genModuleId: Gen[ModuleId] =  Gen.posNum[Long].map(ModuleId(_))

  lazy val genAppId: Gen[AppId] = Gen.posNum[Long].map(AppId(_))

  lazy val genAPIVersionNumber: Gen[APIVersionNumber] = Gen.posNum[Long].map(APIVersionNumber(_))

  lazy val genJarBuild: Gen[JarBuild] = genStringWithSizeInRange(1, 10, genAlphaLowerNumChar).map(JarBuild(_))

  lazy val genRevisionId: Gen[RevisionId] = Gen.posNum[Long].map(RevisionId(_))

  lazy val genJarId: Gen[JarId] = Gen.posNum[Long].map(JarId(_))

  lazy val genCustomCodeMethodName: Gen[String] = genStringWithSizeInRange(1, 10, genAlphaLowerNumChar)

  lazy val genInvalidAppId: Gen[Long] = Gen.negNum[Long]

  lazy val genInvalidClientId: Gen[Long] = Gen.negNum[Long]

  lazy val genPackageId: Gen[PackageId] = Gen.posNum[Long].map(PackageId(_))

  lazy val genTwitterKey: Gen[TwitterKey] = genNonEmptyAlphaStr.map(TwitterKey(_))

  lazy val genTwitterSecret: Gen[TwitterSecret] = genNonEmptyAlphaStr.map(TwitterSecret(_))

  lazy val genPushCertPassword: Gen[PushCertPassword] = genNonEmptyAlphaStr.map(PushCertPassword(_))

  lazy val genClientLoginToken: Gen[ClientLoginToken] = genNonEmptyAlphaStr.map(ClientLoginToken(_))

  lazy val genAndroidGCMKey: Gen[AndroidGCMKey] = genNonEmptyAlphaStr.map(AndroidGCMKey(_))

  lazy val genSandboxDatabaseName: Gen[SandboxDatabaseName] = genNonEmptyAlphaStr.map(SandboxDatabaseName(_))

  lazy val genInvalidDatabaseName: Gen[String] = for {
    invalidChar <- Gen.oneOf('/', '\\', '.', '"', ' ', '*', '<', '>', ':', '|', '?')
    prefix <- arbitrary[String]
    suffix <- arbitrary[String]
  } yield prefix + invalidChar + suffix

  lazy val genProdDatabaseName: Gen[ProdDatabaseName] = genNonEmptyAlphaStr.map(ProdDatabaseName(_))

  lazy val genClusterName: Gen[ClusterName] = genNonEmptyAlphaStr.map(ClusterName(_))

  lazy val genClusterHost: Gen[ClusterHost] = genNonEmptyAlphaStr.map(ClusterHost(_))

  lazy val genS3Key: Gen[S3Key] = genNonEmptyAlphaStr.map(S3Key(_))

  lazy val genS3Secret: Gen[S3Secret] = genNonEmptyAlphaStr.map(S3Secret(_))

  lazy val genS3Bucket: Gen[S3Bucket] = genNonEmptyAlphaStr.map(S3Bucket(_))

  lazy val genS3Path: Gen[S3Path] = genNonEmptyAlphaStr.map(S3Path(_))

  lazy val genGitHubToken: Gen[GitHubToken] = genNonEmptyAlphaStr.map(GitHubToken(_))

  lazy val genSchemaFieldId: Gen[SchemaFieldId] = Gen.posNum[Long].map(SchemaFieldId(_))

  lazy val genSchemaPermissionId: Gen[SchemaPermissionId] = Gen.posNum[Long].map(SchemaPermissionId(_))

  lazy val genSchemaId: Gen[SchemaId] = Gen.posNum[Long].map(SchemaId(_))

  lazy val genRelationId: Gen[RelationId] = Gen.posNum[Long].map(RelationId(_))

  lazy val genSnapshotId: Gen[SnapshotId] = Gen.posNum[Long].map(SnapshotId(_))

  lazy val genSchemaAction: Gen[SchemaAction] = Gen.oneOf(SchemaAction.GET, SchemaAction.PUT, SchemaAction.POST, SchemaAction.DELETE)

  lazy val genFieldType: Gen[FieldType] = Gen.oneOf(
    FieldType.SubObjectType,
    FieldType.StringType,
    FieldType.NumberType,
    FieldType.IntType,
    FieldType.DoubleType,
    FieldType.BoolType,
    FieldType.ArrayType,
    FieldType.DateTimeType,
    FieldType.UTCTimeType,
    FieldType.UserNameType,
    FieldType.PasswordType,
    FieldType.ForgotPasswordEmailType,
    FieldType.CreatorNameRefType,
    FieldType.RelationType,
    FieldType.GeopointType,
    FieldType.BinaryType,
    FieldType.PrimaryKeyType,
    FieldType.NoneType)

  lazy val genRelationType: Gen[RelationType] = Gen.oneOf(
    RelationType.One2One,
    RelationType.One2Many,
    RelationType.Many2One,
    RelationType.Many2Many)

  lazy val genEmail: Gen[Email] = {
    for {
      prefix <- genNonEmptyAlphaStr
      suffix <- genNonEmptyAlphaStr
    } yield Email(prefix + "@" + suffix)
  }

  lazy val genEnvType: Gen[EnvironmentType] = Gen.oneOf(EnvironmentType.DEV, EnvironmentType.PROD)

  lazy val genInvalidEnvType: Gen[String] = arbitrary[String].suchThat(_.readEnum[EnvironmentType].isEmpty)

  lazy val genStackMobEnvironmentType: Gen[StingrayEnvironmentType] = {
    Gen.oneOf(StingrayEnvironmentType.DEVELOPMENT, StingrayEnvironmentType.STAGING, StingrayEnvironmentType.PRODUCTION)
  }

  lazy val genAlphaLowerNumChar: Gen[Char] = Gen.frequency((9, Gen.alphaLowerChar), (1, Gen.numChar))

  lazy val genNonEmptyAlphaStr: Gen[String] = Gen.listOf1(Gen.alphaChar).map(_.mkString)

  def genListWithSizeInRange[T](min: Int, max: Int, gen: Gen[T]): Gen[List[T]] = {
    for {
      n <- Gen.choose(min, max)
      lst <- Gen.listOfN(n, gen)
    } yield lst
  }

  def genStringWithSizeInRange(min: Int, max: Int, gen: Gen[Char]): Gen[String] = {
    genListWithSizeInRange(min, max, gen).map(_.mkString)
  }

  def genError[T <: Throwable](implicit m: Manifest[T]): Gen[T] = {
    Gen.alphaStr.map(s => m.runtimeClass.getConstructor(classOf[String], classOf[Throwable]).newInstance(s, null).asInstanceOf[T])
  }

  def genErrors[T <: Throwable : Manifest]: Gen[List[T]] = Gen.listOf1(genError)

  def genOption[T](gen: Gen[T]): Gen[Option[T]] = gen.flatMap(g => Gen.oneOf(g.some, none[T]))

}


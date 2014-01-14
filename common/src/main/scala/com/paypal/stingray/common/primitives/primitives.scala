package com.paypal.stingray.common

import com.paypal.stingray.common.option._
import com.paypal.stingray.common.env.EnvironmentType
import com.paypal.stingray.common.json._
import java.util.UUID
import scala.util.matching.Regex
import net.liftweb.json.JsonAST._
import org.apache.commons.validator.routines.EmailValidator
import language.implicitConversions
import scala.util.{Success, Failure, Try}

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 6/6/12
 * Time: 3:25 PM
 */

package object primitives {

  class PrimitiveException(msg: String) extends Exception(msg)

  trait LongValue extends Any with Serializable {
    def value: Long
    override def toString: String = value.toString
  }

  trait StringValue extends Any with Serializable {
    def value: String
    override def toString: String = value.toString
  }

  implicit def valueToLong(v: LongValue): Long = v.value
  implicit def valueToString(v: StringValue): String = v.value

  class AppId(override val value: Long) extends AnyVal with LongValue
  class ClientId(override val value: Long) extends AnyVal with LongValue
  class UserId(override val value: Long) extends AnyVal with LongValue
  class APIVersionNumber(override val value: Long) extends AnyVal with LongValue {
    def toEnvType: EnvironmentType = {
      if(0L == value) EnvironmentType.DEV else EnvironmentType.PROD
    }
  }
  class ModuleId(override val value: Long) extends AnyVal with LongValue
  class PackageId(override val value: Long) extends AnyVal with LongValue

  class JarId(override val value: Long) extends AnyVal with LongValue
  class JarBuild(override val value: String) extends AnyVal with StringValue
  class JarMethodId(override val value: Long) extends AnyVal with LongValue
  class JarMethodParameterId(override val value: Long) extends AnyVal with LongValue
  class RevisionId(override val value: Long) extends AnyVal with LongValue

  class AppName(override val value: String) extends AnyVal with StringValue
  class MethodName(override val value: String) extends AnyVal with StringValue
  class ClientName(override val value: String) extends AnyVal with StringValue
  class ServeRootPrefix(override val value: String) extends AnyVal with StringValue
  class PublicKey(override val value: String) extends AnyVal with StringValue
  class PrivateKey(override val value: String) extends AnyVal with StringValue
  class Email(override val value: String) extends AnyVal with StringValue
  class TwitterKey(override val value: String) extends AnyVal with StringValue
  class TwitterSecret(override val value: String) extends AnyVal with StringValue
  class PushCertPassword(override val value: String) extends AnyVal with StringValue
  class ClientLoginToken(override val value: String) extends AnyVal with StringValue
  class AndroidGCMKey(override val value: String) extends AnyVal with StringValue
  class SandboxDatabaseName(override val value: String) extends AnyVal with StringValue
  class ProdDatabaseName(override val value: String) extends AnyVal with StringValue
  class ClusterName(override val value: String) extends AnyVal with StringValue
  class ClusterHost(override val value: String) extends AnyVal with StringValue
  class S3Key(override val value: String) extends AnyVal with StringValue
  class S3Secret(override val value: String) extends AnyVal with StringValue
  class S3Bucket(override val value: String) extends AnyVal with StringValue
  class S3Path(override val value: String) extends AnyVal with StringValue
  class GitHubToken(override val value: String) extends AnyVal with StringValue
  class SchemaName(override val value: String) extends AnyVal with StringValue
  class FieldName(override val value: String) extends AnyVal with StringValue
  class FieldSafeName(override val value: String) extends AnyVal with StringValue
  class NodeAddress(override val value: String) extends AnyVal with StringValue
  class SchemaSafeName(override val value: String) extends AnyVal with StringValue
  class SchemaFieldId(override val value: Long) extends AnyVal with LongValue
  class SchemaPermissionId(override val value: Long) extends AnyVal with LongValue
  class SchemaId(override val value: Long) extends AnyVal with LongValue
  class RelationId(override val value: Long) extends AnyVal with LongValue
  class SnapshotId(override val value: Long) extends AnyVal with LongValue

  implicit def fieldNameToSafe(fieldName: FieldName): FieldSafeName = FieldSafeName(fieldName.value.toLowerCase)
  implicit def schemaNameToSafe(schemaName: SchemaName): SchemaSafeName = SchemaSafeName(schemaName.value.toLowerCase)

  sealed trait TypedLong[A <: AnyVal] {
    protected def newInstance(arg: Long): A
    def fromString(s: String): Option[A] = Try { apply(s) }.toOption
    def fromLong(l: Long): Option[A] = Try { apply(l) }.toOption
    def apply(s: String): A = Try { apply(s.toLong) }.getOrElse {
      throw new PrimitiveException("Invalid %s: %s".format(getClass.getSimpleName, s))
    }
    def apply(l: Long): A = {
      if (l >= 0) newInstance(l) else throw new PrimitiveException("Invalid %s: %s".format(getClass.getSimpleName, l))
    }
    def unapply(s: String): Option[A] = fromString(s)
    def unapply(l: Long): Option[A] = fromLong(l)
    def unapply(i: Int): Option[A] = fromLong(i)
  }

  sealed trait TypedString[A <: AnyVal] {
    protected def newInstance(arg: String): A
    def isValid(s: String): Boolean
    def fromString(s: String): Option[A] = {
      if (isValid(s)) Some(newInstance(s)) else None
    }
    def apply(s: String): A = {
      if (isValid(s)) newInstance(s) else throw new PrimitiveException("Invalid %s: %s".format(getClass.getSimpleName, s))
    }
    def unapply(s: String): Option[A] = fromString(s)
  }

  sealed trait TypedRegexString[A <: AnyVal] extends TypedString[A] {
    protected def minLength: Int
    protected def maxLength: Int
    protected def regex: Regex
    override def isValid(s: String): Boolean = {
      regex.findFirstIn(s).exists(c => c.length >= minLength && c.length <= maxLength)
    }
  }

  sealed trait TypedDatabaseNameString[A <: AnyVal] extends TypedString[A] {
    // http://docs.mongodb.org/manual/reference/limits/#Restrictions%20on%20Database%20Names
    val minLength = 1
    protected def regex: Regex = """[/\\. "*<>:|?]""".r
    override def isValid(s: String): Boolean = {
      regex.findFirstIn(s).isEmpty && s.length >= minLength
    }
  }

  sealed trait TypedUniqueString[A <: AnyVal] {
    protected def newInstance(arg: String): A
    def fromString(s: String): Option[A] = Try { UUID.fromString(s) }.map(apply(_)).toOption
    def apply(s: String): A = fromString(s).orThrow(new PrimitiveException("Invalid %s: %s".format(getClass.getSimpleName, s)))
    def apply(u: UUID): A = newInstance(u.toString)
    def unapply(s: String): Option[A] = fromString(s)
  }

  sealed trait TypedNonEmptyString[A <: AnyVal] extends TypedString[A] {
    override def isValid(s: String): Boolean = Option(s).exists(_.size > 0)
  }

  object AppId extends TypedLong[AppId] {
    override def newInstance(arg: Long): AppId = new AppId(arg)
  }

  object ClientId extends TypedLong[ClientId] {
    override def newInstance(arg: Long): ClientId = new ClientId(arg)
  }

  object UserId extends TypedLong[UserId] {
    override def newInstance(arg: Long): UserId = new UserId(arg)
  }

  object APIVersionNumber extends TypedLong[APIVersionNumber] {
    override def newInstance(arg: Long): APIVersionNumber = new APIVersionNumber(arg)
  }

  object ModuleId extends TypedLong[ModuleId] {
    override def newInstance(arg: Long): ModuleId = new ModuleId(arg)
  }

  object PackageId extends TypedLong[PackageId] {
    override def newInstance(arg: Long): PackageId = new PackageId(arg)
  }

  object JarId extends TypedLong[JarId] {
    override def newInstance(arg: Long): JarId = new JarId(arg)
  }

  object JarBuild extends TypedRegexString[JarBuild] {
    override def newInstance(arg: String): JarBuild = new JarBuild(arg)
    override val minLength = 1
    override val maxLength = 32
    override val regex = """^([a-zA-Z0-9]|[a-zA-Z0-9][-a-zA-Z0-9]*[a-zA-Z0-9])$""".r
  }

  object JarMethodId extends TypedLong[JarMethodId] {
    override def newInstance(arg: Long): JarMethodId = new JarMethodId(arg)
  }

  object JarMethodParameterId extends TypedLong[JarMethodParameterId] {
    override def newInstance(arg: Long): JarMethodParameterId = new JarMethodParameterId(arg)
  }

  object RevisionId extends TypedLong[RevisionId] {
    override def newInstance(arg: Long): RevisionId = new RevisionId(arg)
  }

  object AppName extends TypedRegexString[AppName] {
    override def newInstance(arg: String): AppName = new AppName(arg)
    override val minLength = 3
    override val maxLength = 32
    override val regex = """^([a-z0-9]*|[a-z0-9][a-z0-9_-]*[a-z0-9])$""".r
  }

  object ClientName extends TypedRegexString[ClientName] {
    override def newInstance(arg: String): ClientName = new ClientName(arg)
    override val minLength = 3
    override val maxLength = 32
    override val regex = """^([a-z0-9]|[a-z0-9][-a-z0-9]*[a-z0-9])$""".r
  }

  object MethodName extends TypedRegexString[MethodName] {
    override def newInstance(arg: String): MethodName = new MethodName(arg)
    override val minLength = 1
    override val maxLength = 255
    override val regex = """^([a-z0-9]*|[a-z0-9][a-z0-9_-]*[a-z0-9])$""".r
  }

  object ServeRootPrefix extends TypedRegexString[ServeRootPrefix] {
    override def newInstance(arg: String): ServeRootPrefix = new ServeRootPrefix(arg)
    override val minLength = 1
    override val maxLength = 32
    override val regex = """^([a-z0-9_/-]{%s,%s})$""".format(minLength, maxLength).r
  }

  object Email extends TypedString[Email] {
    override def newInstance(arg: String): Email = new Email(arg)
    private val validator = EmailValidator.getInstance(true)
    override def isValid(s: String): Boolean = validator.isValid(s)
  }

  object SchemaName extends TypedRegexString[SchemaName] {
    override def newInstance(arg: String): SchemaName = new SchemaName(arg)
    override val minLength = 1
    override val maxLength = 32
    override val regex = """^([a-zA-Z0-9_\-]*)$""".r
  }

  object FieldName extends TypedRegexString[FieldName] {
    override def newInstance(arg: String): FieldName = new FieldName(arg)
    override val minLength = 1
    override val maxLength = 32
    override val regex = """^([a-zA-Z0-9_\-]*)$""".r
  }

  object NodeAddress extends TypedRegexString[NodeAddress] {
    override def newInstance(arg: String): NodeAddress = new NodeAddress(arg)
    override val minLength = 7
    override val maxLength = 15
    override val regex = """^[0-9][0-9]?[0-9]?\.[0-9][0-9]?[0-9]?\.[0-9][0-9]?[0-9]?\.[0-9][0-9]?[0-9]?$""".r
  }

  object TwitterKey extends TypedNonEmptyString[TwitterKey] {
    override def newInstance(arg: String): TwitterKey = new TwitterKey(arg)
  }

  object TwitterSecret extends TypedNonEmptyString[TwitterSecret] {
    override def newInstance(arg: String): TwitterSecret = new TwitterSecret(arg)
  }

  object PushCertPassword extends TypedNonEmptyString[PushCertPassword] {
    override def newInstance(arg: String): PushCertPassword = new PushCertPassword(arg)
  }

  object ClientLoginToken extends TypedNonEmptyString[ClientLoginToken] {
    override def newInstance(arg: String): ClientLoginToken = new ClientLoginToken(arg)
  }

  object AndroidGCMKey extends TypedNonEmptyString[AndroidGCMKey] {
    override def newInstance(arg: String): AndroidGCMKey = new AndroidGCMKey(arg)
  }

  object SandboxDatabaseName extends TypedDatabaseNameString[SandboxDatabaseName] {
    override def newInstance(arg: String): SandboxDatabaseName = new SandboxDatabaseName(arg)
  }

  object ProdDatabaseName extends TypedDatabaseNameString[ProdDatabaseName] {
    override def newInstance(arg: String): ProdDatabaseName = new ProdDatabaseName(arg)
  }

  object ClusterName extends TypedNonEmptyString[ClusterName] {
    override def newInstance(arg: String): ClusterName = new ClusterName(arg)
  }

  object ClusterHost extends TypedNonEmptyString[ClusterHost] {
    override def newInstance(arg: String): ClusterHost = new ClusterHost(arg)
  }

  object S3Key extends TypedNonEmptyString[S3Key] {
    override def newInstance(arg: String): S3Key = new S3Key(arg)
  }

  object S3Secret extends TypedNonEmptyString[S3Secret] {
    override def newInstance(arg: String): S3Secret = new S3Secret(arg)
  }

  object S3Bucket extends TypedNonEmptyString[S3Bucket] {
    override def newInstance(arg: String): S3Bucket = new S3Bucket(arg)
  }

  object S3Path extends TypedNonEmptyString[S3Path] {
    override def newInstance(arg: String): S3Path = new S3Path(arg)
  }

  object GitHubToken extends TypedNonEmptyString[GitHubToken] {
    override def newInstance(arg: String): GitHubToken = new GitHubToken(arg)
  }

  object PublicKey extends TypedUniqueString[PublicKey] {
    override def newInstance(arg: String): PublicKey = new PublicKey(arg)
  }

  object PrivateKey extends TypedUniqueString[PrivateKey] {
    override def newInstance(arg: String): PrivateKey = new PrivateKey(arg)
  }

  object SchemaSafeName extends TypedRegexString[SchemaSafeName] {
    override def newInstance(arg: String): SchemaSafeName = new SchemaSafeName(arg)
    override val minLength = 1
    override val maxLength = 32
    override val regex = """^([a-z0-9_\-]*)$""".r
  }

  object FieldSafeName extends TypedRegexString[FieldSafeName] {
    override def newInstance(arg: String): FieldSafeName = new FieldSafeName(arg)
    override val minLength = 1
    override val maxLength = 32
    override val regex = """^([a-z0-9_\-]*)$""".r
  }

  object SchemaFieldId extends TypedLong[SchemaFieldId] {
    override def newInstance(arg: Long): SchemaFieldId = new SchemaFieldId(arg)
  }

  object SchemaPermissionId extends TypedLong[SchemaPermissionId] {
    override def newInstance(arg: Long): SchemaPermissionId = new SchemaPermissionId(arg)
  }

  object SchemaId extends TypedLong[SchemaId] {
    override def newInstance(arg: Long): SchemaId = new SchemaId(arg)
  }

  object RelationId extends TypedLong[RelationId] {
    override def newInstance(arg: Long): RelationId = new RelationId(arg)
  }

  object SnapshotId extends TypedLong[SnapshotId] {
    override def newInstance(arg: Long): SnapshotId = new SnapshotId(arg)
  }

  sealed trait TaggedLongJSON[A <: AnyVal] extends JSON[A] {
    protected def fromLong(l: Long): Option[A]
    protected def targetClassName: String
    override def read(json: JValue): Try[A] = json match {
      case JInt(i) => Try {
        fromLong(i.toLong).orThrow(new PrimitiveException("Invalid %s: %s".format(targetClassName, i)))
      }.recover { case e =>
        throw UncategorizedError(i.toString(), e.getMessage, Nil)
      }
      case j => Failure(UnexpectedJSONError(j, classOf[JInt]))
    }
  }

  sealed trait TaggedStringJSON[A <: AnyVal] extends JSON[A] {
    protected def fromString(s: String): Option[A]
    protected def targetClassName: String
    override def read(json: JValue): Try[A] = json match {
      case JString(s) => Try {
        fromString(s).orThrow(UncategorizedError(s, "Invalid %s: %s".format(targetClassName, s), Nil))
      }
      case j => Failure(UnexpectedJSONError(j, classOf[JString]))
    }
  }

  implicit val appIdJSON = new TaggedLongJSON[AppId] {
    override def write(value: AppId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[AppId] = AppId.fromLong(l)
    override val targetClassName = "AppId"
  }

  implicit val jarIdJSON = new TaggedLongJSON[JarId] {
    override def write(value: JarId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[JarId] = JarId.fromLong(l)
    override val targetClassName = "JarId"
  }

  implicit val jarBuildJSON = new TaggedStringJSON[JarBuild] {
    override def write(value: JarBuild): JValue = JString(value.value)
    override def fromString(s: String): Option[JarBuild] = JarBuild.fromString(s)
    override val targetClassName = "JarBuild"
  }

  implicit val revisionIdJSON = new TaggedLongJSON[RevisionId] {
    override def write(value: RevisionId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[RevisionId] = RevisionId.fromLong(l)
    override val targetClassName = "RevisionId"
  }

  implicit val clientIdJSON = new TaggedLongJSON[ClientId] {
    override def write(value: ClientId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[ClientId] = ClientId.fromLong(l)
    override val targetClassName = "ClientId"
  }

  implicit val userIdJSON = new TaggedLongJSON[UserId] {
    override def write(value: UserId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[UserId] = UserId.fromLong(l)
    override val targetClassName = "UserId"
  }

  implicit val apiVersionNumberJSON = new TaggedLongJSON[APIVersionNumber] {
    override def write(value: APIVersionNumber): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[APIVersionNumber] = APIVersionNumber.fromLong(l)
    override val targetClassName = "APIVersionNumber"
  }

  implicit val moduleIdJSON = new TaggedLongJSON[ModuleId] {
    override def write(value: ModuleId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[ModuleId] = ModuleId.fromLong(l)
    override val targetClassName = "ModuleId"
  }

  implicit val packageIdJSON = new TaggedLongJSON[PackageId] {
    override def write(value: PackageId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[PackageId] = PackageId.fromLong(l)
    override val targetClassName = "PackageId"
  }

  implicit val publicKeyJSON = new TaggedStringJSON[PublicKey] {
    override def write(value: PublicKey): JValue = JString(value.value)
    override def fromString(s: String): Option[PublicKey] = PublicKey.fromString(s)
    override val targetClassName = "PublicKey"
  }

  implicit val privateKeyJSON = new TaggedStringJSON[PrivateKey] {
    override def write(value: PrivateKey): JValue = JString(value.value)
    override def fromString(s: String): Option[PrivateKey] = PrivateKey.fromString(s)
    override val targetClassName = "PrivateKey"
  }

  implicit val clientNameJSON = new TaggedStringJSON[ClientName] {
    override def write(value: ClientName): JValue = JString(value.value)
    override def fromString(s: String): Option[ClientName] = ClientName.fromString(s)
    override val targetClassName = "ClientName"
  }

  implicit val appNameJSON = new TaggedStringJSON[AppName] {
    override def write(value: AppName): JValue = JString(value.value)
    override def fromString(s: String): Option[AppName] = AppName.fromString(s)
    override val targetClassName = "AppName"
  }

  implicit val methodNameJSON = new TaggedStringJSON[MethodName] {
    override def write(value: MethodName): JValue = JString(value.value)
    override def fromString(s: String): Option[MethodName] = MethodName.fromString(s)
    override val targetClassName = "MethodName"
  }

  implicit val serveRootPrefixJSON = new TaggedStringJSON[ServeRootPrefix] {
    override def write(value: ServeRootPrefix): JValue = JString(value.value)
    override def fromString(s: String): Option[ServeRootPrefix] = ServeRootPrefix.fromString(s)
    override val targetClassName = "ServeRootPrefix"
  }

  implicit val emailJSON = new TaggedStringJSON[Email] {
    override def write(value: Email): JValue = JString(value.value)
    override def fromString(s: String): Option[Email] = Email.fromString(s)
    override val targetClassName = "Email"
  }

  implicit val schemaNameJSON = new TaggedStringJSON[SchemaName] {
    override def write(value: SchemaName): JValue = JString(value.value)
    override def fromString(s: String): Option[SchemaName] = SchemaName.fromString(s)
    override val targetClassName = "SchemaName"
  }

  implicit val schemaSafeNameJSON = new TaggedStringJSON[SchemaSafeName] {
    override def write(value: SchemaSafeName): JValue = JString(value.value)
    override def fromString(s: String): Option[SchemaSafeName] = SchemaSafeName.fromString(s)
    override val targetClassName = "SchemaSafeName"
  }

  implicit val fieldSafeNameJSON = new TaggedStringJSON[FieldSafeName] {
    override def write(value: FieldSafeName): JValue = JString(value.value)
    override def fromString(s: String): Option[FieldSafeName] = FieldSafeName.fromString(s)
    override val targetClassName = "FieldSafeName"
  }

  implicit val schemaFieldIdJSON = new TaggedLongJSON[SchemaFieldId] {
    override def write(value: SchemaFieldId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[SchemaFieldId] = SchemaFieldId.fromLong(l)
    override val targetClassName = "SchemaFieldId"
  }

  implicit val schemaPermissionIdJSON = new TaggedLongJSON[SchemaPermissionId] {
    override def write(value: SchemaPermissionId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[SchemaPermissionId] = SchemaPermissionId.fromLong(l)
    override val targetClassName = "SchemaPermissionId"
  }

  implicit val fieldNameJSON = new TaggedStringJSON[FieldName] {
    override def write(value: FieldName): JValue = JString(value.value)
    override def fromString(s: String): Option[FieldName] = FieldName.fromString(s)
    override val targetClassName = "FieldName"
  }

  implicit val twitterKeyJSON = new TaggedStringJSON[TwitterKey] {
    override def write(value: TwitterKey): JValue = JString(value.value)
    override def fromString(s: String): Option[TwitterKey] = TwitterKey.fromString(s)
    override val targetClassName = "TwitterKey"
  }

  implicit val twitterSecretJSON = new TaggedStringJSON[TwitterSecret] {
    override def write(value: TwitterSecret): JValue = JString(value.value)
    override def fromString(s: String): Option[TwitterSecret] = TwitterSecret.fromString(s)
    override val targetClassName = "TwitterSecret"
  }

  implicit val pushCertPasswordJSON = new TaggedStringJSON[PushCertPassword] {
    override def write(value: PushCertPassword): JValue = JString(value.value)
    override def fromString(s: String): Option[PushCertPassword] = PushCertPassword.fromString(s)
    override val targetClassName = "PushCertPassword"
  }

  implicit val clientLoginTokenJSON = new TaggedStringJSON[ClientLoginToken] {
    override def write(value: ClientLoginToken): JValue = JString(value.value)
    override def fromString(s: String): Option[ClientLoginToken] = ClientLoginToken.fromString(s)
    override val targetClassName = "ClientLoginToken"
  }

  implicit val androidGCMKeyJSON = new TaggedStringJSON[AndroidGCMKey] {
    override def write(value: AndroidGCMKey): JValue = JString(value.value)
    override def fromString(s: String): Option[AndroidGCMKey] = AndroidGCMKey.fromString(s)
    override val targetClassName = "AndroidGCMKey"
  }

  implicit val sandboxDatabaseNameSON = new TaggedStringJSON[SandboxDatabaseName] {
    override def write(value: SandboxDatabaseName): JValue = JString(value.value)
    override def fromString(s: String): Option[SandboxDatabaseName] = SandboxDatabaseName.fromString(s)
    override val targetClassName = "SandboxDatabaseName"
  }

  implicit val prodDatabaseNameJSON = new TaggedStringJSON[ProdDatabaseName] {
    override def write(value: ProdDatabaseName): JValue = JString(value.value)
    override def fromString(s: String): Option[ProdDatabaseName] = ProdDatabaseName.fromString(s)
    override val targetClassName = "ProdDatabaseName"
  }

  implicit val clusterNameJSON = new TaggedStringJSON[ClusterName] {
    override def write(value: ClusterName): JValue = JString(value.value)
    override def fromString(s: String): Option[ClusterName] = ClusterName.fromString(s)
    override val targetClassName = "ClusterName"
  }

  implicit val clusterHostJSON = new TaggedStringJSON[ClusterHost] {
    override def write(value: ClusterHost): JValue = JString(value.value)
    override def fromString(s: String): Option[ClusterHost] = ClusterHost.fromString(s)
    override val targetClassName = "ClusterHost"
  }

  implicit val s3KeyJSON = new TaggedStringJSON[S3Key] {
    override def write(value: S3Key): JValue = JString(value.value)
    override def fromString(s: String): Option[S3Key] = S3Key.fromString(s)
    override val targetClassName = "S3Key"
  }

  implicit val s3SecretJSON = new TaggedStringJSON[S3Secret] {
    override def write(value: S3Secret): JValue = JString(value.value)
    override def fromString(s: String): Option[S3Secret] = S3Secret.fromString(s)
    override val targetClassName = "S3Secret"
  }

  implicit val s3BucketJSON = new TaggedStringJSON[S3Bucket] {
    override def write(value: S3Bucket): JValue = JString(value.value)
    override def fromString(s: String): Option[S3Bucket] = S3Bucket.fromString(s)
    override val targetClassName = "S3Bucket"
  }

  implicit val s3PathJSON = new TaggedStringJSON[S3Path] {
    override def write(value: S3Path): JValue = JString(value.value)
    override def fromString(s: String): Option[S3Path] = S3Path.fromString(s)
    override val targetClassName = "S3Path"
  }

  implicit val gitHubTokenJSON = new TaggedStringJSON[GitHubToken] {
    override def write(value: GitHubToken): JValue = JString(value.value)
    override def fromString(s: String): Option[GitHubToken] = GitHubToken.fromString(s)
    override val targetClassName = "GitHubToken"
  }

  implicit val NodeAddressJSON = new TaggedStringJSON[NodeAddress] {
    override def write(value: NodeAddress): JValue = JString(value.value)
    override def fromString(s: String): Option[NodeAddress] = NodeAddress.fromString(s)
    override val targetClassName = "NodeAddress"
  }

  implicit val schemaIdJSON = new TaggedLongJSON[SchemaId] {
    override def write(value: SchemaId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[SchemaId] = SchemaId.fromLong(l)
    override val targetClassName = "SchemaId"
  }

  implicit val snapshotIdJSON = new TaggedLongJSON[SnapshotId] {
    override def write(value: SnapshotId): JValue = JInt(BigInt(value.value))
    override def fromLong(l: Long): Option[SnapshotId] = SnapshotId.fromLong(l)
    override val targetClassName = "SnapshotId"
  }

}

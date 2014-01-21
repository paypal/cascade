package com.paypal.stingray.common.enumeration

/**
 * By mixing this into your enum you are able to pattern match on string values
 * that you want to convert to enum values (iif they are valid). For example,
 * see com.paypal.stingray.common.deploymentapi.metadata.RepositoryType has this trait mixed in
 * and can be used as such:
 *
 * Note: the type returned by the extractor is the general sealed trait T, not the
 * enum instances themselves
 *
 * scala> "HTML5" match { case RepositoryType(a) => a; case _ => throw new Exception("fail!") }
 * res0: com.paypal.stingray.common.deploymentapi.metadata.RepositoryType = HTML5
 * scala> "CC" match { case RepositoryType(a) => a; case _ => throw new Exception("fail!") }
 * res1: com.paypal.stingray.common.deploymentapi.metadata.RepositoryType = CC
 * scala> "a" match { case RepositoryType(a) => a; case _ => throw new Exception("fail!") }
 * java.lang.Exception: fail!
 */
trait EnumUnapply[T <: Enumeration] {
  def unapply(s: String)(implicit reader: EnumReader[T]): Option[T] = reader.read(s)
}

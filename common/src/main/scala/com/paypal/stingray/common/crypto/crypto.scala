package com.paypal.stingray.common

import scalaz._
import javax.crypto.spec.{IvParameterSpec, PBEKeySpec, SecretKeySpec}
import javax.crypto._
import org.apache.commons.codec.binary.{StringUtils, Base64}
import java.lang.String
import java.security.spec.KeySpec
import com.paypal.stingray.common.validation._
import com.paypal.stingray.common.primitives._

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 10/2/12
 * Time: 6:16 PM
 */

package object crypto {

  // This is the 16 byte secret key used to encrypt and decrypt.
  private val key = new SecretKeySpec("adfjif733dfdf738".getBytes, "AES")

  def encrypt(l: Long): String = encrypt(l.toString)

  def encrypt(s: String): String = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val cipherText = cipher.doFinal(StringUtils.getBytesUtf8(s))
    Base64.encodeBase64URLSafeString(cipherText)
  }

  def decrypt(encrypted: String): String = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE")
    cipher.init(Cipher.DECRYPT_MODE, key)
    val encypted = Base64.decodeBase64(encrypted)
    val decrypted = cipher.doFinal(encypted)
    StringUtils.newStringUtf8(decrypted)
  }


  /**
   * Old and overly complex encryption used for push certs. Don't use in new code
   */
  def encryptLegacy(plainText: String, appID: AppId): Validation[Throwable, String] = {
    for {
      cipher <- getCipher(appID, Cipher.ENCRYPT_MODE)
      encrypted <- validating {
        val encrypted = cipher.doFinal(plainText.getBytes("UTF-8"))
        Base64.encodeBase64URLSafeString(encrypted)
      }
    } yield {
      encrypted
    }
  }

  /**
   * Old and overly complex encryption used for push certs. Don't use in new code
   */
  def decryptLegacy(encrypted: String, appID: AppId): Validation[Throwable, String] = {
    for {
      cipher <- getCipher(appID, Cipher.DECRYPT_MODE)
      decrypted <- validating {
        new String(cipher.doFinal(Base64.decodeBase64(encrypted)), "UTF-8")
      }
    } yield {
      decrypted
    }
  }

  private val masterSecret: String = "applicationsPushCertPwsacFVR4.rKUi6{Fx@JJY'<t:LISy4_F;FY^m]vN]ewfJ5OC,O/u"
  private val salt: String = "fw5_3(Q8"
  private final val iv: Array[Byte] = Array[Byte](0x5e.toByte, 0xca.toByte, 0xbb.toByte,
                                                  0x8c.toByte, 0x85.toByte, 0x26, 0x54, 0x7f,
                                                  0xfa.toByte, 0xf2.toByte, 0xc2.toByte, 0x8b.toByte,
                                                  0xed.toByte, 0xe4.toByte, 0x14, 0x84.toByte)

  private def getCipher(appID: AppId, mode: Int): Validation[Throwable, Cipher] = {
    validating {
      val pwd: String = masterSecret + appID.toString
      val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      val spec: KeySpec = new PBEKeySpec(pwd.toCharArray, salt.getBytes, 1024, 128)
      val tmp: SecretKey = factory.generateSecret(spec)
      val secret: SecretKey = new SecretKeySpec(tmp.getEncoded, "AES")
      val ips: IvParameterSpec = new IvParameterSpec(iv)
      val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(mode, secret, ips)
      cipher
    }
  }

}

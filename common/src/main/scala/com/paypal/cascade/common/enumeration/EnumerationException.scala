package com.paypal.cascade.common.enumeration

/**
 * Exception type for failed String-to-Enumeration reading
 * @param unknownValue the supposed stringVal of the failed Enumeration read
 */
class EnumerationException(unknownValue: String) extends Exception("Unknown enumeration value " + unknownValue)

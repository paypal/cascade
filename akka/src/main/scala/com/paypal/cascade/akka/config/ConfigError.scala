package com.paypal.cascade.akka.config

/**
 * Represents a configuration error.
 *
 * @param name the name of the config in error
 */
class ConfigError(name: String) extends Error(name)

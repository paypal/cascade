package com.paypal.stingray.common.constants

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/12/13
 * Time: 12:28 PM
 */
object ValueConstants {
  // Static Values
  val StackMobEnvironment = "stackmob.environment"
  val BuildTag = "build_name"
  val Dependencies = "service_dependencies"
  val Hostname = "hostname"
  val BaseAPIUrl = "platform.apiURL"
  val BaseLongAPIUrl = "platform.longApiURL"
  val BasePushAPIUrl = "platform.pushURL"
  val BasePlatformUrl = "platform.url"
  val ProdURL = "platform.prod.url"
  val DevcenterURL = "platform.devcenter.url"
  val WebsiteURL = "platform.website.url"
  val MarketplaceURL = "platform.marketplace.url"
  val DeployURL = "platform.deploy.url"
  val BarneyStreamingURL = "platform.barney.streaming.url"
  val BarneyAdminURL = "platform.barney.admin.url"
  val GithubAppId = "platform.github.appid"
  val GithubSecret = "platform.github.secret"
  val IronhideHost = "ironhide.host"
  val IronhideSocketTimeout = "ironhide.socket_timeout"
  val IronhideConnTimeout = "ironhide.connection_timeout"
  val IronhideResponseCacheTime = "ironhide.response_cache_time"
  val BumblebeeHost = "bumblebee.host"
  val AllsparkHost = "allspark.host"
  val AllsparkSocketTimeout = "allspark.socket_timeout"
  val AllsparkConnTimeout = "allspark.connection_timeout"
  val AllsparkResponseCacheTime = "allspark.response_cache_time"
  val BlurrHost = "blurr.host"
  val SkywarpHost = "skywarp.host"
  val WheeljackHost = "wheeljack.host"
  val ArceeHost = "arcee.host"
  val JazzHost = "jazz.host"
  val JazzPort = "jazz.port"
  val CookieDomain = "platform.cookieDomain"
  val Ec2LoadBalancer = "ec2.loadBalancer"
  val ClusterName = "cluster.name"
  val ZkServers = "zk.servers"
  val RiemannHost = "riemann.host"
  val RiemannPort = "riemann.port"

  val YakkoHost = "yakko.host"

  val LocalDocsFilePath = "platform.docs.filepath"
  
  val APICompressionLevel = "api.compression.level"
  val APIMaxRequestSize = "api.max.request.size.bytes"
  val APIMaxResponseSize = "api.max.response.size.bytes"
  val APIMaxHeaderSizeBytes = "api.max.header.size.bytes"
  val APIMaxLineLengthBytes= "api.max.line.length.bytes"

  val DBHost = "db.host"
  val DBPort = "db.port"
  val DBDatabase = "db.database"
  val DBUsername = "db.username"
  val DBPassword = "db.password"
  val DBJdbcString = "db.jdbc_string"
  val DBInitialPoolSize = "db.initial_pool_size"
  val DBMinPoolSize = "db.min_pool_size"
  val DBMaxPoolSize = "db.max_pool_size"
  val DBAcquireIncrement = "db.acquire_increment"
  val DBMaxIdleTime = "db.max_idle_time"
  val DBMaxConnectionAge = "db.max_connection_age"
  val DBMaxIdleTimeExcessConnections = "db.max_idle_time_excess_connections"
  val DBStatementCacheNumDeferredCloseThreads= "db.statement_cache_num_deferred_close_threads"
  val DBAutomaticTestTable = "db.automatic_test_table"
  val DBIdleConnectionTestPeriod = "db.idle_connection_test_period"
  val DBPreferredTestQuery = "db.preferred_test_query"
  val DBTestConnectionOnCheckin = "db.test_connection_on_checkin"
  val DBTestConnectionOnCheckout = "db.test_connection_on_checkout"
  val DBMaxStatements = "db.max_statements"
  val DBMaxStatementsPerConnection = "db.max_statements_per_connection"
  val DBAcquireRetryAttempts = "db.acquire_retry_attempts"
  val DBAcquireRetryDelay = "db.acquire_retry_delay"
  val DBBreakAfterAcquireFailure = "db.break_after_acquire_failure"
  val DBAutoCommitOnClose = "db.auto_commit_on_close"
  val DBForceIgnoreUnresolvedTransactions = "db.force_ignore_unresolved_transactions"
  val DBDebugUnreturnedConnectionStackTraces = "db.debug_unreturned_connection_stack_traces"
  val DBUnreturnedConnectionTimeout = "db.unreturned_connection_timeout"
  val DBCheckoutTimeout = "db.checkout_timeout"
  val DBMaxAdministrativeTaskTime = "db.max_administrative_task_time"
  val DBNumHelperThreads = "db.num_helper_threads"
  val DBUseTraditionalReflectiveProxies = "db.use_traditional_reflective_proxies"
  val DBPropertyCycle = "db.property_cycle"
  val DBMaxThreadsAwaitingCheckout = "db.max_threads_awaiting_checkout"

  val MemcachedHosts = "memcached.hosts"
  val MemcachedPort = "memcached.port"
  val MemcachedPoolSize = "memcached.conn_pool_size"
  val MemcachedOpTimeout = "memcached.op_timeout"
  val MemcachedConnTimeout = "memcached.conn_timeout"

  val MongoLoggingDBSuffix = "logs"
  val MongoLoggingColSuffix = "logs"
  val MongoCappedSize = 16777216
  val MongoHost = "mongo.host"
  val MongoLoggingHost = "mongo.logging.host"
  val MongoAnalyticsHost = "mongo.raw_analytics.host"
  val MongoAnalyticsDB = "mongo.raw_analytics.db"
  val MongoAggregatedAnalyticsHost = "mongo.aggregated_analytics.host"
  val MongoAggregatedAnalyticsDB = "mongo.aggregated_analytics.db"
  val MongoLogLocation = "mongo.log.location"
  val RabbitmqHost = "rabbitmq.host"
  val RabbitmqPort = "rabbitmq.port"
  val StatsdHost = "statsd.host"
  val StatsdPort = "statsd.port"
  val JenkinsHost = "jenkins.host"
  val JenkinsPort = "jenkins.port"
  val FlumeHost = "flume.host"
  val FlumePort = "flume.port"
  val FlumeReconnectAttempts = "flume.reconnect_attempts"
  val FlumeRetryAttempts = "flume.retry_attempts"
  val DexLocation = "dex.location"

  // Dynamic Values
  val BillingSandboxName = "billing.sandbox.name"
  val BillingSandboxDescription = "billing.sandbox.desc"
  val BillingSandboxAPI = "billing.sandbox.api"
  val BillingSandboxEmail = "billing.sandbox.email"
  val BillingSandboxPush = "billing.sandbox.push"
  val BillingSandboxCustomCode = "billing.sandbox.customcode"
  val BillingSandboxDatastore = "billing.sandbox.datastore"
  val BillingLimitsActive = "billing.limits.active"
  val BillingAnalyticsDaysToKeep = "billing.analytics.daystokeep"

  val AnalyitcsHitGlobalMongo = "analytics.hit.global.mongo"
  val AnalyitcsHitPerAppMongo = "analytics.hit.perapp.mongo"

  val ClusterHasAgencies = "cluster.agencies.exist"

  val DeploymentHistoryMaxCount = "deployhistory.max_count"

  val Html5FileMaxSizeInMB = "html5.file.maxsize"

  val AgencySafehouseMaxStartTime = "customcode.safehouse.maxstarttime"
  val AgencyMaxSafehousePorts = "customcode.agency.maxsafehouseports"
  val AgencyShouldStreamSafehouseStdout = "custcomcode.agency.capturesafehousestdout"
  val AgencyMaxJarSizeKB = "customcode.agency.maxsafehousesizekb"

  val SafehouseJVMArgs = "safehouse.jvmargs"
  val SafehouseJettyPort = "safehouse.jetty.port"

  val LoggingVerbose = "logging.verbose"

  val GetClientLoginTokenFakeCaptchaChallenge = "platform.config.console.push.clientlogin.fakecaptchachallenge"

  val StackMobClient = "stackmob.client.id"

  val ServerNonceExpirySeconds = "server.nonce.expiry.seconds"

  val ShowHTML5 = "platform.ui.html5"

  val RequestFuturePool = "request.future.pool"

  val MongoCappedSizeKey = "logging.mongo.capped_size"
  val MongoLogginEnabledKey = "logging.mongo.enabled"

  val CustomCodeLoggerServiceMacCallsPerDay = "cc.loggerservice.max_calls.global_per_day"
  val CustomCodeLoggerServiceMacCallsPerAppPerDay = "cc.loggerservice.max_calls.global_per_app_per_day"
}

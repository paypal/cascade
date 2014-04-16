resolvers += "Stingray Nexus" at "http://stingray-nexus.stratus.dev.ebay.com/nexus/content/groups/public/"

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.4")

addSbtPlugin("com.paypal.stingray" % "sbt-build-utilities" % "0.3.1-SNAPSHOT")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.7.2")

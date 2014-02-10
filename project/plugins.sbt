resolvers += "Stingray Nexus" at "http://stingray-jenkins-145195.phx-os1.stratus.dev.ebay.com:8081/nexus/content/groups/public/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.4")

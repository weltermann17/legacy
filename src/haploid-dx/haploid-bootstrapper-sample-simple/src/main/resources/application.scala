// application.scala

import com.typesafe.config.ConfigFactory.parseString

parseString("""
haploid.core.loglevel = WARNING
haploid.core.version = "4.7.12"
haploid.bootstrapper.maximum-restarts=1
""")      



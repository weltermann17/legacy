// application.scala

import com.typesafe.config.ConfigFactory.parseString

parseString("""
haploid.core.loglevel = WARNING
haploid.core.version = "4.7.1444447"
haploid.bootstrapper.maximum-restarts=1
""")      



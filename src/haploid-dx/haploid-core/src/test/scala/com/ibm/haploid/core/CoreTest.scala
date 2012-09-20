package com.ibm.haploid

package core

import org.junit.Assert.assertTrue
import org.junit.Test

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Test private class CoreTest {

  @Test def testCore = {
    val conf = config.toString
    assertTrue(0 < conf.length)
  }

  @Test def testLogging = {
    logger.debug("debug")
    logger.info("info")
    logger.warning("warning")
    logger.error("error")
    logger.error(xml)
    logger.error("<>&\"'")
    logger.error("'This is in apostrophs.'")
    assertTrue(true)
  }

  val xml = """
<configuration scan="false" debug="true">
	<appender name="TEXT"
		class="ch.qos.logback.core.rolling.RollingFileAppender">
		<file>${haploid.core.log-file}</file>
		<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
			<fileNamePattern>${haploid.core.log-file-rolling-pattern-logback}</fileNamePattern>
			<cleanHistoryOnStart>true</cleanHistoryOnStart>
		</rollingPolicy>
		<encoder>
			<pattern>${haploid.core.log-pattern}</pattern>
		</encoder>
		<filter class="com.ibm.haploid.core.util.logging.LoggerNameFilter" />
	</appender>
	<appender name="HTML"
		class="ch.qos.logback.core.rolling.RollingFileAppender">
		<file>${haploid.core.log-file-html}</file>
		<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
			<fileNamePattern>${haploid.core.log-file-html-rolling-pattern-logback}</fileNamePattern>
			<cleanHistoryOnStart>true</cleanHistoryOnStart>
		</rollingPolicy>
		<encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
			<layout class="ch.qos.logback.classic.html.HTMLLayout">
				<pattern><![CDATA[%level%relative%replace(%replace(%replace(%msg){'\n','<br>'}){'<', '&lt;'}){'>', '&gt;'}%logger%X{sourceThread}]]></pattern>
			</layout>
		</encoder>
		<filter class="com.ibm.haploid.core.util.logging.LoggerNameFilter" />
	</appender>
	<root level="${haploid.core.log-level}:-DEBUG">
		<appender-ref ref="TEXT" />
		<appender-ref ref="HTML" />
	</root>
</configuration>
"""
    
}


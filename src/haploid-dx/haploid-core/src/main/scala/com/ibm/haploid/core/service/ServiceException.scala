package com.ibm.haploid.core.service

/**
 * All business logic exceptions should mix-in this trait. This is just a marker interface.
 */
trait ServiceException extends Exception 

/**
 * A simple and non-specific ServiceException
 */
case class SimpleServiceException(message: String) 

extends Exception(message) 

with ServiceException


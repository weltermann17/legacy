package com.ibm.haploid

package core

/**
 * This package is a wrapper around the subcut dependency injection framework.
 */
package object inject {

  type Injectable = org.scala_tools.subcut.inject.Injectable

  type BindingModule = org.scala_tools.subcut.inject.BindingModule

  type BaseBindingModule = org.scala_tools.subcut.inject.NewBindingModule

}


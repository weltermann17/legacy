package com.example

object Division extends Enumeration {
  type Division = Value
  val Truck = Value("truck")
  val Engine = Value("engine")
}

object Subsystem extends Enumeration {
  type Subsystem = Value

  val Enovia5 = Value("enovia5")
}

object Type extends Enumeration {
  type Type = Value

  val Instance = Value("instances")
  val Partnerversion = Value("partnerversions")
  val Product = Value("products")
  val Version = Value("versions")
}
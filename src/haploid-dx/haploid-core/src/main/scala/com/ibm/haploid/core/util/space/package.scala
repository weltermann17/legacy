/*
 * Copyright 2010 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.haploid

package core

package util

// import language.implicitConversions

/**
 * Utilities to ease the handling of space values like bytes and megabytes and converting them.
 */
package object space {

  /**
   * We dropped our own implementation and simple wrap the one from twitter-util.
   */
  final class SpaceUnit(value: Long) extends com.twitter.conversions.storage.RichWholeNumber(value)

  implicit def int2SpaceUnit(value: Int) = com.twitter.conversions.storage.intToStorageUnitableWholeNumber(value)
  implicit def long2SpaceUnit(value: Long) = com.twitter.conversions.storage.longToStorageUnitableWholeNumber(value)

}

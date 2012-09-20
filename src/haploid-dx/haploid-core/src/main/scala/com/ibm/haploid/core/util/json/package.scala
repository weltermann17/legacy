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

/**
 * Providing a wrapper around json/jackson as it is so much faster than the scala internal parser.
 */
package object json {

  /**
   * We dropped our own implementation of a json/jackson wrapper for Jerkson and added small thinks like prettyPrint.
   */
  
  object Json extends com.codahale.jerkson.Json {
    
    def prettyPrint[A](a: A): String = {
      mapper.writerWithDefaultPrettyPrinter.writeValueAsString(a)      
    }
    
  }
  
}

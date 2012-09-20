//package com.ibm.haploid
//
//package hybriddb
//
//import java.sql.Timestamp
//
//import org.junit.Assert.assertTrue
//import org.junit.Test
//
//import core.file._
//import core.util.json._
//import schema.column._
//import schema._
//import io._
//
//import core.inject._
//import core.reflect._
//import core.util.time.now
//
//case class TestTable(val length: Int) extends Columns {
//
//  val id = new UniqueColumn[String]
//  val name = new CompressedColumn[String]
//  val valid = new BooleanColumn
//  val modified = new ArrayColumn[Timestamp]
//
//}
//
//object TestTable extends TableBuilder[TestTable]
//
//
//
//@Test private class HybriddbTest {
//
//  val input = List(
//    Map("id" -> "a", "name" -> "A", "valid" -> true, "modified" -> new Timestamp(now)),
//    Map("id" -> "b", "name" -> "B", "valid" -> false, "modified" -> new Timestamp(now)),
//    Map("id" -> "c", "name" -> "C", "valid" -> true, "modified" -> new Timestamp(now)),
//    Map("id" -> "d", "name" -> "D", "valid" -> false, "modified" -> new Timestamp(now)),
//    Map("id" -> "e", "name" -> "E", "valid" -> true, "modified" -> new Timestamp(now)))
//
//  @Test def testMatrix = {
//    assertTrue(""""m1":1,"m2":0,"m3":0,"m4":0,"m5":1,"m6":0,"m7":0,"m8":0,"m9":1,"m10":0,"m11":0,"m12":0""" == UnityMatrix.toString)
//    assertTrue(1 == UnityMatrix.m1)
//    implicit val prefix = "volume"
//    assertTrue(""""volume1":1,"volume2":0,"volume3":0,"volume4":0,"volume5":1,"volume6":0,"volume7":0,"volume8":0,"volume9":1,"volume10":0,"volume11":0,"volume12":0""" == UnityMatrix.asString)
//    val m = Matrix(3.14, 0, 0, 1.111, 1, 0, 0, 0, 1, 6000000000.0 / 7.123456, 8 / 9.0, 5 / (6000.0 * 7000.0))
//    val r = """"m1":3,14000000000000,"m2":0,00000000000000,"m3":0,00000000000000,"m4":1,11100000000000,"m5":1,00000000000000,"m6":0,00000000000000,"m7":0,00000000000000,"m8":0,00000000000000,"m9":1,00000000000000,"m10":842287788,399339,"m11":0,888888888888889,"m12":1,19047619047619e-07"""
//    // assertTrue(""""m1":3.14000000000000,"m2":0.00000000000000,"m3":0.00000000000000,"m4":1.11100000000000,"m5":1.00000000000000,"m6":0.00000000000000,"m7":0.00000000000000,"m8":0.00000000000000,"m9":1.00000000000000,"m10":842287788.399339,"m11":0.888888888888889,"m12":1.19047619047619e-07""" == m.toString)
//  }
//
//  @Test def testTableSchema = {
//    for (i <- 1 to 10) {
//      val t = TestTable(input)
//      assertTrue(List(("name", 5), ("id", 5), ("valid", 5), ("modified", 5)).sorted == t.columns.toList.map { case (name, column) => (name, column.length) }.sorted)
//    }
//  }
//
//  @Test def testSimpleFiller = {
//    for (i <- 1 to 1) {
//      val t1 = TestTable(input)
//      println("t1 " + t1)
//      val tmp = temporaryFile
//      new Serializer(tmp).extract(t1)
//      val des: Deserializer[TestTable] = new Deserializer(tmp)
//      val t2 = des.fill
//      println("t2 " + t2)
//      val t3 = TestTable(t1)
//      println("t3 " + t3)
//      val json = Json.generate(t3)
//      println(json)
//      val seq = Json.parse[Seq[Map[String, _]]](json)
//      println(seq)
//      val t4 = TestTable(seq)
//      println("t4 " + t4)
//      assertTrue(t1 == t2)
//      assertTrue(t1 == t3)
//      assertTrue(t1 == t4)
//      println("filtered " + t4.filter(_.getOrElse("valid", false).asInstanceOf[Boolean]))
//      println("groupby valid " + t4.groupBy(_.get("valid")))
//      
//     
//    }
//  }
//  
//}
//

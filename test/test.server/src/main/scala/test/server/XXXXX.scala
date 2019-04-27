package test.server

import test.server.compile.CusSparkILoop



object XXXXX extends App {
  println {
    CusSparkILoop.run(List(
      """
         val rdd = spark.sparkContext.parallelize(Array("a,b,c,d", "a,b,c,d"))
         rdd.flatMap(x => x.split(",")).map(x => (x, 1)).reduceByKey(_ + _).collect()
      """.stripMargin,
      """
        spark.stop()
      """.stripMargin
    ))
  }

}
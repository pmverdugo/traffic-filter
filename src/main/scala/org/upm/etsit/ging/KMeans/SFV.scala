package org.upm.etsit.ging.KMeans

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.feature.{StandardScaler, VectorAssembler}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Random

/**
  * Created by Administrator on 11/01/2017.
  */
class SFV(private val spark: SparkSession) {
  // SFV

  def muestraResultados(data: DataFrame): Unit = {
    val numericOnly = data.drop("protocolo", "servicio", "flag").cache()
    (60 to 270 by 30).map(k => (k, muestraResultados(numericOnly, k))).foreach(println)
    numericOnly.unpersist()
  }

  def muestraResultados(data: DataFrame, k: Int): Double = {
    val assembler = new VectorAssembler().
      setInputCols(data.columns.filter(_ != "etiqueta")).
      setOutputCol("vectorCaract")

    val scaler = new StandardScaler()
      .setInputCol("vectorCaract")
      .setOutputCol("scaledvectorCaract")
      .setWithStd(true)
      .setWithMean(false)

    val kmeans = new KMeans().
      setSeed(Random.nextLong()).
      setK(k).
      setPredictionCol("cluster").
      setFeaturesCol("scaledvectorCaract").
      setMaxIter(40).
      setTol(1.0e-5)

    val pipeline = new Pipeline().setStages(Array(assembler, scaler, kmeans))
    val pipelineModel = pipeline.fit(data)

    val kmeansModel = pipelineModel.stages.last.asInstanceOf[KMeansModel]
    kmeansModel.computeCost(pipelineModel.transform(data)) / data.count()
  }
}

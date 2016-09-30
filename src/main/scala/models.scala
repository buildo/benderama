package benderama

import breeze.stats.distributions.Gaussian
import breeze.linalg.DenseVector
import breeze.stats._

package object models {
  sealed trait BenderModel {
    def sample: BenderType
  }

  case class IntGaussianBenderModel(mean: Double, stddev: Double) extends BenderModel {
    private[this] val gauss = Gaussian(mean, stddev)

    def sample: BenderType = BenderType(gauss.draw.ceil.toInt)
  }

  object IntGaussianBenderModel {
    def apply(s: Seq[Int]): IntGaussianBenderModel = {
      val vector = DenseVector(s.map(_.toDouble):_*)
      val (μ, σ): (Double, Double) = (mean(vector), stddev(vector))
      IntGaussianBenderModel(μ, σ)
    }
  }

  case class StringFrequencyBasedBenderModel(private val s: Seq[String]) extends BenderModel {
    import scala.collection.immutable.TreeMap

    private[this] val frequency = s.groupBy(identity).mapValues(_.length)
    private[this] val cumulativeFrequency: TreeMap[Int, String] =
      TreeMap(frequency.scanLeft((0, "")) { case (p, c) => (p._1 + c._2, c._1)  }.tail.toArray:_*)

    def sample: BenderType = {
      val sampledValue = scala.util.Random.nextInt(frequency.values.sum) + 1
      val tmp = cumulativeFrequency.to(sampledValue)
      if (tmp.isEmpty) BenderType(s(0))
      else BenderType(tmp.last._2)
    }
  }

  implicit class TrainableSequence(s: Map[String, BenderColumn]) {
    def train(
      customModelMappings: Map[String, BenderModel] = Map.empty
    ): Map[String, BenderModel] = s map { case (k, v) =>
      val model: BenderModel = v match {
        case BenderColumnString(_, data) =>
          val customModel = customModelMappings.get(k)
          customModel match {
            case Some(model) => model
            case None => StringFrequencyBasedBenderModel(data)
          }
        case BenderColumnInt(_, data) =>
          val customModel = customModelMappings.get(k)
          customModel match {
            case Some(model) => model
            case None => IntGaussianBenderModel(data)
          }
      }

      (k -> model)
    }
  }
}

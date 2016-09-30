package benderama

package object sampler {
  import models._

  implicit class Sampler(m: Map[String, BenderModel]) {
    def draw(): Map[String, BenderType] = {
      m.map { case (k, v) => (k -> v.sample) }
    }
  }
}

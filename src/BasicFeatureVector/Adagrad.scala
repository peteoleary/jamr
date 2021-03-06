package edu.cmu.lti.nlp.amr.BasicFeatureVector
import edu.cmu.lti.nlp.amr._
import edu.cmu.lti.nlp.amr.Train._

import java.lang.Math.abs
import java.lang.Math.log
import java.lang.Math.exp
import java.lang.Math.random
import java.lang.Math.floor
import java.lang.Math.min
import java.lang.Math.max
import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math.sqrt

/******************************** Training **********************************/

class Adagrad extends Optimizer[FeatureVector] {
    def learnParameters(gradient: (Option[Int], Int, FeatureVector) => (FeatureVector, Double),
                        weights: FeatureVector,
                        trainingSize: Int,
                        passes: Int,
                        stepsize: Double,
                        l2reg: Double,
                        noreg: List[String],    // TODO: implement
                        trainingObserver: (Int, FeatureVector) => Boolean,
                        avg: Boolean) : FeatureVector = {
        var avg_weights = FeatureVector()
        var sumSq = FeatureVector()         // G_{i,i}
        var pass = 0
        while (pass < passes && trainingObserver(pass, weights)) {
            logger(0,"Pass "+(pass+1).toString)
            var objective = 0.0 // objective is 1/N \sum_i=1^N Loss(i) + 1/2 * \lambda * ||weights||^2 (var objective is N times this)
            for (t <- Random.shuffle(Range(0, trainingSize).toList)) {
                // normally we would do weights -= stepsize * gradient(t)
                // but instead we do this: (see equation 8 in SocherBauerManningNg_ACL2013.pdf)
                for ((feat, value) <- gradient(Some(pass), t, weights)._1.fmap
                     if value != 0.0 ) {
                    sumSq.fmap(feat) = sumSq.fmap.getOrElse(feat, 0.0) + value * value
                    weights.fmap(feat) = weights.fmap.getOrElse(feat, 0.0) - stepsize * value / sqrt(sumSq.fmap(feat))
                }
                if (l2reg != 0.0) {
                    for { (feat, v) <- weights.fmap
                          if v != 0.0
                          value = v * l2reg } {
                        sumSq.fmap(feat) = sumSq.fmap.getOrElse(feat, 0.0) + value * value
                        weights.fmap(feat) = weights.fmap.getOrElse(feat, 0.0) - stepsize * value / sqrt(sumSq.fmap(feat))
                    }
                }
            }
            logger(0,"                                   Avg objective value last pass: "+(objective/trainingSize.toDouble).toString)
            //logger(0,"                                                       objective: "+((0 until trainingSize).map(x => gradient(None, x, weights)._2).sum/trainingSize).toString)
            avg_weights += weights
            pass += 1
        }
        if(avg) { avg_weights } else { weights }
    }
}


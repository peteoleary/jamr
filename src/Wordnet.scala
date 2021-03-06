package edu.cmu.lti.nlp.amr

import scala.util.matching.Regex
import scala.collection.mutable.{Map, Set, ArrayBuffer}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._ // see http://stackoverflow.com/questions/16162090/how-to-convert-java-util-list-to-scala-list

import java.net.URL
import java.io.File

import edu.mit.jwi.RAMDictionary
import edu.mit.jwi.IRAMDictionary
import edu.mit.jwi.data.ILoadPolicy
import edu.mit.jwi.item.POS
import edu.mit.jwi.morph.WordnetStemmer

object Wordnet {

  private var wordnetStemmer : Option[WordnetStemmer] = None

    def init(wnhome: String) {
      val path : String = wnhome + File.separator + "dict"
      val url : URL = new URL("file", null, path)
      val dict : IRAMDictionary = new RAMDictionary(url, ILoadPolicy.NO_LOAD)
      dict.open
      wordnetStemmer  = Some(new WordnetStemmer(dict))
    }

    def stemmer(word: String) : List[String] = {
        var stems = List[String]()
        for (pos <- POS.values) {
            try { stems ++= wordnetStemmer.get.findStems(word, pos) }
            catch { case e : Throwable => Unit }
        }
        return stems.distinct.sorted
    }

    def stemmer(word: String, pos: POS) : List[String] = {
        try { wordnetStemmer.get.findStems(word, pos).asScala.toList }
        catch { case e : Throwable => List() }
    }

}


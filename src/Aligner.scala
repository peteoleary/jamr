package edu.cmu.lti.nlp.amr

import java.io.PrintStream

import scala.io.Source._
import scala.util.matching.Regex
import scala.collection.mutable.{Map, Set, ArrayBuffer}
import java.util.Date
import java.text.SimpleDateFormat

/****************************** Driver Program *****************************/
object Aligner {

    val usage = """Usage: scala -classpath . edu.cmu.lti.nlp.amr.Aligner < amr_file > alignments"""
    type OptionMap = Map[Symbol, Any]

    def parseOptions(map : OptionMap, list: List[String]) : OptionMap = {
        def isSwitch(s : String) = (s(0) == '-')
        list match {
            case Nil => map
            //case "--train" :: tail =>
            //          parseOptions(map ++ Map('train -> true), tail)
            //case "-a" :: value :: tail =>
            //          parseOptions(map ++ Map('amrfile -> value), tail)
            //case "--only" :: tail =>
            //          parseOptions(map ++ Map('only -> true), tail)
            case "-h" :: value :: tail =>
                      parseOptions(map ++ Map('help -> value.toInt), tail)
            case "-1" :: tail =>
                      parseOptions(map ++ Map('aligner1 -> true), tail)
            case "-v" :: value :: tail =>
                      parseOptions(map ++ Map('verbosity -> value.toInt), tail)
            case "--input" :: value :: tail =>             parseOptions(map ++ Map('input -> value), tail)
            case "--output" :: value :: tail =>             parseOptions(map ++ Map('output -> value), tail)
            case "--wnhome" :: value :: tail =>             parseOptions(map ++ Map('wnhome -> value), tail)
             //case string :: opt2 :: tail if isSwitch(opt2) => 
            //          parseOptions(map ++ Map('infile -> string), list.tail)
            //case string :: Nil =>  parseOptions(map ++ Map('infile -> string), list.tail)
            case option :: tail => println("Error: Unknown option "+option) 
                               sys.exit(1) 
      }
    }

    def main(args: Array[String]) {
        val options = parseOptions(Map(),args.toList)
        if (options.contains('help)) { println(usage); sys.exit(1) }

        if (options.contains('verbosity)) {
            verbosity = options('verbosity).asInstanceOf[Int]
        }

        val wnhome: String = if (options.contains('wnhome))
          options('wnhome).asInstanceOf[String]
        else
          System.getenv("WNHOME")


        Wordnet.init(wnhome)

        var aligner2 = true
        if (options.contains('aligner1)) {
            aligner2 = false
        }

      val outStream = if (options.contains('output)) new PrintStream(options('output).asInstanceOf[String]) else System.out
      val input = if (options.contains('input)) Source.fromFile(options('input).asInstanceOf[String]).getLines() else stdin.getLines


      val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        for (block <- Corpus.splitOnNewline(input)) {
            if (block.split("\n").exists(_.startsWith("("))) {  // Does it contain some AMR?
                logger(1,"**** Processsing Block *****")
                logger(1,block)
                logger(1,"****************************")
                val extrastr : String = block.split("\n[(]")(0)
                val amrstr : String = "(" + block.split("\n[(]").tail.mkString("\n(")
                outStream.println(extrastr)
                val amr = Graph.parse(amrstr)
                val extras = AMRTrainingData.getUlfString(extrastr)
                val tokenized = extras("::tok").split(" ")
                // val wordAlignments = AlignWords.alignWords(tokenized, amr)

                amr.printNodes.foreach(n => logger(1, f"$n"))

                // spanAlignments isn't used, output of .align and .alignSpans is modification of
                // amr.spans
                val spanAlignments = if (aligner2) {
                        AlignSpans2.align(tokenized, amr)
                    } else {
                        AlignSpans.alignSpans(tokenized, amr, AlignWords.alignWords(tokenized, amr))
                    }
                AlignSpans.logUnalignedConcepts(amr.root)

                val spans = amr.spans
                for ((span, i) <- spans.zipWithIndex) {
                    outStream.println("Span "+(i+1).toString+":  "+span.words+" => "+span.amr)
                    logger(3, "* "+span.format)
                }
                outStream.println("# ::alignments "+spans.map(_.format).mkString(" ")+" ::annotator Aligner v.02 ::date "+sdf.format(new Date))
                outStream.println(amrstr+"\n")
            } else {
                outStream.println(block+"\n")
            }
        }
    }

}


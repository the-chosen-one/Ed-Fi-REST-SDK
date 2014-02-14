/**
 *
 */
package org.edfi.restsdk

import com.wordnik.swagger.codegen.BasicCSharpGenerator
import com.wordnik.swagger.model.Operation
import java.io.File
import javax.net.ssl.TrustManager
import scala.collection.mutable.HashMap
import scopt.OptionParser

case class CommandlineConfig(generator: String = "csharp",
                             url: String = "",
                             baseDir: String = "generated-sdk" + java.io.File.separator + "csharp",
                             helperPackage: String = "EdFi.Ods.Generated.Sdk",
                             modelPackage: String = "EdFi.Ods.Generated.Model",
                             apiPackage: String = "EdFi.Ods.Generated.Api")
                             
object EdFiRestGeneratorLauncher {

  def main(args: Array[String]) = {
    val parser = new OptionParser[CommandlineConfig]("java -jar sdk-generate.jar") {
      head("sdk-generate v1.1 - Generate an SDK for the Ed-Fi Rest API")

      arg[String]("target-language") required() action { (x, c) => c.copy(generator = x)} validate { x => if (x == "csharp" || x == "java") success else failure("Only 'csharp' and 'java' currently supported as a target language.") } text("The target language. Choices are: csharp, java")
      opt[String]('u', "url") required() text("The url of a Ed-Fi Rest API metadata endpoint, such as the ones for resources, types and descriptors.  Example: 'https://tn-rest-production.cloudapp.net/metadata/descriptors/api-docs'")
      opt[String]('b', "baseDir") required() text("The base directory for the output of generated SDK files.")
      opt[String]('h', "helperPackage") required() text("The namespace/package and directory structure for generated SDK helper classes.  Example: 'EdFi.Ods.Generated.Sdk'.")
      opt[String]('m', "modelPackage") required() text("The namespace/package and directory structure for generated SDK model classes.  Example: 'EdFi.Ods.Generated.Model'.")
      opt[String]('a', "apiPackage") required() text("The namespace/package and directory structure for generated SDK API classes.  Example: 'EdFi.Ods.Generated.Api'.")
      help("help") text ("Prints this usage text.")
    }

    parser.parse(args, CommandlineConfig()) map { config =>
      config.generator match {
        case "csharp" => CSharpEdFiRestGenerator.main(args.tail)
        case "java" => JavaEdFiRestGenerator.main(args.tail)
        case _ => System.exit(0)
      }
    } getOrElse {
      System.exit(0)
    }
  }
  

 
}
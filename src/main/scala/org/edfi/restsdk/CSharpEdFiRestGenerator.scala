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
                           
object CSharpEdFiRestGenerator extends BasicCSharpGenerator {

  def main(args: Array[String]) = {
    val parser = new OptionParser[CommandlineConfig]("java -jar sdk-generate.jar csharp") {
      head("sdk-generate csharp v1.1 - Generate a C# SDK for the Ed-Fi Rest API")
      opt[String]('u', "url") required() action { (x, c) => c.copy(url = x)} text("The url of a Ed-Fi Rest API metadata endpoint, such as the ones for resources, types and descriptors.  Example: 'https://tn-rest-production.cloudapp.net/metadata/descriptors/api-docs'")
      opt[String]('b', "baseDir") required() action { (x, c) => c.copy(baseDir = x)} text("The base directory for the output of generated SDK files.")
      opt[String]('h', "helperPackage") required() action { (x, c) => c.copy(helperPackage = x)} text("The C# namespace and directory structure for generated SDK helper classes.  Example: 'EdFi.Ods.Generated.Sdk'.")
      opt[String]('m', "modelPackage") required() action { (x, c) => c.copy(modelPackage = x)} text("The C# namespace and directory structure for generated SDK model classes.  Example: 'EdFi.Ods.Generated.Model'.")
      opt[String]('a', "apiPackage") required() action { (x, c) => c.copy(apiPackage = x)} text("The C# namespace and directory structure for generated SDK API classes.  Example: 'EdFi.Ods.Generated.Api'.")
    }

    parser.parse(args, CommandlineConfig()) map { config =>
      _baseDir = config.baseDir
      _invokerPackage = Some(config.helperPackage)
      _modelPackage = Some(config.modelPackage)
      _apiPackage = Some(config.apiPackage)
      
      // adapting to old style feed of args to generator, only needs url
      generateClient(Array(config.url))      
    } getOrElse {
      System.exit(0)
    }
  }
  
  override def generateClient(args: Array[String]) = {
    CertificateValidationDisabler.disable()
    generateClientWithoutExit(args)
    System.exit(0)
  }
  
  // adding type mapping for JSON "date-time" and "number" types
  override def typeMapping = Map(
    "array" -> "List",
    "boolean" -> "bool?",
    "date" -> "DateTime",
    "Date" -> "DateTime",
    "date-time" -> "DateTime",
    "double" -> "double?",
    "float" -> "float?",
    "int" -> "int?",
    "integer" -> "int?",
    "long" -> "long?",
    "number" -> "double?",
    "object" -> "object",
    "string" -> "string"
    )
  
  //set location of templates
  override def templateDir = "csharp"
  
  //set base directory for SDK
  def baseDir = _baseDir
  private var _baseDir = "generated-sdk" + java.io.File.separator + "csharp"
    
  //set location to write generated code
  override def destinationDir = baseDir + java.io.File.separator + "src"
    
  //set package for API invoker
  override def invokerPackage = _invokerPackage
  private var _invokerPackage = Some("EdFi.Ods.Generated.Sdk")
    
  //set package for models
  override def modelPackage = _modelPackage
  private var _modelPackage = Some("EdFi.Ods.Generated.Model")
    
  //set package for API classes
  override def apiPackage = _apiPackage
  private var _apiPackage = Some("EdFi.Ods.Generated.Api")
  
  //by default, model class filenames are same capitalization as resource
  //  need to capitalize to make Pascal case
  override def toModelFilename(name: String) = name.capitalize
  
  //by default, model classnames are lower camel case in type references
  override def toModelName(name: String) = name.capitalize
  
  // operation method name
  override def toMethodName(name: String) = name.capitalize
  
  //operation property names on method signature 
  override def toVarName(name: String): String = {
    val charactersToRemove = "-".toSet
    name match {
      case _ if (reservedWords.contains(name)) => escapeReservedWord(name)
      case "If-Match" => "IfMatch"
      case "If-None-Match" => "IfNoneMatch"
      case "namespace" => "@namespace"
      case _ => name.filterNot(charactersToRemove)
    }
  }
  
  override def toDeclaredType(dt: String): String = {
    val declaredType = dt.indexOf("[") match {
      case -1 => dt
      case n: Int => {
        if (dt.substring(0, n) == "Array")
          "List" + dt.substring(n).replaceAll("\\[", "<").replaceAll("\\]", ">")
        else dt.replaceAll("\\[", "<").replaceAll("\\]", ">")
      }
    }
    typeMapping.getOrElse(declaredType, declaredType.capitalize)
  }
  
  // response classes
  override def processResponseClass(responseClass: String): Option[String] = {
     responseClass match {
      case "void" => None
      case e: String => Some(typeMapping.getOrElse(e, toPascalGenericFriendly(e)))
    }
  }

  override def processResponseDeclaration(responseClass: String): Option[String] = {
	// Convert Array to C# List  
    val rc = responseClass.replaceFirst("Array", "List")
    rc match {
      case "void" => None
      case e: String => {
        val ComplexTypeMatcher = "(.*)\\[(.*)\\].*".r
        val t = e match {
          case ComplexTypeMatcher(container, inner) => {
            e.replaceAll(container, typeMapping.getOrElse(container, container))
          }
          case _ => e
        }
        Some(typeMapping.getOrElse(t, toPascalGenericFriendly(t)))
      }
    }
  }
  
  private def toPascalGenericFriendly(responseClass: String): String = {
    val indexOfStartOfGenericType = responseClass.indexOf('<') match {
      case -1 => -1
      case n => n + 1
    }

    responseClass.zipWithIndex.map {case (c: Char, i: Int) =>
    c match {
      case '[' => '<'
      case ']' => '>'
      case _ => i match {
        case 0 => c.toUpper
        case `indexOfStartOfGenericType` => c.toUpper
        case _ => c
      }  
    }}.mkString
  }
  
  override def prepareApiBundle(apiMap: Map[(String, String), List[(String, Operation)]] ): List[Map[String, AnyRef]] = {
    (for ((identifier, operationList) <- apiMap) yield {
      val basePath = identifier._1
      val name = identifier._2
      val className = toApiName(name)

      val m = new HashMap[String, AnyRef]

      m += "baseName" -> name
      m += "filename" -> toApiFilename(name)
      m += "name" -> toApiName(name)
      m += "className" -> className
      m += "basePath" -> basePath
      m += "package" -> apiPackage
      m += "invokerPackage" -> invokerPackage
      m += "modelPackage" -> modelPackage
      m += "apis" -> Map(className -> operationList.toList)
      m += "models" -> None
      m += "outputDirectory" -> (destinationDir + File.separator + apiPackage.getOrElse("").replace(".", File.separator))
      m += "newline" -> "\n"

      Some(m.toMap)
    }).flatten.toList
  }
  
  // supporting classes
  override def supportingFiles =
    List(
      ("TempModel.mustache", destinationDir + java.io.File.separator + modelPackage.get.replace(".", java.io.File.separator), "TempModel.cs"),
      ("BearerTokenAuthenticator.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator), "BearerTokenAuthenticator.cs"),
      ("ITokenRetriever.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator), "ITokenRetriever.cs"),
      ("TokenRetriever.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator), "TokenRetriever.cs")
      )

}
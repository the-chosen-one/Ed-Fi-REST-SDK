###Generating an Ed-Fi REST SDK

The Ed-Fi REST API exposes metadata describing the exposed resources as well as the inputs, verbs, and schema of the entities involved. This metadata is what the Swagger UI consumes to show the documentation and provide the ability to play with the API through a user interface. This same exact metadata is now being used to generate an SDK for both .NET and Java (Java supported is targeted for 2/24).

It's worth noting why we are releasing an SDK generator instead of an actual SDK. With the adoption rate of the Ed-Fi REST API across several states, many vendors will be integrating with the platform in various flavors - each one differing by the data extensions for a particular state. By releasing an SDK generator, each vendor is free to generate the SDK for each state on demand without waiting for an update to a separate SDK repository.

###Known Issues

The following are known issues for the SDK generator.

* It does not properly handle the if-none-match and if-match functionality. You will notice in the API code that those parameter names are missing the hyphens which make those parameters unrecognizable by the API.
* No support for async calls yet.
* The EducationOrganizationTelephone, EducationOrganizationIdentificationCode, EducationOrganizationCategory, and EducationOrganizationAddress are missing.
* Model properties aren't Pascal-cased (currently lower camel cased).
* The generator does not work with the AZ sandbox since that sandbox requires a new deployment.
* Generating from the "other" section does not currently work.

###Instructions on Generating an Ed-Fi REST SDK

Before starting, clone the codebase. This is the same repository for C# and Java. Note that the generator only creates the code files; no project or solution files are generated.

**From Windows**

1) Download the Java SDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html Version 7 or newer is recommended.

2) Download SBT and install. Just under the "Installing sbt" header download the appropriate package for your operating system. http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html

3) Ensure your environment can find javac. If you're on Windows set the PATH environment variable to find the jdk bin directory (e.g. C:\Program Files\Java\jdk1.7.0_51\bin).

4) Open a command prompt to the ed-fi-rest-sdk folder and type "sbt assembly". This will build the Swagger codegen jar file.

5) Navigate to the folder where the jar file was created and run the command from the command prompt for each section in Swagger.

Parameter names for the command to run the generator -

* Target Language - As of 2/10/14, C# is the only one supported.
* Section  - At the top of the Swagger page, there is a drop down list that shows how the resources are broken into separate sections (Resources, Types, Descriptors, and Other).
* Destination Folder - This is the folder where you would like the code files to be created.
* API Namespace - Namespace for the classes that expose methods that map to resources and verbs (e.g. GetSchoolsAll).
* SDK Namespace - Namespace for the general classes that support the REST API interaction (e.g. TokenRetriever to authenticate with OAuth2).
* Model Namespace - Namespace for the entities that are exchanged with the REST API.

java -jar sdk-generate.jar csharp --url https://tn-rest-production.cloudapp.net/metadata/{section}/api-docs --baseDir {destination folder} --apiPackage {API namespace} --helperPackage {SDK namespace} --modelPackage {Model namespace}

6) Create a new class library in Visual Studio and include all of the generated files. From there, plug in the API URI, key, and secret in the code below to your application to get the list of schools. The example below is from a simple Console application.

Before this will compile, use the Package Manager Console to install Restsharp - Install-Package Restsharp

```C#
// Trust all SSL certs -- needed unless signed SSL certificates are configured.
System.Net.ServicePointManager.ServerCertificateValidationCallback =
    ((sender, certificate, chain, sslPolicyErrors) => true);

// Oauth configuration
var oauthUrl = "https://{Root Administration Portal URI}";
var clientKey = "{API Key}";
var clientSecret = "{API Secret}";

// TokenRetriever makes the oauth  calls
var tokenRetriever = new TokenRetriever(oauthUrl, clientKey, clientSecret);

// Install RestSharp via NuGet
var client = new RestClient("https://{Root REST API URI}/api/v1.0");

// Plug Oauth into RestSharp's authentication scheme
client.Authenticator = new BearerTokenAuthenticator(tokenRetriever);

// GET schools
var api = new SchoolsApi(client);

var response = api.GetSchoolsAll(null, null); // offset, limit

var httpReponseCode = response.StatusCode; // should be System.Net.HttpStatusCode.OK
var schools = response.Data;

Console.WriteLine("Response code is " + httpReponseCode);

foreach(var school in schools)
{
    Console.WriteLine(school.nameOfInstitution);
}
```

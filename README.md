###Generating an Ed-Fi REST SDK

The Ed-Fi REST API exposes metadata describing the exposed resources as well as the inputs, verbs, and schema of the entities involved. This metadata is what the Swagger UI consumes to show the documentation and provide the ability to play with the API through a user interface. This same exact metadata is now being used to generate an SDK for both .NET and Java.

It's worth noting why we are releasing an SDK generator instead of an actual SDK. With the adoption rate of the Ed-Fi REST API across several states, many vendors will be integrating with the platform in various flavors - each one differing by the data extensions for a particular state. By releasing an SDK generator, each vendor is free to generate the SDK for each state on demand without waiting for an update to a separate SDK repository.

###Known Issues

The following are known issues for the SDK generator.

* It does not properly handle the If-None-Match and If-Match functionality. You will notice in the API code that those parameter names are missing the hyphens which make those parameters unrecognizable by the API.
* No support for async calls yet.
* The EducationOrganizationTelephone, EducationOrganizationIdentificationCode, EducationOrganizationCategory, and EducationOrganizationAddress are missing.
* Model properties aren't Pascal-cased in C# (currently lower camel cased).
* Generating from the "other" section does not currently work.

###Instructions on Generating an Ed-Fi REST SDK

Before starting, clone the codebase. This is the same repository for C# and Java. Note that the generator only creates the code files; no project or solution files are generated.

**From Windows**

1) Download the Java SDK from http://www.oracle.com/technetwork/java/javase/downloads/index.html Version 7 or newer is recommended.

2) Download SBT and install. Just under the "Installing sbt" header download the appropriate package for your operating system. http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html

3) Ensure your environment can find javac. If you're on Windows set the PATH environment variable to find the jdk bin directory (e.g. C:\Program Files\Java\jdk1.7.0_51\bin).

4) Open a command prompt to the ed-fi-rest-sdk folder and type "sbt assembly". This will build the Swagger codegen jar file.

5) Navigate to the folder where the jar file was created and run the command from the command prompt for each section in Swagger. This location is listed close to the end of the information log that is output during sbt generation.

Parameter names for the command to run the generator -

* Target Language - Either C# (csharp) or Java (java).
* Section  - At the top of the Swagger page, there is a drop down list that shows how the resources are broken into separate sections (Resources, Types, Descriptors, and Other).
* Destination Folder - This is the folder where you would like the code files to be created.
* API Namespace - Namespace for the classes that expose methods that map to resources and verbs (e.g. GetSchoolsAll).
* SDK Namespace - Namespace for the general classes that support the REST API interaction (e.g. TokenRetriever to authenticate with OAuth2).
* Model Namespace - Namespace for the entities that are exchanged with the REST API.

**C#**

1) Generate C# source files by running the generator once for each for each API section: descriptors, resources, and types.

```
java -jar sdk-generate.jar csharp --url https://{Domain root of API}/metadata/{section}/api-docs --baseDir {destination folder} --apiPackage {API namespace} --helperPackage {SDK namespace} --modelPackage {Model namespace}
```

2) Create a new class library in Visual Studio and include all of the generated files.

3) Use the Package Manager Console to install the RestSharp library.

```
Install-Package Restsharp
```

4) Take the sample code below and plug in the API URI, client key, and client secret in the code below to get the list of schools. The example below is from a simple Console application.

```
// Trust all SSL certs -- needed unless signed SSL certificates are configured.
System.Net.ServicePointManager.ServerCertificateValidationCallback =
    ((sender, certificate, chain, sslPolicyErrors) => true);

// Oauth configuration
var oauthUrl = "https://{Root Administration Portal URI}";
var clientKey = "{API Key}";
var clientSecret = "{API Secret}";

// RestSharp dependency, install via NuGet
var client = new RestClient("https://{Root REST API URI}/api/v1.0");

// TokenRetriever makes the oauth calls
var tokenRetriever = new TokenRetriever(oauthUrl, clientKey, clientSecret);

// Plug Oauth into RestSharp's authentication scheme
client.Authenticator = new BearerTokenAuthenticator(tokenRetriever);

// GET schools
var api = new SchoolsApi(client);

var response = api.GetSchoolsAll(null, null); // offset, limit

var httpReponseCode = response.StatusCode; // returns System.Net.HttpStatusCode.OK
var schools = response.Data;

Console.WriteLine("Response code is " + httpReponseCode);

foreach(var school in schools)
{
    Console.WriteLine(school.nameOfInstitution);
}
```

**Java**

1) Generate Java source files by running the generator once for each for each API section: descriptors, resources, and types.

```
java -jar sdk-generate.jar java --url https://tn-rest-production.cloudapp.net/metadata/{section}/api-docs --baseDir {destination folder} --apiPackage {API namespace} --helperPackage {SDK namespace} --modelPackage {Model namespace}
```

2) Create a new Maven project in Eclipse and import all of the generated files.

3) Overwrite the default pom.xml with the example.pom.xml file that was generated. The generated code has dependencies on JAX-RS (Jersey here), a JAX-RS JSON provider (Jackson here), and the Guava project.

4) Take the sample code below and plug in the API URI, client key, and client secret in the code below to get the list of schools. The example below is from a simple console application.

```
// Trust all SSL certs -- needed unless signed SSL certificates are configured.
CertificateValidationDisabler.disable();  // generated helper class

// Oauth configuration
String oauthUrl = "https://{Root Administration Portal URI}";
String clientKey = "{API Key}";
String clientSecret = "{API Secret}";

// TokenRetriever makes the oauth calls
TokenRetriever tokenRetriever = new RestApiTokenRetriever(oauthUrl, clientKey, clientSecret);

// JAX-RS dependency, installed via Maven
ClientBuilder clientBuilder = ClientBuilder.newBuilder();
Client client = clientBuilder.build().register(new BearerTokenAuthenticator(tokenRetriever));
WebTarget target = client.target("https://{Root REST API URI}/api/v1.0");

// GET schools
SchoolsApi api = new SchoolsApi(target);

// RestResponse wraps a JAX-RS Response to provide a properly typed readEntity() method
RestResponse<List<School>> response = api.getSchoolsAll(null, null); // offset, limit

int httpReponseCode = response.getStatus(); // returns Response.Status.OK
System.out.println("Response code is " + httpReponseCode);

for (School school : response.readEntity()) {
    System.out.println(school.getNameOfInstitution());
}

```

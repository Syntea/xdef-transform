# Mutual transformation of X-Definition and XML Schema

* Transformation from X-Definition to XML Schema can be **lossy**.
  * The user is informed of the lossy transformation by warning messages.
* Transformation from XML Schema to X-Definition should always be lossless (provided that the specific transformation is supported).

## Usage

### Console: X-Definition transformation to XML Schema

#### Prerequisites
* Installed JRE version 8 or higher.

#### Basic examples

* Executing application distribution
```console
.\xdef-transform-xsd.cmd
```

* Manual build
```console
java -jar .\xdef-transform-xsd.jar
```

* Output

```console
Missing required options: i, o
usage: xdef-transform-xsd ...
...
```

The console output includes descriptions of all options and their meaning in standard unix format.

* Required options:
  * `i` – directory containing input X-Definition file(s)
  * `o` – directory containing the output XML Schema file(s)

**Example of using**
```console
.\xdef-transform-xsd.cmd -i "C:\syntea\projects\xdefinition\input" -o "C:\syntea\projects\xdefinition\output"
```

* If you want to add *<xs: annotation>* nodes to the output XML Schema(s), which contain basic information about loss transformations, then just add the `-f` switch with the value` a`. Example:

```console
.\xdef-transform-xsd.cmd -i "C:\syntea\projects\xdefinition\input" -o "C:\syntea\projects\xdefinition\output" -f a
```

* The application execution can be extended by validation of XML data via an internal tool (Java library).
  * If you want to validate a valid (meaning in relation to the input X-Definition file(s)) XML file against the output of the algorithm (XML schema file(s)), then just use the `-tp` option. It is possible to test multiple XML data files at once.
  * At the same time, it is necessary to use the `-r` option to specify the name of the root X-Definition, in fact the name of the output XML schema file against which the data will be validated.
  * Example of a application execution with single data file:
  
```console
.\xdef-transform-xsd.cmd -i "C:\syntea\projects\xdefinition\input" -o "C:\syntea\projects\xdefinition\output" -r "root-name" -tp "C:\syntea\projects\xdefinition\data\testing-data.xml"
```

### Library: X-Definition transformation to XML Schema

The project can be used as third-party library. Example of usage:
```java
import org.xdef.transform.xsd.console.impl.DefaultXDefAdapter;
import org.xdef.transform.xsd.console.impl.XDefAdapterConfig;

XDefAdapterConfig xdefAdapterConfig = ...

DefaultXDefAdapter xDefAdapter = new DefaultXDefAdapter(xdefAdapterConfig);
xDefAdapter.transform();
```

Configuration of `XDefAdapterConfig` is similar to the command line options.

### Console: XML Schema transformation to X-Definition
TBU
### Library: XML Schema transformation to X-Definition
TBU

## Distribution

**Maven command:** creating a distribution archive containing the application
```
mvn clean package -Pdist
```

Distribution archive contains **two examples** to demonstrate using of application.
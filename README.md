# Spring CORS Filter

A simple OncePerRequestFilter implementation offering the possibility to add CORS relevant header to HTTP responses.

## Getting started

### Include the JAR

Include the JAR file as dependency on your project. It is availble through Maven central.

#### Maven

```
<dependency>
    <groupId>io.sprucehill</groupId>
    <artifactId>spring-cors-filter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle

```
compile 'io.sprucehill.spring-cors-filter:1.0.0
```

### Enable the bean

If you're using JavaConfig and ComponentScan, make sure, that `io.sprucehill.spring.filter` is scanned and add at least the following property to your `application.properties` file

```
io.sprucehill.spring.filter.cors.enable=true
```

Please refer to the included `application.properties` file for further configuration options.
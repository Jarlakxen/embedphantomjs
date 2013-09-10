embedphantomjs
==============

Embedded PhantomJS for Java

This project provides an easy interface for execute PhantomJS with Java.

## Why?

- Easy way to execute JS inside Java
- Easy way to make a Scraper with Java

### Maven

Stable [OSS Sonatype](https://oss.sonatype.org/content/repositories/releases/com/github/jarlakxen/embedphantomjs/maven-metadata.xml)

	<dependency>
		<groupId>com.github.jarlakxen</groupId>
		<artifactId>embedphantomjs</artifactId>
		<version>2.1</version>
	</dependency>

### Changelog

2.1 
- Add asyncronic execution

2.0 
- Sopport for versions 1.9.2, 1.9.1, 1.9.0, 1.8.2, 1.8.1, 1.8.0
- Sopport binary versioned

1.3
- Full support for input stream script

1.2

- Bug fixing

1.1
- Bug fixing

1.0
- Auto-detect OS
- Auto-detect architecture
- Sopport for versions 1.7.0, 1.6.1, 1.6.0, 1.5.0, 1.4.1, 1.4.0, 1.3.0
- Check native installation
- Download from page



### Supported Versions

Versions: 1.9.2, 1.9.1, 1.9.0, 1.8.2, 1.8.1, 1.8.0, 1.7.0, 1.6.1, 1.6.0, 1.5.0, 1.4.1, 1.4.0, 1.3.0
Support for Linux, Windows and MacOSX.

### Usage

	Configuration configuration = Configuration.create().useNativeInstallation(false).build();
	
	PhantomJSExecutor executor = new PhantomJSExecutor(configuration);

	String output = executor.execute(new File("~/scrapper.js"));

	or

	String output = executor.execute("console.log('TEST1');phantom.exit();")

	System.out.println(output);  // This prints "TEST1"

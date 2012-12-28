embedphantomjs
==============

Embedded PhantomJS for Java

## Howto

### Maven

Stable (Maven Central Repository, Released: dd.mm.aaaa - wait 24hrs for [maven central](http://repo1.maven.org/maven2/com/jarlakxen/embed/embedphantomjs/maven-metadata.xml))

	<dependency>
		<groupId>com.jarlakxen.embed</groupId>
		<artifactId>embedphantomjs</artifactId>
		<version>1.3</version>
	</dependency>

### Changelog

1.4 ( SNAPSHOT )

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

Versions: 1.7.0, 1.6.1, 1.6.0, 1.5.0, 1.4.1, 1.4.0, 1.3.0
Support for Linux, Windows and MacOSX.

### Usage

	PhantomJSConfiguration configuration = new PhantomJSConfiguration().setCheckNativeInstallation(false);
	
	PhantomJSExecutor executor = new PhantomJSExecutor(configuration);
	String output = executor.execute("~/scrapper.js");
	System.out.println(output);

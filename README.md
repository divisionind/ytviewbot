# YTViewBot - just a YouTube view bot
[![](https://img.shields.io/travis/divisionind/ytviewbot/master.svg?style=flat-square)](https://travis-ci.org/divisionind/ytviewbot)
![](https://img.shields.io/badge/platform-Linux-blue.svg?style=flat-square)
![](https://img.shields.io/badge/license-GPLv3-green.svg?style=flat-square)
![](https://img.shields.io/badge/dev%20status-active-brightgreen.svg?style=flat-square)

## Installation / Building
_Note: This project only supports Linux and is completely headless._

#### For dummies - installing from releases
1. Download the [latest version](https://github.com/divisionind/ytviewbot/releases/latest)
2. Extract the tarball with `tar -xzf ytviewbot-VERSION.tar.gz` and enter it `cd ytviewbot`
3. Install with `chmod +x install && sudo ./install`
4. Now run `ytviewbot` for further help.

#### Advanced - building from source
1. Clone repo and enter it `git clone https://github.com/divisionind/ytviewbot.git && cd ytviewbot`
2. Install prerequisites `chmod +x installDepends && sudo ./installDepends`
3. Build the project `./gradlew linux`
4. Now run `./build/libs/ytviewbot` for further help.

##### You can also choose to install from source
1. Clone repo and enter it `git clone https://github.com/divisionind/ytviewbot.git && cd ytviewbot`
2. Install `chmod +x install && sudo ./install`. This will install all dependencies, build the source, and install ytviewbot.
3. Now run `ytviewbot` for further help.


### Using Windows?
No sweat. You can run Linux on Windows. Just look up how to install the Windows Subsystem for Linux (WSL).


### Using Mac?
Use a better OS.


## Support
If you like the project, please share the wealth. I'm poor. I need money. Pls help.

- BTC: 1FpywKn3H2CrGUR1tziq5wjhwLeXHSet9C
- BCH: bitcoincash:qz32f4h83dn9fpju594eafm4hytr528l4c4utgyw66


## Contributing
All contributions must follow 
[Java's standard code conventions.](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf) 
Failure to comply to these conventions will result in the denial of your contribution, regardless 
of the final functioning of the code.

### What does this mean?
Not a lot. Just DO **NOT** do stuff like this:

```java
public void example()
{
    System.out.println("bad code");
}
```

or this

```java
System.out.println( getName( bad_code ) );
```

or this

```java
public void AnotherBadExample() {}
```

These practices make your code harder to read and are very annoying.

* * *

Do **THIS** instead:

```java
public void example() {
    System.out.println("good code");
}
```

or this

```java
System.out.println(getName(goodCode));
```

or this

```java
public void anotherGoodExample() {}
```
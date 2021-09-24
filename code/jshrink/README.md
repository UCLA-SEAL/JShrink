# Compiling JShrink

For most cases, the following steps will be sufficient. We utilize Vagrant
as it produces a stable environment for installation.

1. Please ensure you have valgrind installed on your system. With APT : `sudo
apt install vagrant`.
2. Copy the `../experiment_resources/Vagrantfile_local` file to
`Vagrantfile` (in the pwd):
`cp ../experiment_resources/Vagrantfile_local Vagrantfile`.
3. Execute `vagrant up`.
4. SSH into the VM: `vagrant ssh`.
5. Move to the `/vagrant` directory: `cd /vagrant`.
6. Compile JShrink using `mvn compile -pl jshrink-app  -am`.

The resulting jar can be found in
`jshrink-app/target/jshrink-app-1.0-SHAPSHOT-jar-with-dependencies.jar`.

## Installing without Valgrind

**WARNING: We strongly recommend compiling within Vagrant. Vagrant produces a
stable environment proven to compile JShrink successfully.**

JShrink requires the following dependencies to be installed:

* OpenJDK-8
* The Maven build tool
* GCC
* Make

For operating systems utilizing the APT build system:

```
sudo apt install openjdk-8-jdk maven gcc make
```

To then compile jshrink: `mvn compile -pl jshrink-app  -am`.

The resulting jar can be found in
`jshrink-app/target/jshrink-app-1.0-SHAPSHOT-jar-with-dependencies.jar`.

## Compiling with Soot v3.2

If Soot v3.2 is required, the app is compiled with the following command:

mvn --file pom_soot-3.2.xml compile -pl jshrink-app -am

## JShrink-Lib

Any compilation of JShrink will also compile the JShrink-Lib. This can be
found at
`jshrink-lib/target/jshrink-lib-1.0-SNAPSHOT-jar-with-dependencies.jar`.

To build jshrink-lib exclusively run the following command:

```
mvn compile -pl jshrink-lib -am
```

## Other Notes

* If you want to open this project in an IDE, please ensure that you
copy `jshrink-lib/src/main/resources/poa.properties` to
`<your home directory>/.tamiflex/poa.properties`.


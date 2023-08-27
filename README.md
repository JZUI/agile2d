<!--
SPDX-FileCopyrightText: 2012 Emmanuel Pietriga <emmanuel.pietriga@inria.fr>
SPDX-FileCopyrightText: 2006-2012 Jean-Daniel Fekete <jean-daniel.fekete@inria.fr>
SPDX-FileCopyrightText: 2012 Rodrigo A. B. de Almeida
SPDX-FileCopyrightText: 2006 Jon Meyer
SPDX-FileCopyrightText: 2006 Ben Bederson

SPDX-License-Identifier: BSD-4-Clause
-->

# Agile2D

[![License: BSD-4-Clause](
    https://img.shields.io/badge/License-BSD--4--Clause-blue.svg)](
    https://spdx.org/licenses/BSD-4-Clause.html)
[![REUSE status](
    https://api.reuse.software/badge/github.com/hoijui/agile2d)](
    https://api.reuse.software/info/github.com/hoijui/agile2d)

A framework that provides a nearly complete implementation of Java2D built using the industry standard OpenGL API.

## Building Agile with Maven

From the directory where the "pom.xml" file is located, type the command:

```shell
mvn -P[platform] clean package
```

Where [platform] can be osx, lin32, lin64, win32, or win64 (the default option is "osx", i.e., Mac OSX Universal)
You should be connected to the internet the first time you use this command so that Maven can fetch the missing dependencies of the project (e.g., JOGL libraries)

## Native Libraries

Agile2D uses JOGL, which is the "Java Binding for the OpenGL API" (check <http://jogamp.org/jogl> )
Thus, the native library files location must be indicated to the VM, in one of the two ways explained below:
A. Simply passing the flag "-Djava.library.path=target/lib/" to the JVM when launching the application
B. Setting the related environment variable, e.g. "export DYLD_LIBRARY_PATH=target/lib" (Warning. The name of this variable and its setting command vary across platforms.)
Observation: By default, Maven places the native library files in the local "./target/lib/" directory

## Launching a "HelloWorld" example and switching among GLCanvas, GLJPanel, and NEWT

```shell
java -Djava.library.path=target/lib/ -cp target/agile2d-3.0.jar agile2d.examples.HelloWorld [GLCanvas | GLJPanel | NEWT]
```

You should pass as argument 'GLCanvas', 'GLJPanel', or 'NEWT' in order to make Agile2D use respectively GLCanvas (Heavyweight component), GLJPanel (Swing Lightweight Component) or NEWT (JOGL's High Performance Native Windowing Toolkit)
The rendering performance (measured in frames per second) of GLCanvas and NEWT seems to be much superior to that of GLJPanel.

## Launching the Benchmark application

```shell
java -Djava.library.path=target/lib/ -cp target/agile2d-3.0.jar:target/test-classes agile2d.benchmark.BenchmarkGUI
```

Use this application to see the performance differences between Agile2D and Java2D but also among different GLDrawable implementations (GLCanvas, GLJPanel, NEWT) when drawing different quantities and types (text, images, shapes) of objects.

## Porting existing Java2D applications to Agile2D

Check the HelloWorld package (the HelloWorld and the AgileFrame classes) since it is a minimal example of how to use Agile2D with different components implementing GLDrawable (GLCanvas, GLJPanel, NEWT)

## Checking the rendering quality of Agile2D (comparing it to a "reference rendering", a pure Java Graphics2D rendering)

```shell
mvn clean package -DskipTests=false
```

A set of unit tests can be executed when Maven builds Agile2D package (by default, without passing the flag above, they are skipped).
In this tests, different shapes and objects are drawn at the same time on both a Graphics2D context and on a Agile2D one.
The result renderings are compared and if there is a great difference, the test fails (and the Agile2D package is not built).
Tests that fails produce a report where one can see the difference between the two renderings (reports are images saved on the local "./testResults" directory).

## Memory Issues

An application using many different font families may produce a memory error, since Agile2D caches the glyph geometry of every type. (e.g., Something like "OutOfMemoryError: Direct buffer memory")
In this case, you should increase the off-heap memory available to the VM. You can do that through the flag "MaxDirectMemorySize".
For example, if you want to increase the off-heap memory to, let's say, 1 Gb: `-XX:MaxDirectMemorySize=1g`
Observation: The off-heap memory is not subject to the Garbage Collector and is accessed via "direct ByteBuffers".

## Known Platform Issues

- The current version of JOGL (2.0-RC5) crashes in some platforms using OSX Lion (10.7) and OSX Leopard (10.5.8 and maybe below). JOGL Community is aware of these problems and may fix them in a future release.
- The current version of JOGL also crashes when using Java7 (Preview Release) under OSX (10.6.8 and 10.7 tested). It seems to work fine with Java7 under Windows7 (32 bits) and Linux (64 bits)

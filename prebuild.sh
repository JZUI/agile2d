#!/bin/bash

#Download and add the jogl 2.0 ZIP archive to the local 
#maven repository.
URL_JOGL=http://download.java.net/media/jogl/builds/archive/jsr-231-2.0-beta10

if [ $# != 1 ]
 then
  echo "\nBad Usage\n\nUsage: \"sh prebuild [PLATFORM]\"\n\nwhere PLATFORM must be one of the following:\n  osx \n  lin32 \n  lin64 \n  win32 \n  win64 \n"
  exit
fi

#According to the platform passed as argument,
#get package zip file name and
#dynamic library files extension
PLATFORM=$1
if [ "$PLATFORM" = "osx" ]
 then
  JOGL_PACKAGE=jogl-2.0-macosx-universal
  LIB_EXTENSION=jnilib
elif [ "$PLATFORM" = "lin32" ]
 then
  JOGL_PACKAGE=jogl-2.0-linux-i586
  LIB_EXTENSION=so
elif [ "$PLATFORM" = "lin64" ]
 then
  JOGL_PACKAGE=jogl-2.0-linux-amd64
  LIB_EXTENSION=so
elif [ "$PLATFORM" = "win32" ]
 then
  JOGL_PACKAGE=jogl-2.0-windows-i586
  LIB_EXTENSION=dll
elif [ "$PLATFORM" = "win64" ]
 then
  JOGL_PACKAGE=jogl-2.0-windows-amd64
  LIB_EXTENSION=dll
else
  echo "\nCannot recognize platform \"$PLATFORM\". PLATFORM argument must be one of the following :\n  osx \n  lin32 \n  lin64 \n  win32 \n  win64 \n"
  exit
fi

PREBUILD_ROOT=`pwd`

#Fecth the package thru a command line http client
if [ "$PLATFORM" = "osx" ]
 then
  cd `mktemp -d -t jogl` &&\
  curl $URL_JOGL/$JOGL_PACKAGE.zip >$JOGL_PACKAGE.zip
else
  cd `mktemp -d` &&\
  wget $URL_JOGL/$JOGL_PACKAGE.zip
fi

unzip $JOGL_PACKAGE.zip &&\
cd $JOGL_PACKAGE &&\
#Get the path to the directory where the archive has been inflated
TMP_PATH=`pwd`
#Copy dynamic library files to maven project directory
mkdir $PREBUILD_ROOT/lib
cp -v $TMP_PATH/lib/lib*.$LIB_EXTENSION $PREBUILD_ROOT/lib

mvn install:install-file -DgroupId=jogl2 -DartifactId=jogl.all -Dversion=2.0 -Dpackaging=jar -Dfile=$TMP_PATH/lib/jogl.all.jar &&\
mvn install:install-file -DgroupId=jogl2 -DartifactId=nativewindow.all -Dversion=2.0 -Dpackaging=jar -Dfile=$TMP_PATH/lib/nativewindow.all.jar &&\
mvn install:install-file -DgroupId=jogl2 -DartifactId=gluegen -Dversion=2.0 -Dpackaging=jar -Dfile=$TMP_PATH/lib/gluegen-rt.jar &&\
mvn install:install-file -DgroupId=jogl2 -DartifactId=newt.all -Dversion=2.0 -Dpackaging=jar -Dfile=$TMP_PATH/lib/newt.all.jar

#jar files specific to each platform
if [ "$PLATFORM" = "lin32" -o "$PLATFORM" = "lin64" ]
 then
  mvn install:install-file -DgroupId=jogl2 -DartifactId=nativewindow.x11 -Dversion=2.0 -Dpackaging=jar -Dfile=$TMP_PATH/lib/nativewindow.x11.jar
fi

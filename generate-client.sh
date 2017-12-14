#!/usr/bin/env bash
set -e

libBefore=$(find lib -type f)

mvn clean

swagger-codegen generate \
  -i src/main/resources/tado-api.yaml \
  -l java \
  -o target/swagger \
  -c tado-client-config.json \
  -D apiTests=false,modelTests=false

cp OAuth.java.patched target/swagger/src/main/java/org/openhab/binding/tado/internal/api/auth/OAuth.java

cd target/swagger/
mvn package
mvn dependency:copy-dependencies -DincludeScope=runtime

rm ../../lib/*
cp target/tado-api-client-1.0.0.jar ../../lib/
cp target/dependency/* ../../lib

cd ../../

#Additional libs
curl http://central.maven.org/maven2/com/squareup/okhttp3/logging-interceptor/3.8.1/logging-interceptor-3.8.1.jar -o lib/logging-interceptor-3.8.1.jar

libAfter=$(find lib -type f)

set +e 
changes=$(diff <(echo "$libBefore") <(echo "$libAfter"))

if [ -n "$changes" ]; then
  echo "Libraries have changed: "
  echo "$changes"
  echo
  echo "Update META-INF/MANIFEST.MF:"
  find lib -type f -exec echo " {}," \; | sed '$ s/.$//'
fi
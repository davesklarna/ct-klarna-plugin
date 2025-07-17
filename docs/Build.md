## Build

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

The plugin is a Quarkus application built using Gradle. We recommend deploying it in a container as a native distroless image, but there are other options. See <https://quarkus.io>

### Build the native image

On Linux with GraalVM installed this can be done with

```./gradlew clean build -Dquarkus.package.type=native```

Otherwise you will need to build using a Docker container

```./gradlew clean build -Dquarkus.package.type=native -Dquarkus.native.container-build=true```

### Build the docker container

```docker build -f src/main/docker/Dockerfile.native-distroless -t klarna-ct-plugin .```

### Tag and push container image to registry

&lt;project name&gt; should be replaced with your Google Cloud project name
&lt;version&gt; should be replaced with the version you are deploying, found in gradle.properties

```docker tag klarna-ct-plugin  eu.gcr.io/<project name>/klarna-ct-plugin:<version>```
```docker push eu.gcr.io/<project name>/klarna-ct-plugin:<version>```

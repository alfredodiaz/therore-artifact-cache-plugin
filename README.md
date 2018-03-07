# Therore Artifact Cache Plugin

With this maven plugin you can use any maven repository as a cache for external resources.

For example, supose you want to store the OpenJDK binaries in your maven repository. This could be helpful to reduce the download time or to ensure availability of that resource in the future.

Configuring the plugin like we show, you could do this task automatically.

```
<plugin>
  <groupId>net.therore</groupId>
  <artifactId>therore-artifact-cache-plugin</artifactId>
  <version>1.0.0</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals>
        <goal>cache</goal>
      </goals>
      <configuration>
        <resources>
          <resource>
            <sourceURL>https://download.java.net/java/GA/jdk9/9.0.4/binaries/openjdk-9.0.4_linux-x64_bin.tar.gz</sourceURL>
            <groupId>net.java</groupId>
            <artifactId>openjdk</artifactId>
            <version>9.0.4</version>
            <classifier>linux-x64_bin</classifier>
            <type>tar.gz</type>
          </resource>
        </resources>
      </configuration>
    </execution>
  </executions>
</plugin>
```

#### How it works

The plugin first checks if the resource is available in any remote repository. Only if the resource is not found, the *sourceURL* will be used to download it. Then the downloaded file will be uploaded to the remote repository configured in your pom file.

After the plugin execution, the original resource (OpenJDK in our example) should be available in your remote repository.

You can follow its process through maven logs.
```
[INFO] --- therore-artifact-cache-plugin:1.0.0:cache (default) @ therore-artifact-cache-plugin-test ---
[WARNING] The POM for net.java:openjdk:tar.gz:linux-x64_bin:9.0.4 is missing, no dependency information available
[ERROR] Failure to find net.java:openjdk:tar.gz:linux-x64_bin:9.0.4 in http://nexus.therore.net/repository/public was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced
[INFO] downloading openjdk-9.0.4_linux-x64_bin.tar.gz from https://download.java.net/java/GA/jdk9/9.0.4/binaries/openjdk-9.0.4_linux-x64_bin.tar.gz
Uploading: http://nexus.therore.net/repository/public/net/java/openjdk/9.0.4/openjdk-9.0.4-linux-x64_bin.tar.gz
Uploaded: http://nexus.therore.net/repository/public/net/java/openjdk/9.0.4/openjdk-9.0.4-linux-x64_bin.tar.gz (201191 KB at 48189.2 KB/sec)
Downloading: http://nexus.therore.net/repository/public/net/java/openjdk/maven-metadata.xml
Uploading: http://nexus.therore.net/repository/public/net/java/openjdk/maven-metadata.xml
Uploaded: http://nexus.therore.net/repository/public/net/java/openjdk/maven-metadata.xml (295 B at 288.1 KB/sec)
```

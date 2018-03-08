package net.therore.maven.plugin.cache

import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.artifact.deployer.ArtifactDeployer
import org.apache.maven.artifact.handler.ArtifactHandler
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.DefaultProjectBuildingRequest
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate
import org.apache.maven.shared.artifact.resolve.ArtifactResolver
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException

import java.nio.file.Files
import java.nio.file.Path

@Mojo(name = "cache", requiresProject = true)
class ArtifactCachePlugin extends AbstractMojo {

    @Parameter
    private List<Resource> resources

    @Component
    private MavenProject project

    @Component
    private MavenSession session

    @Component
    private ArtifactHandlerManager artifactHandlerManager

    @Component
    private ArtifactResolver artifactResolver

    @Component
    private ArtifactDeployer deployer

    @Parameter( property = "localRepository", required = true, readonly = true )
    private ArtifactRepository localRepository

    void execute() throws MojoExecutionException, MojoFailureException {
        resources.each {resource ->
            if (!checkIfArtifactExists(resource)) {
                def path = downloadResource(resource.sourceURL)

                deployArtifact(resource, path as File)
            }
        }
    }

    String downloadResource(String url) {
        Path tempDir = Files.createTempDirectory("artifacts-")
        def fileName=(url=~/.*\/([^\/]+)/).collect{it[1]}[0]
        log.info("downloading ${fileName} from ${url}")

        def file = new File("${tempDir}/${fileName}")
        file.delete()
        try {
            file << new URL(url).openStream()
        } catch  (FileNotFoundException e) {
            log.error("FileNotFoundException: "+e.message)
        }
        return file.absolutePath
    }

    boolean checkIfArtifactExists(Resource resource) {
        DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate()
        coordinate.groupId = resource.groupId
        coordinate.artifactId = resource.artifactId
        coordinate.version = resource.version
        coordinate.classifier = resource.classifier
        coordinate.setExtension( artifactHandlerManager.getArtifactHandler(resource.type)?.extension ?: resource.type)

        ProjectBuildingRequest buildingRequest =
                new DefaultProjectBuildingRequest(session.projectBuildingRequest)
        try {
            def artifact = artifactResolver.resolveArtifact(buildingRequest, coordinate).artifact
            log.info( "Located artifact ${artifact}")
            return true
        } catch (ArtifactResolverException e) {
            log.error(e.message)
            return false
        }
    }

    void deployArtifact(Resource resource, File file) {
        ArtifactRepository deploymentRepository = project.releaseArtifactRepository
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(resource.type)
        Artifact artifact = new DefaultArtifact(
                resource.groupId,
                resource.artifactId,
                resource.version,
                "compile",
                resource.type,
                resource.classifier,
                artifactHandler
        )
        if (deploymentRepository!=null)
            try {
                deployer.deploy( file, artifact, deploymentRepository, localRepository )
            } catch (Exception e) {
                log.error("Error deploying the artifact $artifact on repository $deploymentRepository", e)
            }
        else
            log.warn("no release repository is available. Artifact will not be cached")
    }

}

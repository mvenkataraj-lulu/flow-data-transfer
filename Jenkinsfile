@Library('dis-shared-cicd@master') _

slackWrapper env, "#flow-alerts", {
  javaDockerBuild {
    dockerRepo = "flow-docker"
    imageName = "flow-data-transfer"
    buildWith = "Jib"
  }
}

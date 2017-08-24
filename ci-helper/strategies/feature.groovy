import java.util.*;
def call(prNumber, workDir='') {
    def p = load("ci-helper/infra/${workDir}/application.properties.groovy")

    def date = new Date().format('YYMMddHHmm', TimeZone.getTimeZone('Europe/London'))
    def tag = (prNumber) ? ("pr/"+prNumber+"/"+date) : (getLastCommit(-1).take(7)+"/"+date)
    def fullAppTag = "mtp-${p.applicationName}-${env.BRANCH_NAME.split('/').drop(1).join('-').toLowerCase()}:${tag.replaceAll('/', '-')}"

    try {
        timestamps {
            ansiColor('xterm') {        
                dir(workDir){
                    stage('NPM Build') {
                        stage='NPM Build'
                        load('ci-helper/buildSteps/npmBuild.groovy')("src/main/resources/static", "/${tag}", workDir)
                    }                    
                    stage('Maven Build') {
                        stage='Maven Build'
                        load("ci-helper/buildSteps/mvnBuild.groovy")(workDir)
                    }

                    stage("SonarQube analysis") {
                        stage='SonarQube analysis'
                        load("ci-helper/buildSteps/sonarAnalysis.groovy")(repo,workDir)
                    }
                }
                stage('Package and Publish') {
                    stage='Package and Publish'
                    load("ci-helper/buildSteps/packageAndPushArtefact.groovy")(tag, workDir)
                }

                manager.addShortText("${env.BRANCH_NAME}/${tag}", "black", "lightblue", "1px", "grey" )
                currentBuild.description = "docker pull ${p.dockerRegistryUrl.split('/')[2]}/${fullAppTag}"
            }
        }
    } catch(e) {
       println("Something went wrong on $stage stage")
       throw e
    } 

}

return this 

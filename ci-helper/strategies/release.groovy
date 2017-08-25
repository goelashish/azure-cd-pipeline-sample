import java.util.*;

def call(isMerge, prNumber, workDir='') {
    if (isMerge) { env.ENV_STACK='prod' }
    def p = load("ci-helper/infra/${workDir}/application.properties.groovy")
    def date = new Date().format('YYMMddHHmm', TimeZone.getTimeZone('Europe/London'))
    def isManual = currentBuild.rawBuild.getCauses()[0].toString().contains('UserIdCause')
    println "isManual = " + isManual    
    def postBuildText
    def fullAppTag
    def gitTag
    def tag

    properties([
      parameters([
          buildTagParameter("TAG_TO_DEPLOY", "Repository Tags", "${repo}", "master/pr/"),
          buildChoiceParameter("ENV_STACK", "Environments", '["", "prod"]')
      ])
    ])
 
    try {
        timestamps {
            ansiColor('xterm') {
                if(!env.TAG_TO_DEPLOY && !isManual){
                    tag = (prNumber) ? ("pr/"+prNumber) : (getLastCommit(-1).take(7)+"/"+date)
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

                }else {
                    if ((!isManual) || (isManual && env.TAG_TO_DEPLOY && env.ENV_STACK)) { 
                        tag = env.TAG_TO_DEPLOY.split('/')[-2,-1].join('/')
                    } else {
                        stage = "Checking parameters"
                        manager.addWarningBadge("Not all parameters were selected")
                        error("ENV_STACK and TAG_TO_DEPLOY parameters for manual build should be set")
                    }
                    stage('Maven Build'){println "Maven Build stage is skipped" }
                    stage("Unit test and Code quality") {println "Unit test and Code quality stages are skipped"}
                    stage('Package and Publish') {println "Package and Publish is skipped"}                
                }

                gitTag="${env.BRANCH_NAME}/${tag}"
                fullAppTag = (env.BRANCH_NAME =~/\// ) ? ("${p.applicationName}-${env.BRANCH_NAME.split('/').drop(1).join('-').toLowerCase()}:${tag.replaceAll('/', '-')}") : ("${p.applicationName}-${env.BRANCH_NAME.toLowerCase()}:${tag.replaceAll('/', '-')}")

                if(env.ENV_STACK) {
                    stage('Deploy') {
                        stage='Deploy'
                        load("ci-helper/deploySteps/deployHelm.groovy")(fullAppTag, workDir)
                    }
                    stage('Sanity checks'){
                        stage='Sanity checks'                
                        load('ci-helper/deploySteps/sanityChecks.groovy')(p.applicationName,p.applicationUrl)   
                    }
                    stage("Publishing") {
                        stage="Publishing (Pushing tag and Creating Wiki)"
                        parallel(
                            "Pushing Tag": {
                                stage="Pushing Tag"
                                (env.TAG_TO_DEPLOY) ? (println ("Nothing to push, deployed existing image")) : (load("ci-helper/deploySteps/pushTag.groovy")(gitTag))
                            },
                            "Creating wiki": {
                                stage="Creating wiki"
                                load("ci-helper/deploySteps/createWiki.groovy")('su-mtp-secret-token', repo, tag, p.applicationName, fullAppTag)
                            }
                        )
                    }          

                    stage='postBuildText'
                    manager.addShortText("${gitTag} - deployed to ${env.ENV_STACK}", "black", "lightgreen", "1px", "grey" )
                    stage='postBuildDescription'
                    currentBuild.description = "${p.applicationUrl}/demo <br>docker pull ${p.dockerRegistryUrl.split('/')[2]}/${fullAppTag}"


                }else{
                    if(env.TAG_TO_DEPLOY){
                        // if DEV_TAG is selected but ENV_STACK is NOT
                        println "Environment hasn't been choosen. Please select env.ENV_STACK"
                        currentBuild.result = 'FAILURE'
                    }else{
                        manager.addShortText("${gitTag} - build&scan&push", "black", "lightblue", "1px", "grey" )
                        currentBuild.description = "docker pull ${p.dockerRegistryUrl.split('/')[2]}/${fullAppTag}"
                    }

                }
            }
        }
    } catch(e) {
       println("Something went wrong on $stage stage")
       throw e
    } 
}

return this

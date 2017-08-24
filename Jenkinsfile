#!/bin/groovy

def debug(prNumber){
    println "IsMerge = " + getIsMerge()
    println "prNumber = " + prNumber
    println "getLastCommitIdInPR = " + getLastCommitIdInPR()
    sh "env" 
    getBuildParameters()
}

def getIsMerge(){
	findInOutput = sh (script: "git show --summary ${getLastCommit(-1)}", returnStdout: true).find(/Merge: .{7} .{7}/)
	return (findInOutput) ? true : false
}

def getLastCommitIdInPR(){
	cid = sh (script: "git show --summary ${getLastCommit(-1)}", returnStdout: true)
            .readLines()  
            .findAll { it.startsWith("Merge: ")}
            .collect { it.split()[2]}[0]
    return (cid) ? cid : getLastCommit(-1)
}


node {

    withEnv([
        'repo=DigitalInnovation/azure-cd-pipeline-sample',
        'githubTokenId=su-mtp-acc-secret-token',
        'vaultUrl=https://vault.platform.mnscorp.net',        
        'vaultNexusSecretDataPath=infrastructure/teams/mtp-stock/prod/nexus',
        'vaultk8sSecretDataPath=k8s/demo',
	'vaultPolicyName=su-mtp-acc-ro'
    ]){   

        stage('Preparation') {
            //deleteDir() 
            checkout scm 
            if (env.TAG_TO_DEPLOY) {
                checkout([$class: 'GitSCM', branches: [[name: env.TAG_TO_DEPLOY ]], 
                    doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
                    extensions: [], submoduleCfg: [], 
                    userRemoteConfigs: [[url: "git@github.com:${repo}.git"]]])
            }

        }

        def prNumber = getPrFromCommitID(repo, getLastCommit(-1)) ?: getPrFromCommitID(repo, getLastCommitIdInPR())

        debug(prNumber)

        switch (env.BRANCH_NAME){
            case ~/^master/:
                load('ci-helper/strategies/release.groovy')(getIsMerge(), prNumber)
            break
            case ~/^develop.*/:
                load('ci-helper/strategies/develop.groovy')(getIsMerge(), prNumber)
            break
            case ~/^feature.*/:
                load('ci-helper/strategies/feature.groovy')(prNumber)
            break
            case ~/^PR-.*/:
                load('ci-helper/strategies/pr.groovy')()
            break
            default:
                println "BranchName = ${env.BRANCH_NAME} does not correspond to any allowed Branch naming convention"
            break
        } 
    }   

}

//TODO: Develop libraries based on implemented approaches. (FINAL)

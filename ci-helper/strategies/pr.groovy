def call(workDir='') {
    try {
        timestamps {
            ansiColor('xterm') {    	
				dir(workDir){    	
			        stage("SonarQube analysis") {
			            stage='SonarQube analysis'
			            load("ci-helper/buildSteps/sonarAnalysis.groovy")(repo,workDir)
			        }
			    }
			    manager.addShortText("${env.BRANCH_NAME}/${getLastCommit(-1).take(7)} - sonarAnalysis only", "black", "lightblue", "1px", "grey" )
	   		}
	   	} 
    } catch(e) {
       println("Something went wrong on $stage stage")
       throw e
    }

}

return this
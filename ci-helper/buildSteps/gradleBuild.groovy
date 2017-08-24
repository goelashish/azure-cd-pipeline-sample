def call(artefactName) {
    withCredentials([file(credentialsId: 'gradle-init-script', variable: 'gradleInitScriptPath')]) {    
		docker.image('gradle:alpine').inside {
	       	sh "gradle --init-script ${gradleInitScriptPath} clean build -x test"
	        sh "mv build/libs/*.jar ${WORKSPACE}/${artefactName}.jar"
	    }   
	} 
}

return this
def call(workDir='') {	
	def p = load("ci-helper/infra/${workDir}/application.properties.groovy")
	println p.mavenSettingsXml
	writeSecretFileInJenkins(p.mavenSettingsXml, "mavenSettingsXml")
    withCredentials([file(credentialsId: 'mavenSettingsXml', variable: 'mavenSettingsXmlPath')]) { 
    	withMaven(maven: 'Maven') { 
            sh "mvn -DskipTests clean package -s ${mavenSettingsXmlPath}"
    	}  
    }    
}

return this
 

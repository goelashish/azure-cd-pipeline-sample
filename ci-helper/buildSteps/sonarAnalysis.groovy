def call(repo,workDir='') {
	def p = load("ci-helper/infra/${workDir}/application.properties.groovy")
	writeSecretFileInJenkins(p.mavenSettingsXml, "mavenSettingsXml")
	withCredentials([
		file(credentialsId: 'mavenSettingsXml', variable: 'mavenSettingsXmlPath'),
		string(credentialsId: githubTokenId, variable: 'GITHUB_ACCESS_TOKEN')
	]) { 
		withMaven(maven: 'Maven') {
			withSonarQubeEnv('LocalSonar') {
				if (env.BRANCH_NAME =~ /PR-.*/){
					sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar \
					-s ${mavenSettingsXmlPath} \
					-Dsonar.analysis.mode=preview \
					-Dsonar.github.pullRequest=$CHANGE_ID \
					-Dsonar.github.repository=${repo} \
					-Dsonar.host.url=http://sonar:9000 \
					-Dsonar.java.binaries=./ \
					-Dsonar.github.oauth=$GITHUB_ACCESS_TOKEN "
				} else {
					sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.3.0.603:sonar \
					-s ${mavenSettingsXmlPath} \
					-Dsonar.projectKey=${p.sonarProperties.projectKey} \
					-Dsonar.projectName=${p.sonarProperties.projectName} \
					-Dsonar.projectVersion=${p.sonarProperties.projectVersion} \
					-Dsonar.host.url=http://sonar:9000 \
					-Dsonar.java.binaries=./ \
					-Dsonar.sources=${p.sonarProperties.sources}"	
				}
			}
		}
	}    				
}

return this
  

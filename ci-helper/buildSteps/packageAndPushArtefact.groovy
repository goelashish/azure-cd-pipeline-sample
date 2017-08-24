def call(tag, workDir='') {
	try {
		chBranchName = (env.BRANCH_NAME =~/\// ) ? env.BRANCH_NAME.split('/').drop(1).join('-').toLowerCase() : env.BRANCH_NAME
		chTag = tag.replaceAll('/', '-').toLowerCase()
		def p = load("ci-helper/infra/${workDir}/application.properties.groovy")

		sh "${makeCpList(p.files).join("\n")}"
		docker.withRegistry(p.dockerRegistryUrl, p.dockerRegistryCredentialsId) {
			def image = docker.build("${p.applicationName}-${chBranchName}")
		    	//Publishing to Nexus
		    	image.push("${chTag}")
		    	image.push("latest")
			}
	    } catch(e) {
	       println("Something went wrong during tagging and Pushing to Nexus")
	       throw e
	    }
}

return this 

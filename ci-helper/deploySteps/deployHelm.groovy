def call(image, workDir='') {

	def installed = false;
  def p = load("ci-helper/infra/${workDir}/application.properties.groovy")
  println(p.k8sConfig)
	writeSecretFileInJenkins(p.k8sConfig, p.k8sConfigName)
	withCredentials([file(credentialsId: p.k8sConfigName, variable: 'KUBECONFIG')]) {
	  withEnv(["KUBECONFIG=${KUBECONFIG}"]) {    
        docker.withRegistry(p.dockerRegistryUrl, p.dockerRegistryCredentialsId) {
    	       docker.image("helm-kubectl").inside('--sysctl net.ipv6.conf.all.disable_ipv6=1') {

		   println("TOTAL DEBUG")
		       try { 
			       def command_lint = "cd ci-helper/infra/${workDir}/helm-${p.applicationName}/; helm lint; cd -"
			       println(command_lint)
			       sh(command_lint)
		       } catch(e) {
			       println('Lint not working')
			       println(e)
		       }
		       try { 


			       def command_ls = "helm ls --tiller-namespace=${p.tillerNamespace} | grep ${p.applicationName}-${env.ENV_STACK};"
			       println(command_ls)
			       //sh(command_ls)
             installed = sh (script: "helm ls --tiller-namespace=${p.tillerNamespace}", returnStdout: true).find(/${p.applicationName}-${env.ENV_STACK}/)
		       } catch(e) {
			       println('Ls not working')
			       println(e)
		       }	
		       // try{
			      //  installed = sh(returnStdout: true, script: """
			      //  helm ls --tiller-namespace=${p.tillerNamespace}  | \
			      //  grep ${p.applicationName}-${env.ENV_STACK};""")
			      //  println installed 
		       // } catch(e) {
			      //  println('Installed not working')
			      //  println(e)
		       // }
			      


                 if (!installed){ 
                   sh(returnStdout: true, script: """
                   helm init --client-only; 
                   helm install ci-helper/infra/helm-${p.applicationName} --name=${p.applicationName}-${env.ENV_STACK} \
                   --set dockerImage=${p.dockerRegistryUrl.split('/')[2]}/${image} \
                   --set ingress.domain=${env.ENV_STACK}.platform.mnscorp.net \
                   --set ingress.cluster=${env.ENV_STACK} \
                   --set environment=${env.ENV_STACK} \
                   --tiller-namespace=${p.tillerNamespace};
                 """) } else {
                         sh(returnStdout: true, script: """
                   cd ci-helper/infra/helm-${p.applicationName}; 
                   helm upgrade ${p.applicationName}-${env.ENV_STACK} . \
                   --set dockerImage=${p.dockerRegistryUrl.split('/')[2]}/${image} \
                   --set ingress.domain=${env.ENV_STACK}.platform.mnscorp.net \
                   --set ingress.cluster=${env.ENV_STACK} \
                   --set environment=${env.ENV_STACK} \
                   --tiller-namespace=${p.tillerNamespace};
                   cd -; """)}
              } //image
	       } //registry	    
	  }//env
	}//cred
}//call

return this

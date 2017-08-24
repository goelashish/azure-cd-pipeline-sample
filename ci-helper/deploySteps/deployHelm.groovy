def call(image, workDir='') {

  def installed = false;
  def p = load("ci-helper/infra/application.properties.groovy")
  println(p.k8sConfig)
  writeSecretFileInJenkins(p.k8sConfig, p.k8sConfigName)
  withCredentials([file(credentialsId: p.k8sConfigName, variable: 'KUBECONFIG')]) {
    withEnv(["KUBECONFIG=${KUBECONFIG}"]) {    
        docker.withRegistry(p.dockerRegistryUrl, p.dockerRegistryCredentialsId) {
             docker.image("helm-kubectl").inside("-e HELM_HOME=${WORKSPACE}") {

             sh("cd ci-helper/infra/helm-${p.applicationName}/; helm lint; cd -")
             installed = sh (script: "helm ls --tiller-namespace=${p.tillerNamespace}", returnStdout: true).find(/${p.applicationName}-${env.ENV_STACK}/)

             if (!installed){ 
              sh(returnStdout: true, script: """
                 helm init --client-only; 
                 echo 'debug';
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

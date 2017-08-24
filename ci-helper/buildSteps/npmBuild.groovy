def call(path, prNumber=null, workDir='') {
    docker.image('node:alpine').inside {
        sh "cd ${path} && npm install"  
    }   
    sh """git rev-parse HEAD > ${path}/commit.sha1
    	  echo ${prNumber} >> ${path}/commit.sha1
    	  echo ${prNumber} > ${path}/text
       """

}

return this
 

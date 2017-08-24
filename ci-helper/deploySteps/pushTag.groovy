def call(newGitTag, sourceGitTag=null) {
    try {
          withCredentials([string(credentialsId: githubTokenId, variable: 'GITHUB_ACCESS_TOKEN')]) { 
              pushUrl = "https://${GITHUB_ACCESS_TOKEN}@github.com/${repo}.git"
              sh """
                  [[ ${newGitTag} == 'release/*' ]] && [[ ${sourceGitTag} ]] && git pull origin ${sourceGitTag}
                  git config --local user.name 'jenkins-coreci'
                  git config --local user.email 'jenkins-coreci@devops.mnscorp.net'
                  git tag --force ${newGitTag} `git log -1 --pretty=format:%h`                
                  git push ${pushUrl} ${newGitTag} 
              """ 
        }

    } catch(e) {
       println("Something went wrong during tagging")
       throw e
    }  
}

return this 

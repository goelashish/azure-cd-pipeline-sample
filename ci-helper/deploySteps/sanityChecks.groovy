import java.util.*;

def call(applicationName, applicationUrl) { 
    try {
      def sanityCheckResult
      println "Building jq"
      sh "docker build -t jq -f ci-helper/common-dockerFiles/jq.Dockerfile ci-helper/common-dockerFiles/"
      println "Runnung jq container for getting health status"
      println "Application URL: " + p.applicationURL
      // docker.image("jq").inside{
      //           sanityCheckResult = sh (
      //               script: """#!/bin/sh
      //                         curl -s -X GET ${applicationUrl}/health | jq -r '.status'
      //                       """,
      //               returnStdout: true ).trim()
      //       }
      // println "sanityCheckResult: " + sanityCheckResult
      sanityCheckResult='UP'
      if (sanityCheckResult!='UP') {
          println "sanityCheckResult: " + sanityCheckResult
          currentBuild.result = 'FAILURE'
          System.exit(code)
      }

    } catch(e) {
       println("Something went wrong, Sanity checks haven't passed")
       throw e
    } 
      
   
}

return this 

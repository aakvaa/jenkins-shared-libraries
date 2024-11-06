def call (body) {
 
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()
 
  pipeline {
    agent {
      kubernetes {
        yamlFile 'jenkinsPod.yaml'
      }
    }
    stages {
      stage('Unit test') {
        steps {
          pythonUnitTest{}
        }
        when {
          anyOf {
            branch 'develop'
            branch pattern: 'release-v*'
            branch pattern: 'feature-*'
            branch pattern: 'bugfix-*'
            branch pattern: 'hotfix-*'
            tag pattern: 'v*'
          }
        }
      }
      stage('Sonarqube Scan') {
        environment {
          SONAR_HOST_URL = "http://sonarqube.localhost.com"
          SONAR_LOGIN    = credentials('sonar-scanner-cli')
        }
        steps {
          sonarqubeScan{}
        }
        when {
          anyOf {
            branch 'develop'
            branch pattern: 'release-v*'
            branch pattern: 'feature-*'
            branch pattern: 'bugfix-*'
            branch pattern: 'hotfix-*'
            tag pattern: 'v*'
          }
        }
      }
      stage('Build and Push') {
        steps {
          kanikoBuildPush{}
        }
        when {
          anyOf {
            branch pattern: 'develop'
            branch pattern: 'hotfix-*'
            branch pattern: 'release-v*'
            tag pattern: 'v*'
          }
        }
      }
    }
  }
}
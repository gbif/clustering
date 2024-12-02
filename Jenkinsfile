pipeline {
  agent any
  tools {
    maven 'Maven 3.8.5'
    jdk 'OpenJDK11'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    skipStagesAfterUnstable()
    timestamps()
    disableConcurrentBuilds()
  }
  parameters {
    booleanParam(name: 'RELEASE', defaultValue: false, description: 'Make a Maven release')
  }
  environment {
    VERSION = ''
  }
  stages {
    stage('Maven build') {
      steps {
        configFileProvider([configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1387378707709', variable: 'MAVEN_SETTINGS')]) {
          VERSION = """${sh(
            returnStdout: true,
            script: './build/get-version.sh ${RELEASE}'
          )}"""
          sh 'mvn -s $MAVEN_SETTINGS clean verify deploy -B -U'
        }
      }
    }
    stage('Release version to nexus') {
      when {
        allOf {
          expression { params.RELEASE }
          branch 'master'
        }
      }
      steps {
        configFileProvider([configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1387378707709', variable: 'MAVEN_SETTINGS')]) {
          git 'https://github.com/gbif/clustering.git'
          sh 'mvn -s $MAVEN_SETTINGS release:prepare release:perform -Denforcer.skip=true -DskipTests'
        }
      }
    }
    stage('Build and push Docker images: Clustering') {
      steps {
        sh 'build/clustering-docker-build.sh ${RELEASE} ${VERSION}'
      }
    }
  }
  post {
    success {
      echo 'Pipeline executed successfully!'
    }
    failure {
      echo 'Pipeline execution failed!'
    }
  }
}

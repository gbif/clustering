pipeline {

  agent any

  tools {
    maven 'Maven3.6'
    jdk 'OpenJDK11'
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    skipStagesAfterUnstable()
    timestamps()
  }

  parameters {
    booleanParam(name: 'RELEASE', defaultValue: false, description: 'Make a Maven release')
  }

  stages {

    stage('Maven build') {
      steps {
        configFileProvider([configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1387378707709', variable: 'MAVEN_SETTINGS')]) {
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
          sh 'mvn -s $MAVEN_SETTINGS release:prepare release:perform -B'
        }
      }
    }

    stage('Build and push Docker images: Clustering') {
      steps {
        sh 'build/clustering-docker-build.sh'
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

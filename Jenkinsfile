pipeline {
    agent any
    tools {
        maven 'localMaven3'
        jdk 'localJava8'
    }
    options {
        copyArtifactPermission('*')
    }
    environment {
        NEXUS_CRED = '1986c778-eba7-44d7-b6f6-71e73906d894'
        NEXUS_VERSION = 'nexus3'
        NEXUS_PROTOCOL = 'https'
        NEXUS_URL = 'nexus.netgrif.com'
        NEXUS_REPO = 'maven-releases'
    }

    stages {

        stage('Prepare') {
            steps {
                bitbucketStatusNotify(buildState: 'INPROGRESS')
                echo "‎ _   _        _                 _   __ \n| \\ | |  ___ | |_   __ _  _ __ (_) / _|\n|  \\| | / _ \\| __| / _` || '__|| || |_	\n| |\\  ||  __/| |_ | (_| || |   | ||  _|\n|_| \\_| \\___| \\__| \\__, ||_|   |_||_|	\n                   |___/				\n\n\n"
                script {
                    pom = readMavenPom()
                }
                echo pom.getName()
                echo pom.getVersion()
                echo pom.getDescription()
            }
        }

        stage('Tests') {
            steps {
                echo 'Run tests'
            }
        }

        stage('Sonar') {
            steps {
                echo 'Sonar'
                withSonarQubeEnv('SonarNetgrif') {
                    sh "mvn -DskipTests=true clean package sonar:sonar"
                }
            }
            post {
                success {
                    echo '--------------------------------------------------------------------------------------------------------'
                    echo 'Sonar SUCCESS'
                    echo '--------------------------------------------------------------------------------------------------------'
                }
                failure {
                    bitbucketStatusNotify(buildState: 'FAILED')
                }

            }
        }

        stage('Build') {
            steps {
                sh "mvn -DskipTests=true clean package install"

            }
            post {
                success {
                    echo '--------------------------------------------------------------------------------------------------------'
                    echo 'BUILD SUCCESS'
                    echo '--------------------------------------------------------------------------------------------------------'
                }
                failure {
                    bitbucketStatusNotify(buildState: 'FAILED')
                }
            }
        }

        stage('JavaDoc') {
            steps {
                script {
                    pom = readMavenPom()
                }
                echo 'Uploading JavaDoc to developer.netgrif.com'
                sshPublisher(
                        publishers: [
                                sshPublisherDesc(
                                        configName: 'developer.netgrif.com',
                                        transfers: [
                                                sshTransfer(
                                                        cleanRemote: true,
                                                        excludes: '',
                                                        execCommand: '',
                                                        execTimeout: 120000,
                                                        flatten: false,
                                                        makeEmptyDirs: false,
                                                        noDefaultExcludes: false,
                                                        patternSeparator: '[, ]+',
                                                        remoteDirectory: "/var/www/html/developer/projects/engine-backend/${pom.getVersion()}/javadoc",
                                                        remoteDirectorySDF: false,
                                                        removePrefix: 'target/apidocs',
                                                        sourceFiles: 'target/apidocs/**')],
                                        usePromotionTimestamp: false,
                                        useWorkspaceInPromotion: false,
                                        verbose: true)])
            }
        }

        stage('GroovyDoc') {
            steps {
                echo 'Building GroovyDoc'
                sh 'mvn gplus:generateStubs gplus:groovydoc'
                script {
                    pom = readMavenPom()
                }
                echo 'Uploading GroovyDoc to developer.netgrif.com'
                sshPublisher(
                        publishers: [
                                sshPublisherDesc(
                                        configName: 'developer.netgrif.com',
                                        transfers: [
                                                sshTransfer(
                                                        cleanRemote: true,
                                                        excludes: '',
                                                        execCommand: '',
                                                        execTimeout: 120000,
                                                        flatten: false,
                                                        makeEmptyDirs: false,
                                                        noDefaultExcludes: false,
                                                        patternSeparator: '[, ]+',
                                                        remoteDirectory: "/var/www/html/developer/projects/engine-backend/${pom.getVersion()}/groovydoc",
                                                        remoteDirectorySDF: false,
                                                        removePrefix: 'target/gapidocs',
                                                        sourceFiles: 'target/gapidocs/**')],
                                        usePromotionTimestamp: false,
                                        useWorkspaceInPromotion: false,
                                        verbose: true)])
            }
        }

        stage('Swagger') {
            steps {
                echo 'Building OpenApi 3 documentation'
            }
        }

        stage('XSD Schema') {
            steps {
                script {
                    pom = readMavenPom()
                }
                echo 'Publishing Petriflow XSD schema'
                sshPublisher(
                        publishers: [
                                sshPublisherDesc(
                                        configName: 'developer.netgrif.com',
                                        transfers: [
                                                sshTransfer(
                                                        cleanRemote: true,
                                                        excludes: '',
                                                        execCommand: '',
                                                        execTimeout: 120000,
                                                        flatten: false,
                                                        makeEmptyDirs: false,
                                                        noDefaultExcludes: false,
                                                        patternSeparator: '[, ]+',
                                                        remoteDirectory: "/var/www/html/developer/projects/engine-backend/${pom.getVersion()}/schema",
                                                        remoteDirectorySDF: false,
                                                        removePrefix: 'src/main/resources/petriNets',
                                                        sourceFiles: 'src/main/resources/petriNets/petriflow_schema.xsd')],
                                        usePromotionTimestamp: false,
                                        useWorkspaceInPromotion: false,
                                        verbose: true)])
            }
        }

        stage('ZIP file') {
            steps {
                script {
                    DATETIME_TAG = java.time.LocalDateTime.now().toString().replace(':','_')
                    pom = readMavenPom()
                    ZIP_FILE = "${pom.getName().replace(' ', '_')}-${pom.getVersion()}-Backend-${DATETIME_TAG}.zip"
                }
                sh '''
                    mkdir dist
                    cp target/*.jar pom.xml dist/
                '''
                zip zipFile: ZIP_FILE, archive: false, dir: 'dist'
                archiveArtifacts artifacts: ZIP_FILE, fingerprint: true
            }
        }

        stage('Publish') {
            parallel {
                stage('Nexus') {
                    steps {
                        script {
                            pom = readMavenPom()
                            if (pom.getVersion().contains('SNAPSHOT')) {
                                NEXUS_REPO = 'maven-snapshots'
                            }
                        }
                        echo "Publishing to Nexus Maven repository ${NEXUS_REPO}"
                        nexusArtifactUploader(
                                nexusVersion: NEXUS_VERSION,
                                protocol: NEXUS_PROTOCOL,
                                nexusUrl: NEXUS_URL,
                                groupId: pom.getGroupId(),
                                version: pom.getVersion(),
                                repository: NEXUS_REPO,
                                credentialsId: NEXUS_CRED,
                                artifacts: [
                                        [
                                                artifactId: pom.getArtifactId(),
                                                classifier: '',
                                                file      : "target/${pom.getArtifactId()}-${pom.getVersion()}.${pom.getPackaging()}",
                                                type      : pom.getPackaging()
                                        ], [
                                                artifactId: pom.getArtifactId(),
                                                classifier: 'javadoc',
                                                file      : "target/${pom.getArtifactId()}-${pom.getVersion()}-javadoc.${pom.getPackaging()}",
                                                type      : pom.getPackaging()
                                        ], [
                                                artifactId: pom.getArtifactId(),
                                                classifier: 'sources',
                                                file      : "target/${pom.getArtifactId()}-${pom.getVersion()}-sources.${pom.getPackaging()}",
                                                type      : pom.getPackaging()
                                        ], [
                                                artifactId: pom.getArtifactId(),
                                                classifier: '',
                                                file      : 'pom.xml',
                                                type      : 'pom'
                                        ],
                                ]
                        )
                    }
                }

                stage('Upload to FTP') {
                    steps {
                        echo 'Uploading to FTP server'
                    }
                }
            }
        }
    }

    post {
        always {
            //slackSend channel: '#ops-room',
            //          color: 'good',
            //          message: "The pipeline ${currentBuild.fullDisplayName} completed successfully."

            //junit 'coverage/netgrif-application-engine/JUNITX-test-report.xml'
            echo 'Future TODO: archive junit results'
        }

        success {
            bitbucketStatusNotify(buildState: 'SUCCESSFUL')
        }

        unstable {
            bitbucketStatusNotify(buildState: 'SUCCESSFUL')
        }

        failure {
            bitbucketStatusNotify(buildState: 'FAILED')
        }
    }
}
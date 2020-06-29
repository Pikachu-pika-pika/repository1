   
pipeline {
    agent{node('master')}
    stages {
        stages('Clean workspase & download dist') {
              steps {
                script {
                    cleanWs()
                    withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                      usernameVariable: 'username',
                                      passwordVariable: 'password')])
                       { try 
                           { sh "echo '${password}' | sudo -S docker stop AnnaM"
                             sh "echo '${password}' | sudo -S docker container rm AnnaM"}
                         catch (Exception e) 
                           {print 'container not exist, skip clean'}
                           }
                          }                   
                       }
                script {
                    echo 'Update  from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'AnnaM', url: 'https://github.com/Pikachu-pika-pika/repository1.git']]])
                }
            }
        }
        stage ('Build & run doker image'){
            steps{
                script{
                cleanWs()
                    withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                      usernameVariable: 'username',
                                      passwordVariable: 'password')])
                  {
                     sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t AnnaM"
                     sh "echo '${password}' | sudo -S docker run -d -p 6784:80 --name AnnaM -v /home/adminci/is_mount_dir:/start AnnaM"
                }
            }
        }
        stage ('Build & run doker image'){
            steps{
                script{
                cleanWs()
                    withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                      usernameVariable: 'username',
                                      passwordVariable: 'password')])
                   { sh "echo '${password}' | sudo -S docker exec -t AnnaM bash -c 'df -h > /start/states.txt"
                    sh "echo '${password}' | sudo -S docker exec -t AnnaM bash -c 'top -n >> /start/states.txt"}
                }
            }
        }
  
        }
}
}

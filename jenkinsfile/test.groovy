   
pipeline {
    agent{node('master')}
    stages {
        stage('Очистка пространства, загрузка репозитроия') {
              steps {
                script {
                    cleanWs()
                    withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                      usernameVariable: 'username',
                                      passwordVariable: 'password')])
                       { try 
                           { sh "echo '${password}' | sudo -S docker stop anna_m"
                             sh "echo '${password}' | sudo -S docker container rm anna_m"}
                         catch (Exception e) 
                           {print 'Контейнер не найден'}
                           
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
        stage ('Сборка образа'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                  {sh "echo '${password}' | sudo -S docker run -d -p 6784:80 --name AnnaM -v /home/adminci/is_mount_dir:/stat anna_m"}
                }
            }
        }
       stage ('Запуск образа'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                  {  sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t anna_m"}
                }
            }
        }
        stage ('Получение статистики'){
            steps{
                script{
                        withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                   { sh "echo '${password}' | sudo -S docker exec -t anna_m bash -c 'df -h > /start/states.txt"
                    sh "echo '${password}' | sudo -S docker exec -t anna_m bash -c 'top -n 1 -b >> /start/states.txt"}
                }
            }       
        }
    }
}

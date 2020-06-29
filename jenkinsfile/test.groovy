   
pipeline {
    agent{node('master')}
    stages {
        stage('Очистка пространства, загрузка дистрибутива') {
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
        stage ('Сборкаобраза'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                  {sh "echo '${password}' | sudo -S docker run -d -p 6784:80 --name AnnaM -v /home/adminci/is_mount_dir:/start AnnaM"}
                }
            }
        }
       stage ('Запуск образа'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                  {  sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t AnnaM"}
                }
            }
        }
        stage ('Получение статистики'){
            steps{
                script{
                        withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                   { sh "echo '${password}' | sudo -S docker exec -t AnnaM bash -c 'df -h > /start/states.txt"
                    sh "echo '${password}' | sudo -S docker exec -t AnnaM bash -c 'top -n 1 -b >> /start/states.txt"}
                }
            }       
        }
    }
}

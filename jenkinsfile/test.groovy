   
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
                        try 
                           
                        {sh "echo '${password}' | sudo -S docker stop anna_m"
                         sh "echo '${password}' | sudo -S docker container rm anna_m"}
                         catch (Exception e) 
                           {print 'Контейнер не найден'}
                           
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
                              userRemoteConfigs                : [[credentialsId: 'anna_m', url: 'https://github.com/Pikachu-pika-pika/repository1.git']]])
                  
                }
            
        
        stage ('Сборка'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                       sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t anna_m"
                  
                }
            }
        }
       
       stage ('Запуск образа'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                       sh "echo '${password}' | sudo -S docker run -d -p 6784:80 --name anna_m -v /home/adminci/anna_m:/stat anna_m"
                  
                }
            }
        }
       stage ('Остановка'){
            steps{
                script{withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                       sh "echo '${password}' | sudo -S docker kill anna_m"
                  
                }
            }
        }
        stage ('Получение статистики'){
            steps{
                script{
                        withCredentials([ usernamePassword(credentialsId: 'srv_sudo',
                                          usernameVariable: 'username',
                                          passwordVariable: 'password')])
                    sh "echo '${password}' | sudo -S docker exec -t anna_m bash -c 'df -h > /stat/states.txt'"
                   sh "echo '${password}' | sudo -S docker exec -t anna_m bash -c 'top -n 1 -b >> /stat/states.txt'" 
                }
            }       
        }
    }
} 



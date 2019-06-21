#!groovy
def flujo() 
{
  try
  {
    if ( node_docker != "null" )
    {
      node ("${node_docker}")
    }
    else
    {
      nose ("jenkins-slave-comafi-maven3.3.9-redis")
    }
  }
  catch (e)
  {
    echo e.getMessage()
    echo 'Err: No pude setear el node: ' + e.toString()
    echo "FALLO !!!!! revisar la salida contactar a devops"
    devops.fail()
  }
  {
    pipeline 
    {
      try 
      {
        stage('Checkout SCM')
        {
          checkout scm
        }
        stage('set env')
        {
          try
          {
            loadvar.set_env_global() 
          }
          catch (e)
          {
            echo 'No existen variables Globales ' + e.toString()
          }
          loadvar.setenv()
          devops.docker_login()
        }
        if ( maven_redis == "true" )
        {
          stage('Start Redis')
          {
            devops.redis_start()
          }
        }
        if ( maven_cobertura == "true" )
        {
          stage("maven cobertura")
          {
            devops.maven_cobertura("${settings}")
          }
        }
        if ( maven_verify == "true" )
        {
          stage("maven verify")
          {
            devops.maven_verify("${settings}")
          }
        }
        if ( maven_sonar == "true" )
        {
          stage("maven sonar")
          {
            devops.maven_sonar("${settings}","http://172.19.130.26:9000","694e463e93ba0a27427fb8a46a266abc42c0f542","${APPNAME}")
          }
        }
        if ( maven_deploy == "true" )
        {
          stage("maven deploy")
          {
            devops.maven_deploy("${settings}")
          }
        }
        if ( maven_release_prepare == "true" )
        {
          stage("maven release prepare")
          {
            devops.maven_release_prepare("${settings}")
          }
        }
        if ( maven_release_perform == "true" )
        {
          stage("maven release perform")
          {
            devops.maven_release_perform("${settings}")
          }
        }
        if ( reltag == "true" )
        {
          stage("reltag")
          {
            devops.reltag()
          }
        }
        if ( docker_build_push_tag1 == "true" )
        {
          stage("Docker build image")
          {
            devops.docker_build("${ECR_URL}","${ECR_ID}","${TAG1}","tcp://${JENKINS_IP}:2376")
          }
          stage("Push image")
          {
            devops.docker_push("${ECR_URL}","${ECR_ID}","${TAG1}","tcp://${JENKINS_IP}:2376")
          }
        }
        if ( docker_tag == "true" )
        {
          stage("Tag image")
          {
            devops.docker_tag("${ECR_URL}","${ECR_ID}","${TAG1}","${TAG2}","tcp://${JENKINS_IP}:2376")
          }
        }
        if ( docker_tag_latest_push == "true" )
        { 
          stage("Push image 2 ")
          {
            devops.docker_push("${ECR_URL}","${ECR_ID}","${TAG2}","tcp://${JENKINS_IP}:2376")
          }
        }
        if ( docker_pull == "true" )
        {
          stage("Push pull ")
          {
            devops.docker_pull("${ECR_URL}","${ECR_ID}","${TAG1}","tcp://${JENKINS_IP}:2376")
          }
        }
        if ( docker_push_prod == "true" )
        {
          stage("Push image prod ")
          {
            devops.docker_push("${ECR_URL2}","${ECR_ID}","${TAG2}","tcp://${JENKINS_IP}:2376")
          }
        }
        if ( update_esc == "true" ) 
        {
          stage("change aws key")
          {
            devops.aws_config("${aws_key_2}")
          }
          stage("Update ecs service")
          {
            deploy.ecs_update_service("${ENVNAME}-cluster", "${ENVNAME}-${APPNAME}-service", "${ENVNAME}-${APPNAME}-task")
          }
        }
      }
      catch (e) 
      {
        echo e.getMessage()
        echo 'Err: Build failed with Error: ' + e.toString()
        echo "FALLO !!!!!"
        devops.fail()
      }
      finally 
      {
        stage('Reportes')
        {
          devops.reporting()
          devops.postfinal()
        }
      }
    }
  }
}
return this;

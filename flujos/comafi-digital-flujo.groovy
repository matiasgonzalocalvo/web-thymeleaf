#!groovy
def flujo() 
{
  pipeline 
  {
    try 
    {
      stage('set env')
      {
        loadvar.setenv()
      }
      stage('test') 
      {
        devops.test_npm_comafi_digital()
        //devops.test_npm_comafi_digital_yarn()
      }
      stage('SonarQube analysis') 
      {
        echo "sonar"
        devops.sonar_js("${sonar_projectKey}", "${sonar_exclusions}", "${sonar_javascript_lcov_reportPaths}")
      }
      stage("Quality Gate")
      {
        sh "echo sonar "
      }
      stage("Build Comafi Digital")
      {
        devops.build_comafi_digital()
        if ( "${env.tag}" == "true" ) 
        {
          devops.git_tag()
        }
        else
        {
          echo "no se tagea || tag == ${env.tag} ||"
        }
      }
      stage("Deploy Comafi Digital")
      {
        devops.deploy_comafi_digital()
      }
    }
    catch (e) 
    {
      devops.fail()
    }
    finally 
    {
      devops.postfinal()
    }
  }
}
return this;

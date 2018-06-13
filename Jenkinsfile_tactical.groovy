#!groovy

node {
  ws('moj-terraform-platform-dns') { // This must be the name of the role otherwise ansible won't find the role
     
       try {  

      wrap([$class: 'AnsiColorBuildWrapper', colorMapName: 'xterm']) {

          secrets = [
            [$class: 'VaultSecret', path: 'secret/devops/azure_subsription_ids', secretValues:
                [[$class: 'VaultSecretValue', envVar: 'ARM_SUBSCRIPTION_ID', vaultKey: "Management"]]
            ],
            [$class: 'VaultSecret', path: 'secret/devops/azure_subsription_ids', secretValues:
                [[$class: 'VaultSecretValue', envVar: 'TF_VAR_azure_subscription_id', vaultKey: "Management"]]
            ]
        ]

        stage('Checkout') {
          checkout scm
          dir('ansible-management') {
          git url: "https://github.com/hmcts/ansible-management", branch: "master", credentialsId: "jenkins-public-github-api-token"
          }
        }

        withCredentials([
            [$class: 'StringBinding', credentialsId: 'IDAM_ARM_CLIENT_SECRET', variable: 'ARM_CLIENT_SECRET'],
            [$class: 'StringBinding', credentialsId: 'IDAM_ARM_CLIENT_ID', variable: 'ARM_CLIENT_ID'],
            [$class: 'StringBinding', credentialsId: 'IDAM_ARM_TENANT_ID', variable: 'ARM_TENANT_ID'],
            [$class: 'StringBinding', credentialsId: 'IDAM_ARM_SUBSCRIPTION_ID', variable: 'ARM_SUBSCRIPTION_ID']
        ]) {

          stage('Terraform Plan/Apply') {
            wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            if (type == "CNAME")
              sh """
                cd cname
                terraform init -var 'name=${name}' -var 'zone=${zone}' -var 'destination=${dest}' && \
                terraform plan -var 'name=${name}' -var 'zone=${zone}' -var 'destination=${dest}' -var-file=../ansible-management/terraform_vars/dns.tfvars 
                """
           else if ( type == "A" )
              sh """
                cd a
                terraform init -var 'name=${name}' -var 'zone=${zone}' -var 'destination=${dest}' && \
                terraform plan -var 'name=${name}' -var 'zone=${zone}' -var 'destination=${dest}' -var-file=../ansible-management/terraform_vars/dns.tfvars 
                """
            }
           } 
          }
        }
      }
      
      catch (err) {
      throw err
    } finally {
      deleteDir()
    }
  }
}

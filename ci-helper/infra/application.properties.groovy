[
  applicationName: 'demo-app',
  applicationUrl: "http://demo-app.${env.ENV_STACK}.platform.mnscorp.net",
  tillerNamespace: 'demo',
  k8sConfigName: "demo-config-${env.ENV_STACK}",
  k8sConfig: "${getVaultSecret2(githubTokenId, vaultUrl, "${vaultk8sSecretDataPath}/${env.ENV_STACK}", vaultPolicyName)['kubconfig']}",
  dockerRegistryCredentialsId: "${createShortUserPassRecord2(githubTokenId, vaultUrl, "${vaultNexusSecretDataPath}/nexus_data", 'nexus-creds', vaultPolicyName)}",
  dockerRegistryUrl: "${getVaultSecret2(githubTokenId, vaultUrl, "${vaultNexusSecretDataPath}/nexus_data", vaultPolicyName )['docker_url']}",
  mavenSettingsXml: "${getVaultSecret2(githubTokenId, vaultUrl, "${vaultNexusSecretDataPath}/settings", vaultPolicyName)['data']}",

  sonarProperties: [
      projectKey: "demo-app:${env.BRANCH_NAME.split('/').drop(1).join('-').toLowerCase()}",
      projectName: "demo-app-${env.BRANCH_NAME.split('/').drop(1).join('-').toLowerCase()}",
      projectVersion: '1.0',
      sources: 'src/main'
  ],

  files: [
            ["target/demo-0.0.1-SNAPSHOT.jar", "/"],
            ["ci-helper/infra/dockerfiles/app.Dockerfile", "/Dockerfile"]
        ]
]

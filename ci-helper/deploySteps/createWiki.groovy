import groovy.json.JsonBuilder;

def call(tag, app, image) {
    withCredentials([
        string(credentialsId: githubTokenId, variable: 'GITHUB_ACCESS_TOKEN')
    ]) {

    // Define variables
    def build_user = wrap([$class: 'BuildUser']) { env.BUILD_USER }
    def mode = 'Manual'

    if (!build_user) {
        build_user = sh (script: 'git show -s -1 --pretty=%an', returnStdout: true).trim()
        mode = 'On Merge'
    }

    def sha = getLastCommit(-1).substring(0,7)
    def date = env.BUILD_TIMESTAMP
    /*
    def d = new Date()
    def date = d.format('yyyy/MM/dd HH:mm:SS z', TimeZone.getTimeZone('Europe/London'))
    */      
    
    // Wiki contents
    def sidebar = """# [Home](https://github.com/${repo}/wiki)
- [dev](https://github.com/${repo}/wiki/dev)
- [prod](https://github.com/${repo}/wiki/prod)
"""

    def home = """# ${repo} wiki

Page contains information about all the deployments

- [dev](https://github.com/${repo}/wiki/dev)
- [prod](https://github.com/${repo}/wiki/prod)
"""

    // Checkout wiki
    sh """
        rm -rf wiki || echo 'No wiki folder' 
        git clone git@github.com:${repo}.wiki.git wiki
        cd wiki
        git config --global user.email 'jenkins-coreci@devops.mnscorp.net'
        git config --global user.name 'jenkins-coreci'
    """  
    
    // Changelog format
    def changelog = jsonParser('''
        {
            "dev": {
            },
            "prod": {
            }
        }
    ''')

    // Check changelog
    def file = '.changelog'
    def filePath = "wiki/${file}"

    if (fileExists("${filePath}")) {
        println('Changelog exists, loading...')
        changelog = jsonParser(readFile("${filePath}"))
    } else {
        println('Changelog not found. Creating...')
    }


    // Build info
    def build_info = jsonParser("""
    {
    "${date}": {
        "mode": "${mode}",
        "tag": "${tag}",
        "image": "${image}",
        "user": "${build_user}",
        "sha": "${sha}"
        }
    }
    """)                                                 

    // Add build info
    changelog."${env.ENV_STACK}" << build_info

    // Write changelog
    def changelog_postdeploy = new JsonBuilder(changelog).toPrettyString()
    writeFile(file: filePath, text: changelog_postdeploy)


    // Define  table
    def table_header = '''
# Deployment Details
| Date | Mode | Tag | Image | Build user | Commit SHA |
| --- | --- | --- | --- | --- | --- |'''

    // Initialize page
    def page = "${env.ENV_STACK}.md"
    sh "echo \"${table_header}\" > wiki/${page}"

    // Populate wiki page
    def table_row = ''
    changelog."${env.ENV_STACK}".sort().each { entry ->
        table_row = table_row + "| ${entry.key} | ${entry.value.mode} | ${entry.value.tag} | ${entry.value.image} | ${entry.value.user} | ${entry.value.sha} |\n"
    }
    sh "printf \"${table_row}\" >> wiki/${page}"
    
    // Push wiki and changelog
    sh """
        cd wiki
        echo "${home}" > Home.md
        echo "${sidebar}" > _Sidebar.md
        git add ${page} ${file} Home.md _Sidebar.md
        git commit -m 'Update due to deployment to ${env.ENV_STACK}'
        git push git@github.com:${repo}.wiki.git
        cd -
        rm -rf wiki
    """   
    }
}
return this
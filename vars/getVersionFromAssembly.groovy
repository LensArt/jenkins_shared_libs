#!/usr/bin/groovy

def call(Map map) {
    assert map.project != null
    assert map.repo != null

    //setting up version
    def fileName = "${map.project}/Properties/AssemblyInfo.cs"
    def fileText = readFile(file:fileName, encoding:"utf-8")

    if (fileText.startsWith("\uFEFF")) {
        fileText = fileText.substring(1)
    }

    def regex = /AssemblyVersion\("([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+)"\)/
    def matcher = fileText =~ regex
    def orig_version = matcher[0][1]

    matcher = null
    version = orig_version.replaceAll(/\.[0-9]+$/, ".${env.BUILD_NUMBER}")

    if (env.BRANCH_NAME == "master") {
       nuget_version = orig_version.replaceAll(/\.[0-9]+$/, "")
       withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'bc1decd6-9a21-48a5-9570-211fcf31b12f', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
            bat("git config user.email jenkins@my-clay.com")
            bat("git config user.name Jenkins")
            bat("git tag -a ${nuget_version} -m 'Jenkins'")
            bat("git push https://%GIT_USERNAME%:%GIT_PASSWORD%@bitbucket.org/claysolutions/${map.repo} --tags")
        }
    } else {
       nuget_version = orig_version.replaceAll(/\.[0-9]+$/, "-build" +"${env.BUILD_NUMBER}".padLeft(4, "0"))
    }

    fileText = fileText.replace(orig_version, "${version}")

    writeFile file: fileName, text: fileText, encoding: 'utf-8'

    return readFile(fileName)
}

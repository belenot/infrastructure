import jenkins.*
import hudson.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;
import hudson.model.*
import jenkins.model.*
import hudson.security.*
import jenkins.install.*

println('Init jenkins plugins.')

def jlc = JenkinsLocationConfiguration.get()
def jenkinsPublicUrl = System.getenv()["JENKINS_PUBLIC_URL"]
jlc.setUrl(jenkinsPublicUrl)
jlc.save()

final List<String> REQUIRED_PLUGINS = [
        "ace-editor",
        "ant",
        "antisamy-markup-formatter",
        "apache-httpcomponents-client-4-api",
        "authentication-tokens",
        "aws-credentials",
        "aws-java-sdk",
        "bouncycastle-api",
        "branch-api",
        "build-timeout",
        "cloudbees-folder",
        "command-launcher",
        "copyartifact",
        "credentials",
        "credentials-binding",
        "cvs",
        "display-url-api",
        "docker-commons",
        "docker-workflow",
        "durable-task",
        "email-ext",
        "external-monitor-job",
        "git",
        "git-client",
        "git-server",
        "github",
        "github-api",
        "github-branch-source",
        "gradle",
        "handlebars",
        "jackson2-api",
        "javadoc",
        "jdk-tool",
        "jquery-detached",
        "jsch",
        "junit",
        "ldap",
        "lockable-resources",
        "mailer",
        "mapdb-api",
        "matrix-auth",
        "matrix-project",
        "maven-plugin",
        "momentjs",
        "pam-auth",
        "pipeline-build-step",
        "pipeline-github-lib",
        "pipeline-graph-analysis",
        "pipeline-input-step",
        "pipeline-milestone-step",
        "pipeline-model-api",
        "pipeline-model-declarative-agent",
        "pipeline-model-definition",
        "pipeline-model-extensions",
        "pipeline-rest-api",
        "pipeline-stage-step",
        "pipeline-stage-tags-metadata",
        "pipeline-stage-view",
        "plain-credentials",
        "resource-disposer",
        "scm-api",
        "script-security",
        "ssh-agent",
        "ssh-credentials",
        "ssh-slaves",
        "structs",
        "subversion",
        "tap",
        "timestamper",
        "token-macro",
        "translation",
        "windows-slaves",
        "workflow-aggregator",
        "workflow-api",
        "workflow-basic-steps",
        "workflow-cps",
        "workflow-cps-global-lib",
        "workflow-durable-task-step",
        "workflow-job",
        "workflow-multibranch",
        "workflow-scm-step",
        "workflow-step-api",
        "workflow-support",
        "ws-cleanup",
]

if (Jenkins.instance.pluginManager.plugins.collect {
    it.shortName
}.intersect(REQUIRED_PLUGINS).size() != REQUIRED_PLUGINS.size()) {
    REQUIRED_PLUGINS.collect {
        Jenkins.instance.updateCenter.getPlugin(it).deploy()
    }.each {
        it.get()
    }
    Jenkins.instance.restart()
    println 'Run this script again after restarting to create the jobs!'
    throw new RestartRequiredException(null)
}
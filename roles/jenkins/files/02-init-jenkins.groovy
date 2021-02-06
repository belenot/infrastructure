/* https://github.com/coreos/jenkins-os/blob/master/init.groovy
 * Create all OS projects on a new Jenkins server.
 *
 * This entire script can be pasted directly into the text box found at
 * ${JENKINS_URL}/script to populate the server with OS jobs.  It will
 * define everything based on the contents of this repository.
 *
 * If any required plugins are not installed when this script is run,
 * they will be downloaded and installed automatically, and Jenkins will
 * be restarted to enable them.  In this case, this script must be run
 * again after the restart to create the jobs.
 *
 * Note that settings such as user permissions and secret credentials
 * are not handled by this script.
 */

/* Install required plugins and restart Jenkins, if necessary.  */

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

println('Start execution of custom initialization script.')

global_domain = Domain.global()
credentials_store =
        Jenkins.instance.getExtensionList(
                'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
        )[0].getStore()

credentials = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,null,"root",new BasicSSHUserPrivateKey.UsersPrivateKeySource(),"","")

credentials_store.addCredentials(global_domain, credentials)

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def adminUsername = System.getenv('JENKINS_ADMIN_USERNAME') ?: 'admin'
def adminPassword = System.getenv('JENKINS_ADMIN_PASSWORD') ?: 'password'
hudsonRealm.createAccount(adminUsername, adminPassword)
//hudsonRealm.createAccount("charles", "charles")

def instance = Jenkins.getInstance()
instance.setSecurityRealm(hudsonRealm)
instance.save()


def strategy = new GlobalMatrixAuthorizationStrategy()

//  Slave Permissions
//strategy.add(hudson.model.Computer.BUILD,'charles')
//strategy.add(hudson.model.Computer.CONFIGURE,'charles')
//strategy.add(hudson.model.Computer.CONNECT,'charles')
//strategy.add(hudson.model.Computer.CREATE,'charles')
//strategy.add(hudson.model.Computer.DELETE,'charles')
//strategy.add(hudson.model.Computer.DISCONNECT,'charles')

//  Credential Permissions
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.CREATE,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.DELETE,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.MANAGE_DOMAINS,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.UPDATE,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.VIEW,'charles')

//  Overall Permissions
//strategy.add(hudson.model.Hudson.ADMINISTER,'charles')
//strategy.add(hudson.PluginManager.CONFIGURE_UPDATECENTER,'charles')
//strategy.add(hudson.model.Hudson.READ,'charles')
//strategy.add(hudson.model.Hudson.RUN_SCRIPTS,'charles')
//strategy.add(hudson.PluginManager.UPLOAD_PLUGINS,'charles')

//  Job Permissions
//strategy.add(hudson.model.Item.BUILD,'charles')
//strategy.add(hudson.model.Item.CANCEL,'charles')
//strategy.add(hudson.model.Item.CONFIGURE,'charles')
//strategy.add(hudson.model.Item.CREATE,'charles')
//strategy.add(hudson.model.Item.DELETE,'charles')
//strategy.add(hudson.model.Item.DISCOVER,'charles')
//strategy.add(hudson.model.Item.READ,'charles')
//strategy.add(hudson.model.Item.WORKSPACE,'charles')

//  Run Permissions
//strategy.add(hudson.model.Run.DELETE,'charles')
//strategy.add(hudson.model.Run.UPDATE,'charles')

//  View Permissions
//strategy.add(hudson.model.View.CONFIGURE,'charles')
//strategy.add(hudson.model.View.CREATE,'charles')
//strategy.add(hudson.model.View.DELETE,'charles')
//strategy.add(hudson.model.View.READ,'charles')

//  Setting Anonymous Permissions
strategy.add(hudson.model.Hudson.READ,'anonymous')
strategy.add(hudson.model.Item.BUILD,'anonymous')
strategy.add(hudson.model.Item.CANCEL,'anonymous')
strategy.add(hudson.model.Item.DISCOVER,'anonymous')
strategy.add(hudson.model.Item.READ,'anonymous')

// Setting Admin Permissions
strategy.add(Jenkins.ADMINISTER, "admin")

// Setting easy settings for local builds
def local = System.getenv("BUILD").toString()
if(local == "local") {
    //  Overall Permissions
    strategy.add(hudson.model.Hudson.ADMINISTER,'anonymous')
    strategy.add(hudson.PluginManager.CONFIGURE_UPDATECENTER,'anonymous')
    strategy.add(hudson.model.Hudson.READ,'anonymous')
    strategy.add(hudson.model.Hudson.RUN_SCRIPTS,'anonymous')
    strategy.add(hudson.PluginManager.UPLOAD_PLUGINS,'anonymous')
}

instance.setAuthorizationStrategy(strategy)
instance.save()

println "Plugins were installed successfully"
Jenkins.instance.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)
///

def hudson = Hudson.get()

def project = hudson.getAllItems().findAll { it.name == 'test2' }[0]
def x = hudson.getAllItems().findAll { it.name == 'test' }[0]

if (project == null) {
    project = hudson.createProject(new org.jenkinsci.plugins.workflow.job.WorkflowJob.DescriptorImpl(), "test2")
}


if (project.definition == null || !(project.definition instanceof org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition)) {

    def userRemoteConfig = new hudson.plugins.git.UserRemoteConfig('git@github.com:belenot/vk-api.git', null, null, null)
    def branchSpec       = new hudson.plugins.git.BranchSpec('origin/master')
    def scm = new hudson.plugins.git.GitSCM( [userRemoteConfig], [branchSpec],
            false, [], null, null, [])

    project.definition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(scm, "/Jenkinsfile")
}
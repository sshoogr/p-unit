/*
 * Copyright (C) 2011-2020 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aestasit.puppet.integration.tests

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.CommandOutput
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import com.aestasit.infrastructure.ssh.log.SysOutEventLogger

import static org.junit.Assert.*

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Base Puppet integration test case. 
 * 
 * @author Andrey Adamovich
 *
 */
class BasePuppetIntegrationTest {

  static Random rng = new Random()

  static String host
  static String keyFile
  static String user
  static String modulePath

  static SshDslEngine ssh
  static SshOptions sshOptions

  @BeforeClass
  static void setUp() {
    readSystemProperties()
    setSslContext()
    createSshEngine()
  }

  @AfterClass
  static void tearDown() {
  }

  static void readSystemProperties() {
    host = System.getProperty("puppet.integration.tests.host")
    user  = System.getProperty("puppet.integration.tests.user") ?: 'root'
    keyFile  = System.getProperty("puppet.integration.tests.keyFile")
    modulePath  = System.getProperty("puppet.integration.tests.modulePath") ?: '/root/nc_puppet_modules/imported-modules:/root/nc_puppet_modules/custom-modules'
  }

  static void createSshEngine() {
    sshOptions = new SshOptions()
    sshOptions.with {
      logger = new SysOutEventLogger()
      defaultHost = host
      defaultUser = user
      defaultKeyFile = keyFile ? new File(keyFile) : null
      reuseConnection = true
      trustUnknownHosts = true
      execOptions.with { showOutput = true }
      scpOptions.with { verbose = true }
    }
    ssh = new SshDslEngine(sshOptions)
  }

  static void setSslContext() {

    def nullTrustManager = [
      checkClientTrusted: { chain, authType ->  },
      checkServerTrusted: { chain, authType ->  },
      getAcceptedIssuers: { null }
    ]

    def nullHostnameVerifier = [
      verify: { hostname, session -> true }
    ]

    SSLContext sc = SSLContext.getInstance("SSL")
    sc.init(null, [ nullTrustManager as X509TrustManager ] as TrustManager[], null)
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
    HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)
  }

  /*
   * Utility methods.
   */

  static void download(address, downloadTo) {
    def file = new FileOutputStream(downloadTo + "/" + address.tokenize("/")[-1])
    def out = new BufferedOutputStream(file)
    out << new URL(address).openStream()
    out.close()
  }

  static void session(Closure cl) {
    ssh.remoteSession(cl)
  }

  static String fileText(String filePath) {
    String content = null
    session {
      content = remoteFile(filePath).text
    }
    content
  }

  static Integer command(String command) {
    CommandOutput result = null
    session {
      result = exec(command: command, failOnError: false)
    }
    result?.exitStatus
  }

  static String commandOutput(String command, String defaultOutput = "") {
    CommandOutput result = null
    session {
      result = exec(command: command, failOnError: false)
    }
    result?.output?.toString()?.trim() ?: defaultOutput
  }

  static protected boolean check(String command, int exitCode = 0) {
    CommandOutput result = null
    session {
      result = exec(command: command, showCommand: false, showOutput: false, failOnError: false)
    }
    result?.exitStatus == exitCode
  }

  static String apply(String content, Integer repeat = 1) {
    String applyOutput = ""
    session {

      def index = Math.abs(rng.nextInt())

      // Generate dummy manifest with specified content.
      def manifest = "\nclass test${index} {\n ${content} \n}\n\nnode default { include test${index} }"
      remoteFile("test${index}.pp").text = manifest

      println "Applying manifest: $manifest"

      repeat.times {

        // Apply manifest.
        def result = exec "puppet apply -v --modulepath=${modulePath} test${index}.pp"

        result.output.eachLine { String line ->
          if (line.toLowerCase().contains('error:')) {
            throw new IntegrationError("Puppet returned an error: " + line)
          }
          applyOutput += line + "\n"
        }

      }

    }
    return applyOutput
  }
  
  static boolean fileExists(String path) {
    command("test -f $path") == 0
  }

  static boolean directoryExists(String path) {
    command("test -d $path") == 0
  }

  static Integer numberOfFiles(String path) {
    commandOutput("find ${path} 2>/dev/null | wc -l", "-1").toInteger()
  }

  static Integer permissions(String path) {
    commandOutput("stat -c %a $path", "0").toInteger()
  }

  static String fact(String fact) {
    commandOutput("facter $fact", "")
  }

  static String[] pgrep(String pattern) {
    commandOutput("pgrep -f $pattern", "-1").split('(?smi)$')
  }

  static boolean userExists(String user) {
    command("id $user") == 0
  }

  static boolean packageExists(String pkg) {
    command("rpm -qa | grep $pkg") == 0
  }

  static TestRule commandRule(final String comm) {
    new TestRule() {
      Statement apply(Statement stmt, Description desc) {
        BasePuppetIntegrationTest.command(comm)
        stmt
      }
    }
  }

  static TestRule printRule(final String message) {
    new TestRule() {
      Statement apply(Statement stmt, Description desc) {
        println(message)
        stmt
      }
    }
  }

  static TestRule uploadRule(final String classpathFile, String remoteDir = '/tmp') {
    new TestRule() {
      Statement apply(Statement stmt, Description desc) {
        def tmpFile = File.createTempFile("upload.", ".tmp")
        tmpFile.renameTo(new File(classpathFile).name)
        tmpFile.deleteOnExit()
        tmpFile << Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile)
        session {
          exec "mkdir -p $remoteDir"
          scp {
            from { localFile tmpFile }
            into { remoteDir(remoteDir) }
          }
        }
        tmpFile.delete()
        stmt
      }
    }
  }

}

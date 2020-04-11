# p-unit 

![GitHub Workflow Status](https://github.com/sshoogr/p-unit/workflows/Build/badge.svg)
![ASL2 Licensed](http://img.shields.io/badge/license-ASL2-blue.svg)
![Latest Version](https://api.bintray.com/packages/sshoogr/sshoogr/p-unit/images/download.svg)

## Overview

A **Groovy** library (based on **JUnit** - https://github.com/junit-team/junit, and **Sshoogr** - 
https://github.com/sshoogr/sshoogr) for creating integration tests of provisioning results of **Puppet** modules, **Chef** cookbook, **Ansible** playbooks and other.

The **p-unit** is designed to work with some virtualization or cloud solutions, which allow starting testable 
server instances on-demand. **Aestas/IT** usually uses **p-unit** together with **gramazon** (https://github.com/aestasit/gramazon) - 
a **Groovy** library and **Gradle** plugin for working with **Amazon EC2** instances.  

## Quick example

The following code shows an integration test for simple **JDBC** driver installation module:

    class JdbcInstallTest extends BasePuppetIntegrationTest {
          
      @Test
      def void verifyMySqlDriverIsDownloaded() {
        
        apply("""
          jdbc::install { 'mysql':
            media_library_host => '$mediaLibraryHost' 
          }
        """)
    
        assertTrue directoryExists("/opt/jdbc")
        assertTrue fileExists("/opt/jdbc/mysql-connector-5.1.23-generic-noarch.jar")
        
      }
    
      @Test
      def void verifyDerbyDriverIsDownloaded() {
        
        apply("""
          jdbc::install { 'derby':
            media_library_host => '$mediaLibraryHost' 
          }
        """)
    
        assertTrue directoryExists("/opt/jdbc")
        assertTrue fileExists("/opt/jdbc/derby-driver-10.9.1.0-generic-noarch.jar")
        
      }
            
      @BeforeClass
      def static void installTools() {
        command('yum -y -q install tree')
      }
    
      @Before
      def void cleanUp() {
        println "Removing installation directories:"
        command('rm -rf /opt/jdbc')
      }
    
      @After
      def void debug() {
        println "Checking directory content:"
        command('tree -ad /opt/jdbc')
      }
      
    }



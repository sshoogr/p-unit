# puppet-unit 

## Overview

A **Groovy** library (based on **JUnit** - https://github.com/junit-team/junit, and **Groovy SSH DSL** - 
https://github.com/aestasit/groovy-ssh-dsl) for creating integration tests of **Puppet** modules.

The **puppet-unit** is designed to work with some virtualization or cloud solutions, which allow starting testable 
server instances on-demand. **Aestas/IT** usually uses **puppet-unit** together with **gramazon** (https://github.com/aestasit/gramazon) - 
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



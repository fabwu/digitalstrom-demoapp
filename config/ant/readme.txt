*
* Copyright 2014 Roland Gisler
* Hochschule Luzern Technik & Architektur, Switzerland
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
*

Damit zusätzliche Funktionen in NetBeans zur Verfügung stehen bitte das folgende 
XML-Fragment in das Element <ide-actions> der Datei nbproject/project.xml
einfügen (sofern noch nicht enthalten!):  

            <action name="run.single">
               <script>config/ant/netbeans-targets.xml</script>
               <target>nb.run.single</target>
               <context>
                  <property>run.class</property>
                  <folder>src/main/java</folder>                        
                  <pattern>\.java$</pattern>
                  <format>java-name</format>
                  <arity>
                     <one-file-only/>
                  </arity>
               </context>
            </action>
            <action name="test.single">
               <script>config/ant/netbeans-targets.xml</script>
               <target>nb.test.single</target>
               <context>
                  <property>test.class</property>
                  <folder>src/test/java</folder>                        
                  <pattern>\.java$</pattern>
                  <format>java-name</format>
                  <arity>
                     <one-file-only/>
                  </arity>
               </context>
            </action>
            <action name="debug">
               <script>config/ant/netbeans-targets.xml</script>
               <target>nb.debug</target>
            </action>
            <action name="debug.single">
               <script>config/ant/netbeans-targets.xml</script>
               <target>nb.debug.single</target>
               <context>
                  <property>debug.class</property>
                  <folder>src/main/java</folder>                        
                  <pattern>\.java$</pattern>
                  <format>java-name</format>
                  <arity>
                     <one-file-only/>
                  </arity>
               </context>
            </action>
            <action name="debug.test.single">
               <script>config/ant/netbeans-targets.xml</script>
               <target>nb.debug.test.single</target>
               <context>
                  <property>test.class</property>
                  <folder>src/test/java</folder>                        
                  <pattern>\.java$</pattern>
                  <format>java-name</format>
                  <arity>
                     <one-file-only/>
                  </arity>
               </context>
            </action>


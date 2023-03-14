## SophosSE

This is a client server application for storing PDF files on a server in an encrypted way such that the 
server can not read the content of the files while still 
enabling the client to search the stored documents for 
keywords. 
Keep in mind that this is a learning project to use the Sophos scheme for a real world use case.
This means that we concentrated on the core algorithms and not on topics like deployment or adaptability.

## Dependencies

We use a couple of third party projects/functionalities for this solution. Since we use Maven as build tool, the pom.xml file contains all the dependencies. One dependency is not in 
the maven repository where we provided the jar files in the lib folder.

## How to use SophosSE

At the current state of development there are only the source 
files. If you want you can package it yourselve and deploy it.
If you do be careful to check the used paths in the code.
The intended use case is to start both server and client in
a development environment. First start the server with the 
main in the file Server.java and then the client with the main in Client.java. Then upload PDFs with the upload button 
at the bottom. You can then search for words with the search 
word field at the bottom and confirm your input with the search button. SophosSE lists all files containing the exact search word on the left side. If you click one file on the list on the left the PDF is opened in the right panel.

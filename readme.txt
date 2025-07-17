javac -cp ".:lib/mysql-connector-j-9.3.0.jar" -d out @sources.txt 


java -cp ".;out;lib/mysql-connector-j-9.3.0.jar" server.ServerApp
